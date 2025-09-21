package com.byrski.service;

import com.byrski.domain.entity.dto.Room;
import com.byrski.domain.entity.dto.Trade;
import com.byrski.domain.entity.dto.Tutorial;
import com.byrski.domain.entity.vo.request.BusStatusVo;
import com.byrski.domain.entity.vo.request.JoinRoomVo;
import com.byrski.domain.entity.vo.request.OrderVo;
import com.byrski.domain.entity.vo.request.SkiChoiceVo;
import com.byrski.domain.entity.vo.response.*;
import com.wechat.pay.java.service.refund.model.Refund;

import java.util.List;

public interface TradeService {

    /**
     * 生成订单，将订单数据添加到数据库，修改用户的信息，门票销量，活动当前参与人数，车站选择人数
     * @param order 请求订单信息，包含产品ID、站点ID、租赁物品ID等信息
     * @return 支付信息，包含支付所需的参数
     */
    PaymentVo makeOrder(OrderVo order);

    /**
     * 删除指定的订单
     * @param tradeId 订单ID
     * @return 删除是否成功
     */
    Boolean deleteTrade(Long tradeId);

    /**
     * 获取行程卡片信息
     * @param tradeId 订单ID
     * @return 行程卡片信息，包含滑雪场、酒店、上车点等信息
     */
    ItineraryCardVo getItineraryCard(Long tradeId);

    /**
     * 获取用户的所有行程列表
     * @return 行程列表，包含普通用户行程和领队行程
     */
    ItineraryListVo getItineraryList();

    /**
     * 获取行程详细信息
     * @param tradeId 订单ID
     * @return 行程详细信息，包含行程状态、车辆信息、时间安排等
     */
    ItineraryDetailVo getItineraryDetail(Long tradeId);

    /**
     * 领队获取行程站点信息列表
     * @param tradeId 订单ID
     * @return 站点信息列表
     */
    List<ItineraryDetailVo.StationInfo> leaderGetItineraryStationInfoList(Long tradeId);

    /**
     * 取消订单并申请退款
     * @param tradeId 订单ID
     * @return 退款信息
     */
    Refund cancelTrade(Long tradeId);

    /**
     * 设置去程已上车状态
     * @param tradeId 订单ID
     */
    void setDepartureBoarded(Long tradeId);

    /**
     * 设置返程已上车状态
     * @param tradeId 订单ID
     */
    void setReturnBoarded(Long tradeId);

    /**
     * 获取当前教程信息
     * @param tradeId 订单ID
     * @return 教程信息
     */
    Tutorial getTutorial(Long tradeId);

    /**
     * 进入下一步教程
     * @param tradeId 订单ID
     * @return 下一步教程信息
     */
    Tutorial nextTutorial(Long tradeId);

    /**
     * 跳过当前教程
     * @param tradeId 订单ID
     * @return 跳过后的教程信息
     */
    Tutorial skipTutorial(Long tradeId);

    /**
     * 获取教程列表
     * @param tutorialId 教程ID
     * @return 教程列表，包含教程图片
     */
    List<List<TutorialWithImage>> getTutorialList(Long tutorialId);

    /**
     * 获取指定状态的订单列表
     * @param status 订单状态
     * @return 订单元信息列表
     */
    List<TradeMeta> getTradeList(Integer status);

    /**
     * 获取订单详细信息
     * @param tradeId 订单ID
     * @return 订单详细信息
     */
    TradeDetailVo getTradeDetail(Long tradeId);



    /**
     * 获取管理员可见的订单列表
     * @return 订单列表
     */
    List<Trade> getAdminTradeList();

    /**
     * 更新订单信息
     * @param trade 订单信息
     */
    void updateTrade(Trade trade);

    /**
     * 取消过期未支付的订单
     * @param outTradeNo 商户订单号
     */
    void cancelExpiredTrade(String outTradeNo);

    /**
     * 获取站点人数统计
     * @param tradeId 订单ID
     * @param route 路线
     * @param stationId 站点ID
     * @return 人数统计信息
     */
    HeadCountVo getHeadCount(Long tradeId, String route, Long stationId);

    /**
     * 检查票务是否已验证
     * @param tradeId 订单ID
     * @return 是否已验证
     */
    Boolean getTicketChecked(Long tradeId);

    /**
     * 更新车辆状态
     * @param busStatusVo 车辆状态信息
     */
    void updateBusStatus(BusStatusVo busStatusVo);

    /**
     * 标记站点出发完成
     * @param busId 车辆ID
     * @param stationId 站点ID
     */
    void goFinished(Long busId, Long stationId);

//    void arriveFinished(Long busId);
//
//    void skiFinished(Long busId);
//
//    void returnFinished(Long busId);

    /**
     * 获取已验票乘客统计
     * @param tradeId 订单ID
     * @return 验票人数统计
     */
    TicketCheckHeadCountVo getCheckedPassenger(Long tradeId);

    /**
     * 检查房间分配状态
     * @param tradeId 订单ID
     * @return 房间分配状态
     */
    CheckRoomVo checkRoomAlloc(Long tradeId);

    /**
     * 创建房间
     * @param tradeId 订单ID
     * @return 创建的房间信息
     */
    Room createRoom(Long tradeId);

    /**
     * 查询房间信息
     * @param code 房间编码
     * @return 房间信息
     */
    Room queryRoom(String code);

    /**
     * 加入房间
     * @param code 房间加入信息
     * @return 是否加入成功
     */
    Boolean joinRoom(JoinRoomVo code);

    /**
     * 升级行程
     * @param tradeId 订单ID
     */
    void upgradeItinerary(Long tradeId);

    /**
     * 选择滑雪装备
     * @param skiChoiceVo 滑雪装备选择信息
     */
    void chooseSki(SkiChoiceVo skiChoiceVo);

    /**
     * 归还租赁物品
     * @param tradeId 订单ID
     */
    void returnItem(Long tradeId);

    /**
     * 取消拼房
     * @param tradeId 订单ID
     * @return 是否成功取消拼房
     */
    Boolean cancelRoom(Long tradeId);
}
