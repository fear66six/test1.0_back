package com.byrski.infrastructure.mapper.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byrski.domain.entity.dto.BusMove;
import com.byrski.domain.entity.dto.Trade;
import com.byrski.domain.entity.dto.ticket.RoomTicket;
import com.byrski.domain.enums.*;
import com.byrski.common.exception.ByrSkiException;
import com.byrski.infrastructure.mapper.TradeMapper;
import com.byrski.domain.user.LoginUser;
import com.byrski.strategy.factory.TicketStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Options;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TradeMapperService extends ServiceImpl<TradeMapper, Trade> {

    private final TradeTicketsMapperService tradeTicketsMapperService;
    private final TicketStrategyFactory ticketStrategyFactory;

    public TradeMapperService(TradeTicketsMapperService tradeTicketsMapperService, TicketStrategyFactory ticketStrategyFactory) {
        this.tradeTicketsMapperService = tradeTicketsMapperService;
        this.ticketStrategyFactory = ticketStrategyFactory;
    }

    /**
     * 向数据库中添加订单，用于makeOrder接口
     * @param trade 订单信息
     * @return 带有id的订单信息
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    public Long addTrade(Trade trade) {
        this.save(trade);
        return trade.getId();
    }

    /**
     * 使用订单号更新订单状态，用于退款的通知回调接口，退款单中仅包含trade的订单号信息
     * @param outTradeNo 订单号
     * @param tradeStatus 订单状态
     * @return 是否更新成功
     */
    public boolean updateOrderStatus(String outTradeNo, TradeStatus tradeStatus) {
        return this.update(new LambdaUpdateWrapper<Trade>()
                        .eq(Trade::getOutTradeNo, outTradeNo)
                        .set(Trade::getStatus, tradeStatus.getCode())
                );
    }

    /**
     * 更新订单状态
     * @param tradeId 订单id
     * @param tradeStatus 订单状态
     * @return 是否更新成功
     */
    public boolean updateStatus(Long tradeId, TradeStatus tradeStatus) {
        return this.lambdaUpdate()
                .eq(Trade::getId, tradeId)
                .set(Trade::getStatus, tradeStatus.getCode())
                .update();
    }

    public boolean updateStationId(Long tradeId, Long stationId) {
        return this.lambdaUpdate()
                .eq(Trade::getId, tradeId)
                .set(Trade::getStationId, stationId)
                .update();
    }

    public boolean clearStationId(Long tradeId) {
        return this.lambdaUpdate()
                .eq(Trade::getId, tradeId)
                .set(Trade::getStationId, -1)
                .set(Trade::getStatus, TradeStatus.CONFIRMING.getCode())
                .update();
    }

    public boolean setBusAndBusMove(Long tradeId, BusMove busMove) {
        return this.lambdaUpdate()
                .eq(Trade::getId, tradeId)
                .set(Trade::getBusId, busMove.getBusId())
                .set(Trade::getBusMoveId, busMove.getId())
                .update();
    }

    public boolean setRoomId(Long tradeId, Long roomId) {
        return this.lambdaUpdate()
                .eq(Trade::getId, tradeId)
                .set(Trade::getRoomId, roomId)
                .update();
    }

    public boolean not_exists(String outTradeNo) {
        return this.lambdaQuery()
                .eq(Trade::getOutTradeNo, outTradeNo)
                .one() == null;
    }

    public Boolean checkTradeExist(Long tradeId) {
        LambdaQueryWrapper<Trade> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Trade::getId, tradeId)
                .ne(Trade::getStatus, TradeStatus.DELETED.getCode());
        return this.exists(queryWrapper);
    }

    public Boolean checkValidTradeExist(Long activityId) {
        Long userId = LoginUser.getLoginUserId();

        // 创建查询条件
        LambdaQueryWrapper<Trade> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Trade::getActivityId, activityId)
                .eq(Trade::getUserId, userId)
                .in(Trade::getStatus, Arrays.asList(
                        TradeStatus.UNPAID.getCode(),
                        TradeStatus.PAID.getCode(),
                        TradeStatus.LOCKED.getCode(),
                        TradeStatus.REFUNDING.getCode()
                ));

        // 查询是否存在符合条件的记录
        return this.exists(queryWrapper);
    }

    public Trade getByOutTradeNo(String outTradeNo) {
        LambdaQueryWrapper<Trade> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Trade::getOutTradeNo, outTradeNo)
                .ne(Trade::getStatus, TradeStatus.DELETED.getCode());
        Trade trade = this.getOne(queryWrapper);
        if (trade == null || !Objects.equals(trade.getUserId(), LoginUser.getLoginUserId())) {
            throw new ByrSkiException(ReturnCode.TRADE_NOT_EXIST);
        }
        return trade;
    }

    /**
     * 根据订单号获取所有相关的Trade记录（用于多人票）
     * @param outTradeNo 订单号
     * @return Trade记录列表
     */
    public List<Trade> getAllByOutTradeNo(String outTradeNo) {
        LambdaQueryWrapper<Trade> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Trade::getOutTradeNo, outTradeNo)
                .ne(Trade::getStatus, TradeStatus.DELETED.getCode())
                .orderByAsc(Trade::getId);
        return this.list(queryWrapper);
    }



    public Trade getByOutTradeNoWithoutAuth(String outTradeNo) {
        LambdaQueryWrapper<Trade> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Trade::getOutTradeNo, outTradeNo)
                .ne(Trade::getStatus, TradeStatus.DELETED.getCode());
        Trade trade = this.getOne(queryWrapper);
        if (trade == null) {
            throw new ByrSkiException(ReturnCode.TRADE_NOT_EXIST);
        }
        return trade;
    }

    /**
     * 判断订单状态是否为取消或已退款或已归还
     * @param tradeId 订单id
     * @return 订单信息
     */
    public Boolean checkTradeFinish(Long tradeId) {
        LambdaQueryWrapper<Trade> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Trade::getId, tradeId)
                .in(Trade::getStatus, Arrays.asList(
                        TradeStatus.CANCELLED.getCode(),
                        TradeStatus.REFUNDED.getCode(),
                        TradeStatus.RETURN.getCode()
                ));
        return this.exists(queryWrapper);
    }

    public Boolean checkTradePaid(Long tradeId) {
        LambdaQueryWrapper<Trade> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Trade::getId, tradeId)
                .ne(Trade::getStatus, TradeStatus.UNPAID.getCode());
        return this.exists(queryWrapper);
    }

    // 伪删除，仅修改状态
    public Boolean deleteById(Long tradeId) {
        LambdaQueryWrapper<Trade> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Trade::getId, tradeId)
                .ne(Trade::getStatus, TradeStatus.DELETED.getCode());
        Trade trade = this.getOne(queryWrapper);
        trade.setStatus(TradeStatus.DELETED.getCode());
        return this.updateById(trade);
    }

    public Trade get(Long tradeId) {
        Trade trade = this.getById(tradeId);
        if (trade == null || trade.getStatus() == TradeStatus.DELETED.getCode()) {
            throw new ByrSkiException(ReturnCode.TRADE_NOT_EXIST);
        }
        if (!Objects.equals(trade.getUserId(), LoginUser.getLoginUserId())) {
            throw new ByrSkiException(ReturnCode.TRADE_NOT_EXIST);
        }
        return trade;
    }

    public Trade getWithoutOwner(Long tradeId) {
        Trade trade = this.getById(tradeId);
        if (trade == null || trade.getStatus() == TradeStatus.DELETED.getCode()) {
            throw new ByrSkiException(ReturnCode.TRADE_NOT_EXIST);
        }
        return trade;
    }

    public List<Trade> getByUserId(Long userId) {
        LambdaQueryWrapper<Trade> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Trade::getUserId, userId)
                .ne(Trade::getStatus, TradeStatus.DELETED.getCode())
                .orderByDesc(Trade::getCreateTime);
        return this.list(queryWrapper);
    }

    public List<Trade> getByUserIdAndStatusSet(Long userId, Set<Integer> statusSet) {
        LambdaQueryWrapper<Trade> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Trade::getUserId, userId)
                .in(Trade::getStatus, statusSet)
                .orderByDesc(Trade::getCreateTime);
        return this.list(queryWrapper);
    }

    public List<Trade> getReadyByUserId(Long userId) {
        LambdaQueryWrapper<Trade> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Trade::getUserId, userId)
                .in(Trade::getStatus, Arrays.asList(
                        TradeStatus.PAID.getCode(),
                        TradeStatus.LOCKED.getCode(),
                        TradeStatus.LEADER.getCode(),
                        TradeStatus.RETURN.getCode(),
                        TradeStatus.CONFIRMING.getCode()
                ));
        return this.list(queryWrapper);
    }

    public void setDepartureBoarded(Long tradeId) {
        Trade trade = this.get(tradeId);
        trade.setGoBoarded(true);
        this.updateById(trade);
    }

    public void setReturnBoarded(Long tradeId) {
        Trade trade = this.get(tradeId);
        trade.setReturnBoarded(true);
        this.updateById(trade);
    }

    public void setTicketChecked(Long tradeId) {
        Trade trade = this.getById(tradeId);
        // 此处断言该订单未验票
        trade.setTicketCheck(true);
        this.updateById(trade);
    }

    /**
     * 获取同一辆车的订单列表
     * @param activityId 活动id
     * @param busId 车辆id
     * @return 订单列表
     */
    public List<Trade> getTotalList(Long activityId, Long busId) {
        return this.lambdaQuery()
                .notIn(Trade::getStatus, Arrays.asList(
                        TradeStatus.CANCELLED.getCode(),
                        TradeStatus.DELETED.getCode(),
                        TradeStatus.REFUNDING.getCode(),
                        TradeStatus.REFUNDED.getCode(),
                        TradeStatus.LEADER.getCode()
                ))                .eq(Trade::getActivityId, activityId)
                .eq(Trade::getBusId, busId)
                .list();
    }

    /**
     * 根据站点id获取订单列表
     * @param activityId 活动id
     * @param stationId 站点id
     * @param busId 车辆id
     * @return 订单列表
     */
    public List<Trade> getTotalListByStationId(Long activityId, Long stationId, Long busId) {
        return this.lambdaQuery()
                .notIn(Trade::getStatus, Arrays.asList(
                        TradeStatus.CANCELLED.getCode(),
                        TradeStatus.DELETED.getCode(),
                        TradeStatus.REFUNDING.getCode(),
                        TradeStatus.REFUNDED.getCode(),
                        TradeStatus.LEADER.getCode()
                     ))
                .eq(Trade::getActivityId, activityId)
                .eq(Trade::getStationId, stationId)
                .eq(Trade::getBusId, busId)
                .list();
    }

    /**
     * 获取返程未上车的订单列表
     * @param activityId 活动id
     * @param busId 车辆id
     * @return 订单列表
     */
    public List<Trade> getNotReturnBoardedList(Long activityId, Long busId) {
        return this.lambdaQuery()
                .notIn(Trade::getStatus, Arrays.asList(
                        TradeStatus.CANCELLED.getCode(),
                        TradeStatus.DELETED.getCode(),
                        TradeStatus.REFUNDING.getCode(),
                        TradeStatus.REFUNDED.getCode(),
                        TradeStatus.LEADER.getCode()
                ))                .eq(Trade::getActivityId, activityId)
                .eq(Trade::getBusId, busId)
                .eq(Trade::getReturnBoarded, false)
                .list();
    }

    /**
     * 获取去程未上车的订单列表
     * @param activityId 活动id
     * @param busId 车辆id
     * @return 订单列表
     */
    public List<Trade> getNotGoBoardedList(Long activityId, Long busId) {
        return this.lambdaQuery()
                .notIn(Trade::getStatus, Arrays.asList(
                        TradeStatus.CANCELLED.getCode(),
                        TradeStatus.DELETED.getCode(),
                        TradeStatus.REFUNDING.getCode(),
                        TradeStatus.REFUNDED.getCode(),
                        TradeStatus.LEADER.getCode()
                ))
                .eq(Trade::getActivityId, activityId)
                .eq(Trade::getBusId, busId)
                .eq(Trade::getGoBoarded, false)
                .list();
    }

    /**
     * 获取验票未完成的订单列表
     * @param activityId 活动id
     * @param busId 车辆id
     * @return 订单列表
     */
    public List<Trade> getNotCheckedList(Long activityId, Long busId) {
        return this.lambdaQuery()
             .notIn(Trade::getStatus, Arrays.asList(
                        TradeStatus.CANCELLED.getCode(),
                        TradeStatus.DELETED.getCode(),
                        TradeStatus.REFUNDING.getCode(),
                        TradeStatus.REFUNDED.getCode(),
                        TradeStatus.LEADER.getCode()
                ))
                .eq(Trade::getActivityId, activityId)
                .eq(Trade::getBusId, busId)
                .eq(Trade::getTicketCheck, false)
                .list();
    }

    /**
     * 根据站点id获取去程未上车的订单列表
     * @param activityId 活动id
     * @param stationId 站点id
     * @param busId 车辆id
     * @return 订单列表
     */
    public List<Trade> getNotGoBoardedListByStationId(Long activityId, Long stationId, Long busId) {
        return this.lambdaQuery()
                .notIn(Trade::getStatus, Arrays.asList(
                        TradeStatus.CANCELLED.getCode(),
                        TradeStatus.DELETED.getCode(),
                        TradeStatus.REFUNDING.getCode(),
                        TradeStatus.REFUNDED.getCode(),
                        TradeStatus.LEADER.getCode()
                ))
                .eq(Trade::getActivityId, activityId)
                .eq(Trade::getStationId, stationId)
                .eq(Trade::getBusId, busId)
                .eq(Trade::getGoBoarded, false)
                .list();
    }

    /**
     * 全局方法，取消属于activityId的未支付订单
     * @param activityId 活动id
     * @return 需要取消的未支付订单列表
     */
    public List<Trade> getUnpaidTradeByActivityId(Long activityId) {
        return this.lambdaQuery()
             .eq(Trade::getActivityId, activityId)
             .eq(Trade::getStatus, TradeStatus.UNPAID.getCode())
             .list();
    }

    /**
     * 获取活动的所有有效trade
     * @param activityId 活动id
     * @return trade列表
     */
    public List<Trade> getTradeByActivityId(Long activityId) {
        return this.lambdaQuery()
                .eq(Trade::getActivityId, activityId)
                .notIn(Trade::getStatus,Arrays.asList(
                        TradeStatus.CANCELLED.getCode(),
                        TradeStatus.REFUNDING.getCode(),
                        TradeStatus.REFUNDED.getCode(),
                        TradeStatus.DELETED.getCode(),
                        TradeStatus.LEADER.getCode()
                ))
                .list();
    }

    /**
     * 获取所有可能参与分房的订单
     * @param activityId 活动id
     * @return trade列表
     */
    public List<Trade> getAllocRoomTradeByActivityId(Long activityId) {
        return this.lambdaQuery()
                .eq(Trade::getActivityId, activityId)
                .notIn(Trade::getStatus,Arrays.asList(
                        TradeStatus.CANCELLED.getCode(),
                        TradeStatus.REFUNDING.getCode(),
                        TradeStatus.REFUNDED.getCode(),
                        TradeStatus.DELETED.getCode()
                ))
                .list();
    }

    /**
     * 获取一些活动下的的所有trade，用于导出报表
     * @param activityIds 活动id列表
     * @return trade列表
     */
    public List<Trade> getAllTradeByActivityIds(List<Long> activityIds) {
        if (activityIds == null || activityIds.isEmpty()) {
            return this.list();
        } else {
            return this.lambdaQuery()
                    .in(Trade::getActivityId, activityIds)
                    .in(Trade::getStatus, Arrays.asList(
                            TradeStatus.PAID.getCode(),
                            TradeStatus.LOCKED.getCode(),
                            TradeStatus.RETURN.getCode(),
                            TradeStatus.CONFIRMING.getCode(),
                            TradeStatus.LEADER.getCode()
                    ))
                    .list();
        }

    }

    /**
     * 获取活动的所有参与分车的有效trade
     * @param activityId 活动id
     * @return trade列表，不包含类型为单雪票的订单，不包含领队订单
     */
    public List<Trade> getDispatchTradeByActivityId(Long activityId) {
        return this.lambdaQuery()
                .eq(Trade::getActivityId, activityId)
                .ne(Trade::getType, ProductType.SKI.getCode())
                .notIn(Trade::getStatus,Arrays.asList(
                        TradeStatus.CANCELLED.getCode(),
                        TradeStatus.REFUNDING.getCode(),
                        TradeStatus.REFUNDED.getCode(),
                        TradeStatus.DELETED.getCode(),
                        TradeStatus.LEADER.getCode()
                ))
                .list();
    }

    /**
     * 获取活动的所有有效trade分页数据
     * @param activityIds 活动id
     * @param page 页码
     * @param pageSize 每页大小
     * @param stationIds 站点id
     * @param busId 大巴车id
     * @return trade分页数据
     */
    public Page<Trade> getTradeByActivityIds(List<Long> activityIds, int page, int pageSize,
                                             List<Long> stationIds, Long busId, Integer status, Boolean goBoarded,
                                             Boolean returnBoarded, RoomType roomType, Set<Long> userIdsWithNameFilter) {

        // 构建基础查询条件
        QueryWrapper<Trade> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("update_time");
        queryWrapper.ne("status", TradeStatus.DELETED.getCode());

        if (activityIds != null && !activityIds.isEmpty()) {
            queryWrapper.in("activity_id", activityIds);
        }
        if (stationIds != null && !stationIds.isEmpty()) {
            queryWrapper.in("station_id", stationIds);
        }
        if (busId != null) {
            queryWrapper.eq("bus_id", busId);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        if (goBoarded != null) {
            queryWrapper.eq("go_boarded", goBoarded);
        }
        if (returnBoarded != null) {
            queryWrapper.eq("return_boarded", returnBoarded);
        }
        if (userIdsWithNameFilter != null && !userIdsWithNameFilter.isEmpty()) {
            queryWrapper.in("user_id", userIdsWithNameFilter);
        }

        // 如果需要按 roomType 筛选
        if (roomType != null) {
            // 1. 先查询所有符合条件的 trade（不分页）
            List<Trade> allTrades = this.list(queryWrapper);
            if (allTrades.isEmpty()) {
                return new Page<>(page, pageSize);
            }

            // 2. 获取这些 trade 对应的 RoomTicket 信息
            Map<Long, Long> roomTicketIdMap = tradeTicketsMapperService
                    .getRoomTicketIdMapByTradeIds(allTrades.stream()
                            .map(Trade::getId)
                            .collect(Collectors.toList()));

            // 3. 筛选出符合 roomType 的 tradeIds
            List<Long> filteredTradeIds = allTrades.stream()
                    .map(Trade::getId)
                    .filter(id -> {
                        Long ticketId = roomTicketIdMap.get(id);
                        if (ticketId != null) {
                            RoomTicket roomTicket = (RoomTicket) ticketStrategyFactory.getStrategy(TicketType.ROOM).getTicketById(ticketId);
                            return roomTicket != null && roomType.equals(roomTicket.getRoomType());
                        }
                        return false;
                    })
                    .collect(Collectors.toList());

            // 如果没有符合条件的数据，直接返回空分页
            if (filteredTradeIds.isEmpty()) {
                return new Page<>(page, pageSize);
            }

            // 4. 将筛选后的 tradeIds 添加到查询条件中
            queryWrapper.in("id", filteredTradeIds);
        }

        // 用queryWrapper查询出符合的全部订单到一个list
        List<Trade> tradeList = this.list(queryWrapper);

        // 执行分页查询
        Page<Trade> tradePage = new Page<>(page, pageSize);
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, tradeList.size());

        Page<Trade> result = new Page<>(page, pageSize);
        if (start < tradeList.size()) {
            result.setRecords(tradeList.subList(start, end));
        }
        result.setTotal(tradeList.size());
        return result;
    }

    public List<Trade> getByRoomId(Long roomId) {
        return this.lambdaQuery()
                .eq(Trade::getRoomId, roomId)
                .notIn(Trade::getStatus, Arrays.asList(
                        TradeStatus.CANCELLED.getCode(),
                        TradeStatus.REFUNDING.getCode(),
                        TradeStatus.REFUNDED.getCode(),
                        TradeStatus.DELETED.getCode()
                ))
                .list();
    }

    /**
     * 获取活动的所有带有Leader的有效trade
     * @param activityId 活动id
     * @return trade列表
     */
    public List<Trade> getTradeByActivityIdWithLeader(Long activityId) {
        return this.lambdaQuery()
                .eq(Trade::getActivityId, activityId)
                .notIn(Trade::getStatus,Arrays.asList(
                        TradeStatus.CANCELLED.getCode(),
                        TradeStatus.REFUNDING.getCode(),
                        TradeStatus.REFUNDED.getCode(),
                        TradeStatus.DELETED.getCode()
                ))
                .list();
    }

    public void updateLeaderTrade(Long leaderId, Long activityId, Long busId) {
        this.lambdaUpdate()
                .eq(Trade::getUserId, leaderId)
                .eq(Trade::getActivityId, activityId)
                .set(Trade::getBusId, busId)
                .set(Trade::getStatus, TradeStatus.LEADER.getCode())
                .update();
    }

    public void removeLeaderTrade(Long leaderId, Long activityId, Long busId) {
        this.lambdaUpdate()
                .eq(Trade::getUserId, leaderId)
                .eq(Trade::getActivityId, activityId)
                .eq(Trade::getBusId, busId)
                .set(Trade::getBusId, null)
                .set(Trade::getStatus, TradeStatus.LOCKED.getCode())
                .update();
    }

    public void cancelRoom(Long roomId) {
        this.lambdaUpdate()
                .eq(Trade::getRoomId, roomId)
                .set(Trade::getRoomId, null)
                .update();
    }

    public boolean cleanRoom(Long tradeId) {
        return this.lambdaUpdate()
                .eq(Trade::getId, tradeId)
                .set(Trade::getRoomId, null)
                .update();
    }

    public Trade getByActivityIdAndUserId(Long activityId, Long userId) {
        return this.lambdaQuery()
                .eq(Trade::getActivityId, activityId)
                .eq(Trade::getUserId, userId)
                .one();
    }
}
