package com.byrski.service.impl;

import com.byrski.common.dispatch.BusInDispatch;
import com.byrski.common.dispatch.BusPlanner;
import com.byrski.common.utils.RoomAllocUtils;
import com.byrski.domain.entity.dto.*;
import com.byrski.domain.enums.*;
import com.byrski.domain.entity.vo.response.ActivityDetailVo;
import com.byrski.domain.entity.vo.response.HomePageVo;
import com.byrski.domain.entity.vo.response.Place;
import com.byrski.common.exception.ByrSkiException;
import com.byrski.infrastructure.mapper.impl.*;
import com.byrski.infrastructure.repository.manager.ProductManager;
import com.byrski.service.ActivityService;
import com.byrski.domain.user.LoginUser;
import com.byrski.common.utils.DocUtils;
import com.byrski.common.utils.PayUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class ActivityServiceImpl implements ActivityService {

    private final BusMoveMapperService busMoveMapperService;
    private final SnowfieldMapperService snowfieldMapperService;
    private final ActivityTemplateMapperService activityTemplateMapperService;
    private final ActivityMapperService activityMapperService;
    private final TradeMapperService tradeMapperService;
    private final SnowfieldImageMapperService snowfieldImageMapperService;
    private final StationMapperService stationMapperService;
    private final StationInfoMapperService stationInfoMapperService;
    private final AreaMapperService areaMapperService;
    private final AreaLowerBoundMapperService areaLowerBoundMapperService;
    private final BusTypeMapperService busTypeMapperService;
    private final BusMapperService busMapperService;
    private final AccountMapperService accountMapperService;
    private final SchoolMapperService schoolMapperService;
    private final PayUtils payUtils;
    private final DocUtils docUtils;
    private final ProductManager productManager;
    private final RoomAllocUtils roomAllocUtils;

    @Value("${bus.staff-num}")
    private Integer staffNum;
    @Value("${bus.small_capacity}")
    private Integer smallCapacity;
    @Value("${bus.large_capacity}")
    private Integer largeCapacity;
    @Value("${bus.small_cost}")
    private Double smallCost;
    @Value("${bus.large_cost}")
    private Double largeCost;
    @Value("${test.activity.id}")
    private Long testActivityId;

    public ActivityServiceImpl(
            SnowfieldMapperService snowfieldMapperService,
            ActivityTemplateMapperService activityTemplateMapperService,
            ActivityMapperService activityMapperService,
            TradeMapperService tradeMapperService,
            SnowfieldImageMapperService snowfieldImageMapperService,
            StationMapperService stationMapperService,
            StationInfoMapperService stationInfoMapperService,
            AreaMapperService areaMapperService,
            AreaLowerBoundMapperService areaLowerBoundMapperService,
            BusTypeMapperService busTypeMapperService,
            BusMapperService busMapperService,
            BusMoveMapperService busMoveMapperService,
            AccountMapperService accountMapperService,
            SchoolMapperService schoolMapperService,
            PayUtils payUtils, DocUtils docUtils, ProductManager productManager, RoomAllocUtils roomAllocUtils) {
        this.snowfieldMapperService = snowfieldMapperService;
        this.activityTemplateMapperService = activityTemplateMapperService;
        this.activityMapperService = activityMapperService;
        this.tradeMapperService = tradeMapperService;
        this.snowfieldImageMapperService = snowfieldImageMapperService;
        this.stationMapperService = stationMapperService;
        this.stationInfoMapperService = stationInfoMapperService;
        this.areaMapperService = areaMapperService;
        this.areaLowerBoundMapperService = areaLowerBoundMapperService;
        this.busTypeMapperService = busTypeMapperService;
        this.busMapperService = busMapperService;
        this.busMoveMapperService = busMoveMapperService;
        this.accountMapperService = accountMapperService;
        this.schoolMapperService = schoolMapperService;
        this.payUtils = payUtils;
        this.docUtils = docUtils;
        this.productManager = productManager;
        this.roomAllocUtils = roomAllocUtils;
    }

    /**
     * 获取活动详情
     * @param activityId 活动id
     * @return 活动详情
     */
    @Override
    public ActivityDetailVo getActivityDetail(Long activityId) {
        Activity activity = activityMapperService.get(activityId);
        if (activity == null) {
            throw new ByrSkiException(ReturnCode.ACTIVITY_NOT_EXIST);
        }
        Long activityTemplateId = activity.getActivityTemplateId();
        ActivityTemplate activityTemplate = activityTemplateMapperService.get(activityTemplateId);
        Long snowfieldId = activity.getSnowfieldId();
        Snowfield snowfield = snowfieldMapperService.get(snowfieldId);
        Doc schedule = docUtils.getDocByActivityTemplateIdAndType(activityTemplateId, DocType.SCHEDULE.getCode());
        Doc detail = docUtils.getDocByActivityTemplateIdAndType(activityTemplateId, DocType.DETAIL.getCode());
        Doc attention = docUtils.getDocByActivityTemplateIdAndType(activityTemplateId, DocType.ATTENTION.getCode());
        return ActivityDetailVo.builder()
                .location(snowfield.getLocation())
                .cover(snowfield.getCover())
                .name(activityTemplate.getName())
                .detail(detail)
                .schedule(schedule)
                .attention(attention)
                .images(snowfieldImageMapperService.getImagesById(snowfieldId))
                .isAvailable(LocalDateTime.now().isBefore(activity.getSignupDdlDate()))
                .isValid(tradeMapperService.checkValidTradeExist(activityId))
                .signupDdlDate(activity.getSignupDdlDate())
                .build();
    }

    /**
     * 获取活动的所有上车点
     * @param activityId 活动id
     * @return 上车点信息
     */
    @Override
    public Map<String, List<Place>> listStationByActivityId(Long activityId) {

        activityMapperService.get(activityId);

        List<Station> stations = stationMapperService.getByActivityId(activityId);
        Map<String, List<Place>> locationDataMap = new HashMap<>();

        for (Station station : stations) {
            StationInfo stationInfo = stationInfoMapperService.get(station.getStationInfoId());
            String areaName = areaMapperService.getAreaNameById(stationInfo.getAreaId());
            Place place = Place.builder()
                    .id(station.getId())
                    .area(areaName)
                    .school(stationInfo.getPosition())
                    .campus(stationInfo.getCampus())
                    .location(stationInfo.getLocation())
                    .choicePeopleNum(station.getChoicePeopleNum())
                    .targetPeopleNum(station.getTargetPeopleNum())
                    .build();
            locationDataMap.computeIfAbsent(areaName, k -> new ArrayList<>()).add(place);
        }

        return locationDataMap;
    }

    @Override
    public HomePageVo getHomePage() {
        List<Activity> activities = activityMapperService.getHomePageActivities(LocalDate.now());
        List<HomePageVo.Activity> activityList = new ArrayList<>();
        for (Activity activity : activities) {
            Long snowfieldId = activity.getSnowfieldId();
            Snowfield snowfield = snowfieldMapperService.get(snowfieldId);
            Area area = areaMapperService.get(snowfield.getAreaId());
            activityList.add(
                    HomePageVo.Activity.builder()
                            .snowfieldId(snowfieldId)
                            .snowfieldName(snowfield.getName())
                            .snowfieldArea(area.getCityName() + "·" + area.getAreaName())
                            .beginDate(DateTimeFormatter.ofPattern("yyyy年MM月dd日").format(activity.getActivityBeginDate()))
                            .cover(snowfield.getCover())
                            .minPrice(productManager.getMinProductPriceByActivityId(activity.getId()))
                            .createTime(activity.getCreateTime())
                            .build()
            );
        }
        activityList.sort(Comparator.comparing(HomePageVo.Activity::getCreateTime).reversed());
        if (!LoginUser.isLoginUser()) {
            return HomePageVo.builder()
                    .activities(activityList)
                    .build();
        }
        Long userId = LoginUser.getLoginUserId();
        Account account = accountMapperService.getById(userId);
        Integer leadTimes = null;
        String intro = null;
        if (Objects.equals(account.getIdentity(), UserIdentity.LEADER.getCode())) {
            leadTimes = account.getLeadTimes();
            intro = account.getIntro();
        }
        String school = null;
        if (account.getSchoolId() != null) {
            school = schoolMapperService.getById(account.getSchoolId()).getName();
        }
        return HomePageVo.builder()
                .user(
                        HomePageVo.User.builder()
                                .id(userId)
                                .name(account.getUsername())
                                .school(school)
                                .identity(account.getIdentity())
                                .isStudent(account.getIsStudent())
                                .registerDays((int)Duration.between(account.getRegisterTime(), LocalDateTime.now()).toDays())
                                .savedMoney(account.getSavedMoney())
                                .leadTimes(leadTimes)
                                .intro(intro)
                                .build()
                )
                .activities(activityList)
                .build();
    }

    @Override
    public List<Activity> getActivityList() {
        return activityMapperService.list();
    }

    @Override
    public List<ActivityTemplate> getActivityTemplateList() {
        return activityTemplateMapperService.list();
    }

    @Override
    public void updateActivityTemplate(ActivityTemplate activityTemplate) {
        activityTemplateMapperService.updateById(activityTemplate);
    }

    @Override
    public void updateActivity(Activity activity) {
        activityMapperService.updateById(activity);
    }

    /**
     * 绑定定时任务，每天凌晨0点执行，检查到期的活动并设置状态为已过期
     * 然后删除活动的无效订单、无效上车点，锁定订单
     */
    @Override
    public void activityDeadLineCheck() {
        activityMapperService.getRegistrationDeadlineActivities(LocalDateTime.now()).forEach(activity -> {
            log.info("活动 {} 已到报名截止日期", activity.getId());
            activity.setStatus(ActivityStatus.DEADLINE_NOT_LOCKED.getCode());
            activityDead(activity.getId());
            activityMapperService.updateById(activity);
        });
    }

    @Override
    public void TestDead(Long activityId) {
        activityDead(activityId);
    }

    /**
     * 活动截止报名后的处理
     * @param activityId 活动id
     */
    private void activityDead(Long activityId) throws ByrSkiException {
        deleteUnpaidTrade(activityId);
        deleteInvalidStation(activityId);
        lockValidTrade(activityId);
    }

    /**
     * 删除活动的无效订单，也即报名截止但仍未支付的订单
     * @param activityId 活动id
     */
    private void deleteUnpaidTrade(Long activityId) {
        List<Trade> unpaidTrades = tradeMapperService.getUnpaidTradeByActivityId(activityId);
        for (Trade trade : unpaidTrades) {
            payUtils.cancelTrade(trade);
        }
    }

    /**
     * 删除活动的无效上车点
     * @param activityId 活动id
     */
    private void deleteInvalidStation(Long activityId) {
        // 先根据地区限制，删除人数少于限制的地区中的全部上车点
        List<AreaLowerBound> areaLowerBounds = areaLowerBoundMapperService.getAreaLowerBoundListByActivityId(activityId);
        for (AreaLowerBound areaLowerBound : areaLowerBounds) {
            Long areaId = areaLowerBound.getAreaId();
            // 获取该地区的所有上车点
            List<Station> stationsInArea = stationMapperService.getStationsByActivityIdAndAreaId(activityId, areaId);
            // 计算选择在该区域上车的人数
            int sumInArea = stationsInArea.stream().mapToInt(Station::getChoicePeopleNum).sum();
            // 如果人数少于限制，删除该区域的全部上车点
            boolean peopleNumberInsufficient = sumInArea < areaLowerBound.getLowerLimit();

            if (peopleNumberInsufficient) {
                log.info("将活动 {} 中地区 {} 的全部上车点状态更新为无效", activityId, areaId);
                // 获取需要更新的站点ID列表
                List<Long> stationIds = stationsInArea.stream()
                        .map(Station::getId)
                        .toList();

                // 构建批量更新的实体列表
                List<Station> stationsToUpdate = stationIds.stream()
                        .map(id -> {
                            Station station = Station.builder().build();
                            station.setId(id);
                            station.setStatus(StationStatus.INVALID.getCode());  // 设置状态为无效
                            return station;
                        })
                        .toList();

                // 执行批量更新
                stationMapperService.updateBatchById(stationsToUpdate);
            }
        }
        // 再删除本次活动中所有上车点中选择人数少于目标人数的上车点
        stationMapperService.updateInvalidStationByActivityId(activityId);
    }

    /**
     * 对于上车点未被删除的以及不用上车点的，进行锁票操作
     * @param activityId 活动id
     */
    private void lockValidTrade(Long activityId) {
        // 先获取该活动所有仍存在的上车点
        List<Station> validStations = stationMapperService.getByActivityId(activityId);
        // 对于每一个属于该活动的有效trade，检查其上车点属性是否存在于有效上车点中
        List<Trade> trades = tradeMapperService.getTradeByActivityId(activityId);
        for (Trade trade : trades) {
            if (trade.getStationId() == null) {
                // 如果上车点为null，直接锁票并跳过
                tradeMapperService.updateStatus(trade.getId(), TradeStatus.LOCKED);
                log.info("订单 {} 为单雪票，无需上车，已锁定", trade.getId());
            } else if (validStations.stream().map(Station::getId).toList().contains(trade.getStationId())) {
                // 如果在有效上车点中，锁定订单
                tradeMapperService.updateStatus(trade.getId(), TradeStatus.LOCKED);
                log.info("订单 {} 上车点有效，已锁定", trade.getId());

            } else {
                //不在有效上车点中，将上车点设置为-1，然后将状态设置为待确认上车点
                tradeMapperService.clearStationId(trade.getId());
                log.info("订单 {} 上车点无效 ，状态设置为待确认上车点", trade.getId());
            }
        }
    }

    @Override
    public void activityLockedCheck() {
        activityMapperService.getLockedActivities(LocalDateTime.now()).forEach(activity -> {
            log.info("活动 {} 已到锁票日期", activity.getId());
            activity.setStatus(ActivityStatus.LOCKED.getCode());
            activityLocked(activity.getId());
            activityMapperService.updateById(activity);
        });
    }

    @Override
    public void TestBegin(Long activityId) {
        updateTradeStationInfo(activityId);
        roomAllocUtils.allocateRoom(activityId);
    }

    @Override
    public void activityBeginCheck() {
        activityMapperService.getBeginActivities(LocalDate.now()).forEach(activity -> {
            log.info("活动 {} 已到开始日期", activity.getId());
            updateTradeStationInfo(activity.getId());
            roomAllocUtils.allocateRoom(activity.getId());
            activity.setStatus(ActivityStatus.START.getCode());
            activityMapperService.updateById(activity);
        });
    }

    @Override
    public void TestLock(Long activityId) {
        activityLocked(activityId);
    }

    private void activityLocked(Long activityId) {
        handleRemainTrade(activityId);

        // 分车
        List<Integer> vehicleCapacity = new ArrayList<>();
        List<Double> vehicleCosts = new ArrayList<>();
        getBusTypes(vehicleCapacity, vehicleCosts, activityId);

//        // 获取按区域分组的station信息
//        Map<Long, Map<Long, Integer>> areaStations =
//                stationMapperService.getStationPeopleNumByActivityIdGroupByArea(activityId);
//
//        // 对每个区域分别进行分车处理
//        for (Map.Entry<Long, Map<Long, Integer>> areaEntry : areaStations.entrySet()) {
//
//            // key--stationId, value--peopleNum
//            Map<Long, Integer> stationsInArea = areaEntry.getValue();
//
//            // 计算该区域的总人数
//            Integer areaTotalNum = stationsInArea.values().stream()
//                    .mapToInt(Integer::intValue)
//                    .sum();
//
//            // 对该区域进行分车，得到每一辆bus的途径结果，存放于route中。
//            List<BusInDispatch> busListWithRoute = BusPlanner.planRouteTop(
//                    areaTotalNum,
//                    stationsInArea,
//                    vehicleCapacity,
//                    vehicleCosts
//            );
//            saveBusAndBusMove(busListWithRoute, activityId);
//            // 不再在第二截止时间进行车辆订单分配
////            updateTradeStationInfo(activityId);
////            verifyTradeStationInfo(activityId);
//        }

        List<Station> stations = stationMapperService.getByActivityId(activityId);
        // key--stationId, value--peopleNum
            Map<Long, Integer> stationsWithPeople = stations.stream()
                    .collect(Collectors.toMap(Station::getId, Station::getChoicePeopleNum));

            // 计算该区域的总人数
            Integer areaTotalNum = stationsWithPeople.values().stream()
                    .mapToInt(Integer::intValue)
                    .sum();

            // 对该区域进行分车，得到每一辆bus的途径结果，存放于route中。
            List<BusInDispatch> busListWithRoute = BusPlanner.planRouteTop(
                    areaTotalNum,
                    stationsWithPeople,
                    vehicleCapacity,
                    vehicleCosts
            );
            saveBusAndBusMove(busListWithRoute, activityId);
    }

    /**
     * 处理未处理的订单，对于有上车点的订单，直接锁定，对于没有上车点的订单，直接退款
     * @param activityId 活动id
     */
    private void handleRemainTrade(Long activityId) {
        // 获取该activityId下所有非delete，canceled，leader的订单
        List<Trade> trades = tradeMapperService.getTradeByActivityId(activityId);
        for (Trade trade : trades) {
            // 对于已经有上车点的trade，直接锁定，上车点为null的已经在上一步中锁定过了
            if (trade.getType() == ProductType.SKI ||(trade.getStationId() != null && trade.getStationId() != -1)) {
                if (trade.getStatus() != TradeStatus.LOCKED.getCode()) {
                    tradeMapperService.updateStatus(trade.getId(), TradeStatus.LOCKED);
                    log.info("订单 {} 已锁定", trade.getId());
                }
            } else {
                payUtils.cancelTrade(trade);
                log.info("订单 {} 为已退款", trade.getId());
            }
        }
    }

    /**
     * 获取本次活动的两种车型信息
     * @param vehicleCapacity 车辆容量
     * @param vehicleCosts 车辆花费
     * @param activityId 活动id
     */
    private void getBusTypes(List<Integer> vehicleCapacity, List<Double> vehicleCosts, Long activityId) {
        // 获取本次活动的两种车型信息
        List<BusType> busTypes = busTypeMapperService.getBusTypeByActivityId(activityId);

        if (busTypes.size() == 2) {
            for (BusType busType : busTypes) {
                vehicleCapacity.add(busType.getPassengerNum() - staffNum);
                vehicleCosts.add(busType.getPrice());
            }
        } else {
            // 如果没有设置车型，使用默认值
            log.warn("活动 {} 车型设置出错，使用默认值", activityId);
            vehicleCapacity.add(smallCapacity - staffNum);
            vehicleCapacity.add(largeCapacity - staffNum);
            vehicleCosts.add(smallCost);
            vehicleCosts.add(largeCost);
        }
    }

    /**
     * 保存bus和bus_move表
     * @param busListWithRoute 本次活动的所有分车列表，其中的bus为分车结果
     * @param activityId 活动id
     */
    private void saveBusAndBusMove(List<BusInDispatch> busListWithRoute, Long activityId) {
        int index = 1;
        // busListWithRoute代表车辆总数，用它来填充bus表和bus_move表
        for (BusInDispatch busInDispatch : busListWithRoute) {
            Bus bus = Bus.builder()
                    .carryPeople(busInDispatch.getCapacity() - busInDispatch.getEmptySeats())
                    .maxPeople(busInDispatch.getCapacity())
                    .activityId(activityId)
                    .route(busInDispatch.getRoute().toString())
                    .name(index++ + "号车")
                    .build();
            busMapperService.save(bus);
            Map<Long, Integer> route = busInDispatch.getRoute();
            for (Map.Entry<Long, Integer> entry : route.entrySet()) {
                BusMove busMove = BusMove.builder()
                        .stationPeopleNum(entry.getValue())
                        .remainNum(entry.getValue())
                        .busId(bus.getId())
                        .stationId(entry.getKey())
                        .activityId(activityId)
                        .build();
                busMoveMapperService.save(busMove);
            }
        }
    }

    /**
     * 对某次活动下的trade进行更新，将trade的busMoveId、busId、stationId更新为分车结果中的信息
     * @param activityId 活动id
     */
    private void updateTradeStationInfo(Long activityId) {
        List<Trade> trades = tradeMapperService.getDispatchTradeByActivityId(activityId);
        List<BusMove> busMoves = busMoveMapperService.getByActivityId(activityId);
        Map<Long, List<BusMove>> remainBusMoveMap = busMoves.stream()
                .collect(Collectors.groupingBy(BusMove::getStationId));

        for (Trade trade : trades) {
            if (trade.getBusId() != null) {
                continue;
            }
            Long StationId = trade.getStationId();
            List<BusMove> busMovesInStation = remainBusMoveMap.get(StationId);
            if (busMovesInStation!= null) {
                for (BusMove busMove : busMovesInStation) {
                    if (busMove.getRemainNum() > 0) {
                        if (!tradeMapperService.setBusAndBusMove(trade.getId(), busMove)) {
                            throw new ByrSkiException(ReturnCode.DATABASE_ERROR);
                        }
                        busMove.setRemainNum(busMove.getRemainNum() - 1);
                        break;
                    }
                }
            }
        }
    }

    /**
     * 对一次活动下所有trade进行校验，检查trade的busMoveId、busId、stationId是否一致
     * @param activityId 活动id
     */
    private void verifyTradeStationInfo(Long activityId) {
        List<Trade> trades = tradeMapperService.getTradeByActivityId(activityId);
        trades.stream().collect(Collectors.groupingBy(Trade::getBusMoveId))
                .forEach((busMoveId, tradesInStation) -> {
                    BusMove busMove = busMoveMapperService.getById(busMoveId);
                    log.info("{} BusMove: {}", busMoveId, busMove);
                    verifyTradeWithBusMove(busMoveId, tradesInStation, busMove);
                });
    }

    /**
     * 针对一个busMove下的所有trade进行校验，分别检查trade总数和该busMove的station_peoplenum、busId、stationId是否一致
     * @param busMoveId 校验的busMove的id
     * @param tradesInStation 该busMove下的所有trade
     * @param busMove 该busMove的信息
     */
    private static void verifyTradeWithBusMove(Long busMoveId, List<Trade> tradesInStation, BusMove busMove) {
        if (busMove.getStationPeopleNum() == tradesInStation.size()) {
            log.info("{} BusMove人数校验通过", busMoveId);
        } else {
            log.error("{} BusMove人数校验失败，trade中为{}而bus_move中为{}", busMoveId, tradesInStation.size(), busMove.getStationPeopleNum());
        }
        if (Objects.equals(busMove.getBusId(), tradesInStation.get(0).getBusId())) {
            log.info("{} Trade Bus校验通过", busMoveId);
        } else {
            log.error("{} Trade Bus校验失败，trade中为{}而bus_move中为{}", busMoveId, tradesInStation.get(0).getBusId(), busMove.getBusId());
        }
        if (Objects.equals(busMove.getStationId(), tradesInStation.get(0).getStationId())) {
            log.info("{} Trade Station校验通过", busMoveId);
        } else {
            log.error("{} Trade Station校验失败，trade中为{}而bus_move中为{}", busMoveId, tradesInStation.get(0).getStationId(), busMove.getStationId());
        }
    }

}
