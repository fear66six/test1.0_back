package com.byrski.service.impl;

import com.byrski.common.utils.*;
import com.byrski.domain.entity.dto.*;
import com.byrski.domain.entity.dto.ticket.RoomTicket;
import com.byrski.domain.entity.BaseTicketEntity;
import com.byrski.domain.entity.vo.request.*;
import com.byrski.domain.entity.vo.response.*;
import com.byrski.domain.enums.*;

import com.byrski.common.exception.ByrSkiException;
import com.byrski.infrastructure.mapper.impl.*;
import com.byrski.infrastructure.mapper.impl.SchoolMapperService;
import com.byrski.infrastructure.repository.manager.ProductManager;
import com.byrski.service.TradeService;
import com.byrski.service.WechatPayService;
import com.byrski.service.CouponService;
import com.byrski.domain.user.LoginUser;
import com.byrski.strategy.factory.TicketStrategyFactory;
import com.wechat.pay.java.service.refund.model.Refund;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.byrski.common.utils.Const.STATUS_MAPPING;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class TradeServiceImpl implements TradeService {

    private final WechatPayService wechatPayService;
    private final TradeMapperService tradeMapperService;
    private final TradeUtils tradeUtils;
    private final AccountMapperService accountMapperService;
    private final SnowfieldMapperService snowfieldMapperService;
    private final ActivityMapperService activityMapperService;
    private final ActivityTemplateMapperService activityTemplateMapperService;
    private final AreaMapperService areaMapperService;
    private final StationInfoMapperService stationInfoMapperService;
    private final StationMapperService stationMapperService;
    private final WxGroupMapperService wxGroupMapperService;
    private final BusMapperService busMapperService;
    private final BusMoveMapperService busMoveMapperService;
    private final RentItemMapperService rentItemMapperService;
    private final TutorialImageMapperService tutorialImageMapperService;
    private final RentOrderMapperService rentOrderMapperService;
    private final RentUtils rentUtils;
    private final InfoUtils infoUtils;
    private final PayUtils payUtils;
    private final RoomMapperService roomMapperService;
    private final TutorialMapperService tutorialMapperService;
    private final TutorialEdgeMapperService tutorialEdgeMapperService;
    private final DocUtils docUtils;
    private final ProductManager productManager;
    private final TradeTicketsMapperService tradeTicketsMapperService;
    private final TicketMapperService ticketMapperService;
    private final TicketStrategyFactory ticketStrategyFactory;
    private final CouponService couponService;
    private final SchoolMapperService schoolMapperService;

    public TradeServiceImpl(
            WechatPayService wechatPayService,
            TradeMapperService tradeMapperService,
            TradeUtils tradeUtils,
            AccountMapperService accountMapperService,
            SnowfieldMapperService snowfieldMapperService,
            ActivityMapperService activityMapperService,
            ActivityTemplateMapperService activityTemplateMapperService,
            AreaMapperService areaMapperService,
            StationInfoMapperService stationInfoMapperService,
            StationMapperService stationMapperService,
            WxGroupMapperService wxGroupMapperService,
            BusMapperService busMapperService,
            BusMoveMapperService busMoveMapperService,
            RentItemMapperService rentItemMapperService,
            TutorialImageMapperService tutorialImageMapperService,
            RentOrderMapperService rentOrderMapperService,
            RentUtils rentUtils,
            InfoUtils infoUtils,
            PayUtils payUtils,
            RoomMapperService roomMapperService,
            TutorialMapperService tutorialMapperService,
            TutorialEdgeMapperService tutorialEdgeMapperService,
            DocUtils docUtils,
            ProductManager productManager,
            TradeTicketsMapperService tradeTicketsMapperService,
            TicketMapperService ticketMapperService,
            TicketStrategyFactory ticketStrategyFactory,
            CouponService couponService,
            SchoolMapperService schoolMapperService) {
        this.wechatPayService = wechatPayService;
        this.tradeMapperService = tradeMapperService;
        this.tradeUtils = tradeUtils;
        this.accountMapperService = accountMapperService;
        this.snowfieldMapperService = snowfieldMapperService;
        this.activityMapperService = activityMapperService;
        this.activityTemplateMapperService = activityTemplateMapperService;
        this.areaMapperService = areaMapperService;
        this.stationInfoMapperService = stationInfoMapperService;
        this.stationMapperService = stationMapperService;
        this.wxGroupMapperService = wxGroupMapperService;
        this.busMapperService = busMapperService;
        this.busMoveMapperService = busMoveMapperService;
        this.rentItemMapperService = rentItemMapperService;
        this.tutorialImageMapperService = tutorialImageMapperService;
        this.rentOrderMapperService = rentOrderMapperService;
        this.rentUtils = rentUtils;
        this.infoUtils = infoUtils;
        this.payUtils = payUtils;
        this.roomMapperService = roomMapperService;
        this.tutorialMapperService = tutorialMapperService;
        this.tutorialEdgeMapperService = tutorialEdgeMapperService;
        this.docUtils = docUtils;
        this.productManager = productManager;
        this.tradeTicketsMapperService = tradeTicketsMapperService;
        this.ticketMapperService = ticketMapperService;
        this.ticketStrategyFactory = ticketStrategyFactory;
        this.couponService = couponService;
        this.schoolMapperService = schoolMapperService;
    }

    @Override
    public PaymentVo makeOrder(OrderVo order) {
        // 验证用户信息列表
        if (order.getUsers() == null || order.getUsers().isEmpty()) {
            throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "用户信息不能为空");
        }
        
        Product product = productManager.getProductById(order.getProductId());
        Long activityId = product.getActivityId();

        Activity activity = activityMapperService.get(activityId);
        if (!LocalDateTime.now().isBefore(activity.getSignupDdlDate())) {
            throw new ByrSkiException(ReturnCode.ACTIVITY_DEADLINE);
        }
        
        // 检查活动参与人数是否足够
        int userCount = order.getUsers().size();
        if (activity.getCurrentParticipant() + userCount > activity.getTargetParticipant()) {
            throw new ByrSkiException(ReturnCode.ACTIVITY_FULL);
        }

        // 订单类型信息
        ProductType type = product.getType();
        Account account = accountMapperService.getById(LoginUser.getLoginUserId());
        if (product.getIsStudent() && !account.getIsStudent()) {
            throw new ByrSkiException(ReturnCode.NOT_STUDENT);
        }

        Long stationId = order.getStationId();
        if (stationId == null && !Objects.equals(type, ProductType.SKI)) {
            throw new ByrSkiException(ReturnCode.STATION_NOT_EXIST);
        }
        if (tradeMapperService.checkValidTradeExist(activityId)) {
            throw new ByrSkiException(ReturnCode.TRADE_EXISTS);
        }

        // 计算订单金额信息
        int basePrice = product.getPrice() * userCount; // 基础票价 * 用户数量
        int total = basePrice;
        List<Long> rentItemIds = order.getRentItemIds();
        int costRent = 0;
        if (rentItemIds != null && !rentItemIds.isEmpty()) {
            for (Long rentItemId : rentItemIds) {
                RentItem rentItem = rentItemMapperService.getById(rentItemId);
                if (rentItem == null) {
                    throw new ByrSkiException(ReturnCode.DATABASE_ERROR.getCode(), "租借物品不存在");
                }
                total += rentItem.getPrice();
                total += rentItem.getDeposit();
                costRent += rentItem.getPrice();
            }
        }
        if (order.getRoomTicketId() != null) {
            total += ticketStrategyFactory.getStrategy(TicketType.ROOM).getTicketById(order.getRoomTicketId()).getBaseTicket().getPrice();
        }

        // 优惠券处理
        Double discountAmount = 0.0;
        if (order.getUserCouponId() != null) {
            // 验证优惠券可用性
            if (!couponService.isUserCouponAvailable(order.getUserCouponId(), 
                    LoginUser.getLoginUserId(), product.getId(), (double)total, userCount)) {
                throw new ByrSkiException(ReturnCode.COUPON_THRESHOLD_NOT_MET);
            }
            
            // 计算优惠金额
            discountAmount = couponService.calculateDiscount(order.getUserCouponId(), (double)total);
            total -= discountAmount.intValue();
        }

        WxGroup wxGroup = wxGroupMapperService.getByActivityId(activityId);
        String onTradeNo = tradeUtils.getBusinessNo(BusinessNoPrefix.TRADE);
        
        // 为每个用户创建独立的Trade记录
        List<Long> tradeIds = new ArrayList<>();
        for (int i = 0; i < userCount; i++) {
            OrderVo.OrderUserInfo userInfo = order.getUsers().get(i);
            
            // 计算单个用户的费用（基础票价 + 租赁费用分摊 + 房间费用分摊）
            int singleUserBasePrice = product.getPrice();
            int singleUserRentCost = 0;
            int singleUserTotal = singleUserBasePrice;
            
            // 租赁费用分摊
            if (rentItemIds != null && !rentItemIds.isEmpty()) {
                for (Long rentItemId : rentItemIds) {
                    RentItem rentItem = rentItemMapperService.getById(rentItemId);
                    singleUserRentCost += rentItem.getPrice();
                    singleUserTotal += rentItem.getPrice();
                    singleUserTotal += rentItem.getDeposit();
                }
            }
            
            // 房间费用分摊
            if (order.getRoomTicketId() != null) {
                BaseTicketEntity roomTicketEntity = ticketStrategyFactory.getStrategy(TicketType.ROOM).getTicketById(order.getRoomTicketId());
                if (roomTicketEntity instanceof RoomTicket) {
                    RoomTicket roomTicket = (RoomTicket) roomTicketEntity;
                    singleUserTotal += roomTicket.getBaseTicket().getPrice();
                }
            }
            
            // 优惠券费用分摊
            int singleUserDiscount = 0;
            if (discountAmount > 0) {
                singleUserDiscount = (int)(discountAmount / userCount);
                if (i == userCount - 1) { // 最后一个用户承担剩余的优惠金额
                    singleUserDiscount = discountAmount.intValue() - (singleUserDiscount * (userCount - 1));
                }
                singleUserTotal -= singleUserDiscount;
            }
            
            Trade trade = Trade.builder()
                    .outTradeNo(onTradeNo)
                    .type(type)
                    .stationId(stationId)
                    .productId(product.getId())
                    .activityId(activityId)
                    .activityTemplateId(product.getActivityTemplateId())
                    .snowfieldId(product.getSnowfieldId())
                    .status(TradeStatus.UNPAID.getCode())
                    .total(singleUserTotal)
                    .userId(LoginUser.getLoginUserId())
                    .wxgroupId(wxGroup.getId())
                    .costTicket(singleUserBasePrice)
                    .costRent(singleUserRentCost)
                    .userCount(1) // 每个Trade记录代表一个用户
                    .userCouponId(order.getUserCouponId()) // 设置优惠券ID
                    .build();
            
            Long tradeId = tradeMapperService.addTrade(trade);
            tradeIds.add(tradeId);
            
            // 更新用户信息到Account表
            accountMapperService.updateUserDataByTrade(userInfo.getName(), userInfo.getGender(), 
                userInfo.getIdCardNumber(), userInfo.getPhone(), userInfo.getSchoolId());
        }

        // 更新优惠券关联到第一个订单ID
        if (order.getUserCouponId() != null) {
            // 使用CouponService统一处理优惠券使用
            couponService.useCoupon(order.getUserCouponId(), tradeIds.get(0), (double)(total + discountAmount.intValue()));
        }

        // 为每个Trade记录保存租赁信息
        if (rentItemIds != null) {
            for (Long tradeId : tradeIds) {
                rentItemIds.forEach(rentItemId -> rentOrderMapperService.save(RentOrder.builder()
                        .rentDay(activityTemplateMapperService.getById(product.getActivityTemplateId()).getDurationDays())
                        .tradeId(tradeId)
                        .rentItemId(rentItemId)
                        .userId(LoginUser.getLoginUserId())
                        .build()));
            }
        }

        // 为每个Trade记录保存房间信息
        if (order.getRoomTicketId() != null) {
            for (Long tradeId : tradeIds) {
                tradeTicketsMapperService.saveTradeTickets(tradeId, order.getRoomTicketId(), TicketType.ROOM);
            }
            ticketMapperService.addSale(order.getRoomTicketId());
        }

        updateSystemDataAfterOrder(order, product, activityId, stationId);

        // 构建TradeVo调用callWechatPay（使用第一个Trade记录）
        TradeVo tradeVo = TradeVo.builder()
                .outTradeNo(onTradeNo)
                .total(total) // 总金额
                .description(order.getDescription())
                .openid(order.getOpenid())
                .build();
        
        // 计算原始金额（不含优惠）
        int originalAmount = basePrice;
        if (rentItemIds != null && !rentItemIds.isEmpty()) {
            for (Long rentItemId : rentItemIds) {
                RentItem rentItem = rentItemMapperService.getById(rentItemId);
                originalAmount += rentItem.getPrice();
                originalAmount += rentItem.getDeposit();
            }
        }
        if (order.getRoomTicketId() != null) {
            originalAmount += ticketStrategyFactory.getStrategy(TicketType.ROOM).getTicketById(order.getRoomTicketId()).getBaseTicket().getPrice();
        }
        
        return wechatPayService.callWechatPay(tradeVo, tradeIds.get(0), originalAmount, discountAmount.intValue(), userCount);
    }

    /**
     * 根据订单信息更新系统数据：主产品销量，活动当前参与人数，上车点选择人数
     * @param order 订单信息
     * @param product 主产品
     * @param activityId 活动ID
     * @param stationId 上车点ID
     */
    private void updateSystemDataAfterOrder(OrderVo order, Product product, Long activityId, Long stationId) {
        // 更新主产品销量
        productManager.addSale(product.getId());
        
        // 更新参与人数（根据用户数量）
        int userCount = order.getUsers().size();
        for (int i = 0; i < userCount; i++) {
            activityMapperService.addCurrentParticipant(activityId);
        }
        
        // 更新上车点选择人数
        if (stationId != null && stationId != -1) {
            stationMapperService.addChoicePeopleNum(stationId);
        }
    }

    @Override
    public Boolean deleteTrade(Long tradeId) {
        tradeMapperService.get(tradeId);
        if (!tradeMapperService.checkTradeFinish(tradeId)) {
            throw new ByrSkiException(ReturnCode.TRADE_NOT_FINISH);
        }
        return tradeMapperService.deleteById(tradeId);
    }

    @Override
    public ItineraryCardVo getItineraryCard(Long tradeId) {
        Trade trade = tradeMapperService.get(tradeId);

        if (!tradeMapperService.checkTradePaid(tradeId)) {
            throw new ByrSkiException(ReturnCode.TRADE_UNPAID);
        }

        Snowfield snowfield = snowfieldMapperService.get(trade.getSnowfieldId());
        Account account = accountMapperService.getById(LoginUser.getLoginUserId());
        WxGroup wxGroup = wxGroupMapperService.get(trade.getWxgroupId());
        RoomTicket roomTicket = getRoomTicketWithTradeId(tradeId);
        if (trade.getStationId() != null && trade.getStationId() != -1) {
            Station station = stationMapperService.get(trade.getStationId());
            StationInfo stationInfo = stationInfoMapperService.get(station.getStationInfoId());
            String from = areaMapperService.get(stationInfo.getAreaId()).getAreaName();
            String to = areaMapperService.get(snowfield.getAreaId()).getAreaName();

            BusMove busMove = new BusMove();
            if (trade.getBusMoveId() != null) {
                busMove = busMoveMapperService.get(trade.getBusMoveId());
            }

            String busMoveTimeStr = Optional.ofNullable(busMove)
                    .map(BusMove::getTime)
                    .map(time -> time.format(DateTimeFormatter.ofPattern("HH:mm a")))
                    .orElse("");
            return ItineraryCardVo.builder()
                    .skiResortName(snowfield.getName())
                    .location(snowfield.getLocation())
                    .skiResortPhone(snowfield.getPhone())
                    .hotel(Optional.ofNullable(roomTicket)
                            .map(RoomTicket::getDescription)
                            .orElse(""))
                    .fromArea(from)
                    .toArea(to)
                    .stationLocation(stationInfo.getPosition())
                    .busMoveTime(busMoveTimeStr)
                    .name(account.getUsername())
                    .type(trade.getType())
                    .gender(account.getGender())
                    .phone(account.getPhone())
                    .qrCode(wxGroup.getQrCode())
                    .build();
        } else {
            return ItineraryCardVo.builder()
                    .skiResortName(snowfield.getName())
                    .location(snowfield.getLocation())
                    .skiResortPhone(snowfield.getPhone())
                    .hotel(Optional.ofNullable(roomTicket)
                            .map(RoomTicket::getDescription)
                            .orElse(""))
                    .name(account.getUsername())
                    .type(trade.getType())
                    .gender(account.getGender())
                    .phone(account.getPhone())
                    .qrCode(wxGroup.getQrCode())
                    .build();
        }


    }

    @Override
    public ItineraryListVo getItineraryList() {
        Long userId = LoginUser.getLoginUserId();
        List<Trade> trades = tradeMapperService.getReadyByUserId(userId);
        trades.sort(Comparator.comparing(Trade::getCreateTime).reversed());
        List<ItineraryVo> itineraryVos = new ArrayList<>();
        List<ItineraryVo> leaderItineraryVos = new ArrayList<>();
        for (Trade trade : trades) {
            Activity activity = activityMapperService.get(trade.getActivityId());
            ActivityTemplate activityTemplate = activityTemplateMapperService.get(trade.getActivityTemplateId());
            if (!(LocalDate.now().isAfter(activity.getActivityEndDate()))) {

                BusMove busMove = new BusMove();
                if (trade.getBusMoveId() != null) {
                    busMove = busMoveMapperService.get(trade.getBusMoveId());
                }
                ItineraryVo vo = ItineraryVo.builder()
                        .id(trade.getId())
                        .type(trade.getType())
                        .name(activityTemplate.getName())
                        .beginDate(DateTimeFormatter.ofPattern("yyyy年MM月dd日").format(activity.getActivityBeginDate()))
                        .ticketIntro(Optional.ofNullable(productManager.getProductById(trade.getProductId()))
                                .map(Product::getDescription)
                                .orElse(""))
                        .busMoveTime(busMove.getTime() == null ? "" : busMove.getTime().format(DateTimeFormatter.ofPattern("HH:mm a")))
                        .position(trade.getStationId() == null || trade.getStationId() == -1 ? "" : stationInfoMapperService.get(stationMapperService.get(trade.getStationId()).getStationInfoId()).getPosition())
                        .build();
                if (trade.getStatus() == TradeStatus.LEADER.getCode()) {
                    vo.setItineraryStatus(getLeaderItineraryStatus(trade, activity.getActivityBeginDate()).getCode());
                    leaderItineraryVos.add(vo);
                } else {
                    vo.setItineraryStatus(getUserItineraryStatus(trade).getCode());
                    itineraryVos.add(vo);
                }
            }
        }
        return new ItineraryListVo(itineraryVos, leaderItineraryVos);
    }

    @Override
    public ItineraryDetailVo getItineraryDetail(Long tradeId) {

        Trade trade = tradeMapperService.get(tradeId);
        ProductType type = trade.getType();
        Activity activity = activityMapperService.get(trade.getActivityId());
        ActivityTemplate activityTemplate = activityTemplateMapperService.get(trade.getActivityTemplateId());
        Snowfield snowfield = snowfieldMapperService.get(trade.getSnowfieldId());

        Long stationId = trade.getStationId();
        boolean busMoveAvailable = (stationId == null || stationId == -1);

        Station station = Station.builder().build();
        if (stationId != null && stationId != -1) {
            station = stationMapperService.get(stationId);
        }

        StationInfo stationInfo = new StationInfo();
        if (station.getStationInfoId() != null) {
            stationInfo = stationInfoMapperService.get(station.getStationInfoId());
        }

        Bus bus = new Bus();
        BusMove busMove = new BusMove();
        Long busId = trade.getBusId();
        Long busMoveId = trade.getBusMoveId();
        if (busId != null) {
            bus = busMapperService.get(busId);
        }
        if (busMoveId != null) {
            busMove = busMoveMapperService.get(busMoveId);
        }
        String hotel = "";
        String productIntro = "";
        if (trade.getProductId() != null) {
            Product product = productManager.getProductById(trade.getProductId());
            productIntro = product.getDescription();
            RoomTicket roomTicket = getRoomTicketWithTradeId(tradeId);
            if (roomTicket != null) {
                hotel = roomTicket.getDescription();
            }
        }

        String roomCode = null;
        if (trade.getRoomId() != null) {
            Room room = roomMapperService.get(trade.getRoomId());
            if (room != null) {
                roomCode = room.getCode();
            }
        }
        // 获取Leader信息
        ItineraryDetailVo.LeaderInfo leaderInfo = getLeaderInfo(bus.getLeaderId());

        // 构建基础的Builder
        ItineraryDetailVo.ItineraryDetailVoBuilder builder = createBaseBuilder(
                activityTemplate, snowfield, activity, bus, busMove, stationInfo, productIntro, hotel, roomCode, type, stationId
        );

        // 根据用户身份添加特定字段
        if (trade.getStatus() == TradeStatus.LEADER.getCode()) {
            return buildLeaderItinerary(builder, trade, activity, activityTemplate);
        } else {
            return buildUserItinerary(builder, trade, leaderInfo, busMoveAvailable, wxGroupMapperService);
        }

    }

    // 创建基础Builder
    private ItineraryDetailVo.ItineraryDetailVoBuilder createBaseBuilder(
            ActivityTemplate activityTemplate,
            Snowfield snowfield,
            Activity activity,
            Bus bus,
            BusMove busMove,
            StationInfo stationInfo,
            String productIntro,
            String hotel,
            String roomCode,
            ProductType type,
            Long stationId
    ) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String busMoveTimeStr = Optional.ofNullable(busMove)
                .map(BusMove::getTime)
                .map(time -> time.format(formatter))
                .orElse("");

        String arrivalTimeStr = Optional.ofNullable(bus)
                .map(Bus::getArrivalTime)
                .map(time -> time.format(formatter))
                .orElse("");
        String returnTimeStr = Optional.ofNullable(activity.getReturnTime())
                .map(time -> time.format(formatter))
                .orElse("");
        Doc schedule = docUtils.getDocByActivityTemplateIdAndType(activityTemplate.getId(), DocType.SCHEDULE.getCode());
        Doc attention = docUtils.getDocByActivityTemplateIdAndType(activityTemplate.getId(), DocType.ATTENTION.getCode());
        return ItineraryDetailVo.builder()
                .name(activityTemplate.getName())
                .type(type)
                .skiResortLocation(snowfield.getLocation())
                .beginDate(DateTimeFormatter.ofPattern("yyyy年MM月dd日").format(activity.getActivityBeginDate()))
                .busNumber(Optional.ofNullable(bus).map(Bus::getCarNumber).orElse(null))
                .busName(Optional.ofNullable(bus).map(Bus::getName).orElse(null))
                .toArea(areaMapperService.get(snowfield.getAreaId()).getAreaName())
                .busMoveTime(busMoveTimeStr)
                .arrivalTime(arrivalTimeStr)
                .school(stationInfo.getSchool())
                .campus(stationInfo.getCampus())
                .location(stationInfo.getLocation())
                .arrivalLocation(snowfield.getName())
                .returnTime(returnTimeStr)
                .returnLocation(activity.getReturnLocation())
                .schedule(schedule)
                .attention(attention)
                .hotel(Optional.ofNullable(hotel).orElse(""))
                .roomCode(roomCode)
                .stationId(stationId)
                .ticketIntro(Optional.ofNullable(productIntro).orElse(""));
    }

    // 构建普通用户行程
    private ItineraryDetailVo buildUserItinerary(
            ItineraryDetailVo.ItineraryDetailVoBuilder builder,
            Trade trade,
            ItineraryDetailVo.LeaderInfo leaderInfo,
            boolean busMoveAvailable,
            WxGroupMapperService wxGroupMapperService
    ) {
        return builder
                .qrCode(wxGroupMapperService.get(trade.getWxgroupId()).getQrCode())
                .leaderInfo(leaderInfo)
                .busMoveAvailable(busMoveAvailable)
                .itineraryStatus(this.getUserItineraryStatus(trade).getCode())
                .build();
    }

    // 构建领队行程
    private ItineraryDetailVo buildLeaderItinerary(
            ItineraryDetailVo.ItineraryDetailVoBuilder builder,
            Trade trade,
            Activity activity,
            ActivityTemplate activityTemplate) {
        Long busId = trade.getBusId();
        Doc leaderNotice = docUtils.getDocByActivityTemplateIdAndType(activityTemplate.getId(), DocType.LEADER_NOTICE.getCode());
        return builder
                .leaderNotice(leaderNotice)
                .itineraryStatus(this.getLeaderItineraryStatus(trade, activity.getActivityBeginDate()).getCode())
                .busId(busId)
                .build();
    }

    /**
     * 获取用户行程状态
     * -1--报名截止，且上车点无效，需要调用获取替换的上车点、
     * 0--还未锁票
     * 1--去程未上车且不在活动当天
     * 10--选车牌专属状态
     * 2--去程未上车且在活动当天
     * 3--去程已上车，进入验票阶段，显示验票页面
     * 4--去程已上车，已验票，教程第一步
     * 5--去程已上车，已验票，教程在进行
     * 6--教程完成，但领队未结束滑雪，不给用户展示归还雪具的指引
     * 7--教程完成，且领队点击了结束滑雪，给他们显示20那个指引，也就是归还雪具
     * 8--雪具归还
     * 9--返程已上车，行程完成
     * @param trade 交易信息
     * @return 行程状态
     */
    private ItineraryStatus getUserItineraryStatus(Trade trade) {
        Long tutorialId = trade.getTutorialId();
        Activity activity = activityMapperService.get(trade.getActivityId());
        LocalDateTime lockDdlDate = activity.getLockDdlDate();
        LocalDate activityBeginDate = activity.getActivityBeginDate();

        // 单雪票的判定
        // 0--还未锁票
        // 3--已锁票，显示验票页面
        // 4--已验票，教程第一步
        // 5--已验票，教程在进行
        // 7--教程完成，显示归还雪具
        // 8--雪具归还，行程完成
        if (Objects.equals(trade.getType(), ProductType.SKI)) {
            if (trade.getStatus() != TradeStatus.LOCKED.getCode() && trade.getStatus() != TradeStatus.RETURN.getCode()) {
                // 状态不是锁票或者已归还
                return ItineraryStatus.UNLOCKED_TRADE;
            }
            if (trade.getTicketCheck()) {
                // 已验票
                if (tutorialEdgeMapperService.prev(tutorialId).isEmpty()) {
                    // 教程第一步
                    return ItineraryStatus.TICKET_CHECKED;
                }
                else if (tutorialEdgeMapperService.next(tutorialId).isEmpty()) {
                    // 教程已到最后一步
                    if (trade.getStatus() == TradeStatus.RETURN.getCode()) {
                        // 已归还雪具
                        return ItineraryStatus.RETURNED_BOARDED;
                    } else {
                        // 未归还雪具
                        return ItineraryStatus.RETURNED;
                    }
                } else {
                    // 教程未到最后一步
                    return ItineraryStatus.GUIDING;
                }
            } else {
                // 未验票
                return ItineraryStatus.TICKET_UNCHECK;
            }

        }

        // 单车票的判定
        // -1--报名截止，且上车点无效，需要调用获取替换的上车点
        // 0--还未锁票
        // 10--选车牌专属状态
        // 1--未上车，不在活动当天
        // 2--未上车，活动当天
        // 9--已上车，行程完成
        if (Objects.equals(trade.getType(), ProductType.BUS)) {
            // 上车点无效
            if (trade.getStationId() == null || trade.getStationId() == -1) {
                return ItineraryStatus.INVALID_STATION;
            }
            if (trade.getStatus() != TradeStatus.LOCKED.getCode() && trade.getStatus() != TradeStatus.RETURN.getCode()) {
                // 状态不是锁票或者已归还
                return ItineraryStatus.UNLOCKED_TRADE;
            }
            if (trade.getGoBoarded()) {
                // 已上车
                return ItineraryStatus.RETURNED_BOARDED;
            } else {
                // 未上车
                if (LocalDate.now().isBefore(activityBeginDate)) {
                    // 未到活动当天
                    if (activity.getStatus() == ActivityStatus.LOCKED.getCode() || !LocalDateTime.now().isBefore(lockDdlDate)) {
                        // 已到lockDdlDate，可以选车了
                        return ItineraryStatus.CHOOSE_BUS;
                    }
                    return ItineraryStatus.BERORE_TRIP;
                } else {
                    // 已到活动当天
                    return ItineraryStatus.NEAR_TRIP;
                }
            }
        }


        // 上车点无效
        if (trade.getStationId() == null || trade.getStationId() == -1) {
            return ItineraryStatus.INVALID_STATION;
        }
        if (trade.getStatus() != TradeStatus.LOCKED.getCode() && trade.getStatus() != TradeStatus.RETURN.getCode()) {
            return ItineraryStatus.UNLOCKED_TRADE;
        }
        // 返程
        boolean returnBoarded = trade.getReturnBoarded();
        // 去程
        boolean goBoarded = trade.getGoBoarded();
        // 验票
        boolean ticketCheck = trade.getTicketCheck();

        if (!goBoarded) {
            // 去程未上车
            if (LocalDate.now().isBefore(activityBeginDate)) {
                if (activity.getStatus() == ActivityStatus.LOCKED.getCode() || !LocalDateTime.now().isBefore(lockDdlDate)) {
                    // 已到lockDdlDate，可以选车了
                    return ItineraryStatus.CHOOSE_BUS;
                }
                return ItineraryStatus.BERORE_TRIP;
            } else {
                return ItineraryStatus.NEAR_TRIP;
            }
        } else {
            // 去程已上车
            if (!ticketCheck) {
                // 未验票
                return ItineraryStatus.TICKET_UNCHECK;
            } else {
                // 已验票
                if (tutorialEdgeMapperService.prev(tutorialId).isEmpty()) {
                    // 教程第一步
                    return ItineraryStatus.TICKET_CHECKED;
                } else if (tutorialEdgeMapperService.next(tutorialId).isEmpty()) {
                    Integer status = busMapperService.get(trade.getBusId()).getStatus(); // 领队所决定的当前大巴上乘客的旅途状态
                    // 教程最后一步
                    if (status < BusStatus.ski.getCode()) {
                        // 领队未结束滑雪
                        return ItineraryStatus.GUIDE_FINISH;
                    } else {
                        // 领队结束滑雪
                        if (trade.getStatus() == TradeStatus.RETURN.getCode()) {
                            // 归还了雪具
                            if (returnBoarded) {
                                // 返程已上车
                                return ItineraryStatus.RETURNED_BOARDED;
                            } else {
                                return ItineraryStatus.RETURNED;
                            }
                        } else {
                            // 未归还雪具
                            return ItineraryStatus.GUIDE_FINISH_SHOW;
                        }
                    }
                } else {
                    // 教程进行中
                    return ItineraryStatus.GUIDING;
                }
            }
        }
    }

    /**
     * 0--未到行程第一天。不显示上车情况
     * 1--有去程未上车的人。显示去程上车情况，以及验票按钮(万一有人没来，人不齐也能验票)
     * 2--去程全部上车。不显示去程上车情况，显示验票按钮和验票人数情况
     * 3--点击了结束滑雪，开始显示返程上车情况
     * 4--领队点完了车上的人，大巴车启程返回
     * 5--车上所有人都送回，领队点行程结束
     * @param trade 交易信息
     * @return 行程状态
     */
    private LeaderItineraryStatus getLeaderItineraryStatus(Trade trade, LocalDate beginDate) {
        Long busId = trade.getBusId();
        Bus bus = busMapperService.get(busId);
        Long activityId = trade.getActivityId();
        if (busId == null || activityId == null) {
            return LeaderItineraryStatus.BEFORE_TRIP;
        }
        // 获取去程未上车的订单列表
//        List<Trade> notGoBoardedList = tradeMapperService.getNotGoBoardedList(activityId, busId);
        if (LocalDate.now().isBefore(beginDate)) {
            return LeaderItineraryStatus.BEFORE_TRIP;
        } else if (bus.getStatus() == BusStatus.returned.getCode()) {
            return LeaderItineraryStatus.RETURN_ABOARD;
        } else if (bus.getStatus() == BusStatus.finish.getCode()){
            return LeaderItineraryStatus.RETURN_FINISH;
        } else if (bus.getStatus() == BusStatus.ski.getCode()) {
            return LeaderItineraryStatus.SKI_END;
        } else if (bus.getStatus() == BusStatus.arrive.getCode()) {
            return LeaderItineraryStatus.ALL_GO_ABOARD;
        } else {
            return LeaderItineraryStatus.NOT_ALL_GO_ABOARD;
        }
    }

    /**
     * 获取该行程的leader信息
     * @param leaderId 领队id
     * @return 返回的leader信息，若没有领队则返回空的leader信息
     */
    private ItineraryDetailVo.LeaderInfo getLeaderInfo(Long leaderId) {
        if (leaderId == null) {
            return null;
        }
        Account account = accountMapperService.getById(leaderId);
        if (account == null) {
            return null;
        }
        return new ItineraryDetailVo.LeaderInfo(
                leaderId,
                account.getUsername(),
                account.getPhone(),
                account.getProfile(),
                account.getIntro()
        );
    }

    @Override
    public List<ItineraryDetailVo.StationInfo> leaderGetItineraryStationInfoList(Long tradeId) {
        Trade trade = tradeMapperService.get(tradeId);
        Bus bus = busMapperService.get(trade.getBusId());

        if (bus == null) {
            throw new ByrSkiException(ReturnCode.BUS_NOT_EXIST);
        }

        List<BusMove> busMoves = busMoveMapperService.getByBusId(bus.getId());
        if (busMoves.isEmpty()) {
            throw new ByrSkiException(ReturnCode.BUS_MOVE_NOT_EXIST);
        }
        return busMoves.stream()
                .map(BusMove::getStationId)
                .filter(Objects::nonNull)
                .map(stationMapperService::get)
                .map(station -> {
                    StationInfo stationInfo = stationInfoMapperService.get(station.getStationInfoId());
                    BusMove busMove = busMoveMapperService.getByBusIdAndStationId(bus.getId(), station.getId());
                    return new ItineraryDetailVo.StationInfo(
                            station.getId(),
                            stationInfo.getSchool(),
                            stationInfo.getCampus(),
                            stationInfo.getLocation(),
                            busMove.getGoFinished(),
                            busMove.getTime()
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * 取消订单，提供给外部接口调用，其中包含了Authorization的校验
     * 支持单用户和多用户订单的取消
     * @param tradeId 订单id
     * @return 退款结果
     */
    @Override
    public Refund cancelTrade(Long tradeId) {
        Trade trade = tradeMapperService.get(tradeId);
        
        // 检查是否为多用户订单（通过outTradeNo查询所有相关Trade记录）
        List<Trade> relatedTrades = tradeMapperService.getAllByOutTradeNo(trade.getOutTradeNo());
        
        if (relatedTrades.size() > 1) {
            // 多用户订单：取消所有相关订单
            log.info("检测到多用户订单，开始取消订单组，订单号: {}, 订单数量: {}", 
                    trade.getOutTradeNo(), relatedTrades.size());
            
            Refund firstRefund = null;
            for (Trade relatedTrade : relatedTrades) {
                try {
                    Refund refund = payUtils.cancelTrade(relatedTrade);
                    if (firstRefund == null) {
                        firstRefund = refund;
                    }
                    log.info("取消多用户订单中的订单成功，订单ID: {}", relatedTrade.getId());
                } catch (Exception e) {
                    log.error("取消多用户订单中的订单失败，订单ID: {}", relatedTrade.getId(), e);
                    // 单个订单取消失败不应该影响其他订单的取消
                }
            }
            
            log.info("多用户订单取消完成，订单号: {}, 成功取消订单数: {}", 
                    trade.getOutTradeNo(), relatedTrades.size());
            
            return firstRefund; // 返回第一个订单的退款信息
        } else {
            // 单用户订单：正常取消
            return payUtils.cancelTrade(tradeId);
        }
    }

    /**
     * 通过订单号取消订单，提供给定时任务调用，其中不包含Authorization的校验
     * @param outTradeNo 订单号
     */
    @Override
    public void cancelExpiredTrade(String outTradeNo) {
        Trade trade = tradeMapperService.getByOutTradeNoWithoutAuth(outTradeNo);
        if (trade.getStatus() == TradeStatus.UNPAID.getCode()) {
            payUtils.cancelTrade(trade);
        }
    }

    /**
     * 领队获取局部行程上车人数，需要筛选同一activity的trade下相同busId和stationId的trade
     * @param tradeId 领队订单id
     * @param route 行程类型，go代表去程，return代表返程
     * @param stationId 站点id，若为null则代表查询全部站点
     * @return 返回的上车人数
     */
    @Override
    public HeadCountVo getHeadCount(Long tradeId, String route, Long stationId) {
        Trade trade = tradeMapperService.get(tradeId);
        if (trade.getStatus() != TradeStatus.LEADER.getCode()) {
            throw new ByrSkiException(ReturnCode.FORBIDDEN);
        }
        Long activityId = trade.getActivityId();
        Long busId = trade.getBusId();
        if (route.equals("go")) {
            if (stationId == -1) {
                List<Trade> notGoBoardedList = tradeMapperService.getNotGoBoardedList(activityId, busId);
                return getHeadCountVo(route, activityId, busId, notGoBoardedList);
            } else {
                List<Trade> notGoBoardedListByStationId = tradeMapperService.getNotGoBoardedListByStationId(activityId, stationId, busId);
                List<Trade> totalListByStationId = tradeMapperService.getTotalListByStationId(activityId, stationId, busId);
                int thisStationMissingPassengerCount = notGoBoardedListByStationId.size();
                int thisStationTotalPassengerCount = totalListByStationId.size();
                int missingPassengerCount = tradeMapperService.getNotGoBoardedList(activityId, busId).size();
                int totalPassengerCount = tradeMapperService.getTotalList(activityId, busId).size();

                return HeadCountVo.builder()
                        .thisStationMissingPassengerCount(thisStationMissingPassengerCount)
                        .thisStationTotalPassengerCount(thisStationTotalPassengerCount)
                        .missingPassengerCount(missingPassengerCount)
                        .boardedPassengerCount(totalPassengerCount - missingPassengerCount)
                        .totalPassengerCount(totalPassengerCount)
                        .unboardedPassengerList(fromTradeList(notGoBoardedListByStationId, route))
                        .totalPassengerList(fromTradeList(totalListByStationId, route))
                        .build();
            }
        } else if (route.equals("return")) {
            List<Trade> notReturnBoardedList = tradeMapperService.getNotReturnBoardedList(activityId, busId);
            return getHeadCountVo(route, activityId, busId, notReturnBoardedList);
        } else {
            throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION);
        }
    }

    @Override
    public Boolean getTicketChecked(Long tradeId) {
        Trade trade = tradeMapperService.get(tradeId);
        return trade.getTicketCheck();
    }

    @Override
    public void updateBusStatus(BusStatusVo busStatusVo) {
        infoUtils.checkLeader();
        Long busId = busStatusVo.getId();
        Bus bus = busMapperService.get(busId);
        bus.setStatus(BusStatus.valueOf(busStatusVo.getType()).getCode());
        busMapperService.updateById(bus);
    }

    @Override
    public void goFinished(Long busId, Long stationId) {
        infoUtils.checkLeader();
        Long busMoveId = busMoveMapperService.getByBusIdAndStationId(busId, stationId).getId();
        busMoveMapperService.setGoFinished(busMoveId);
    }

    @Override
    public TicketCheckHeadCountVo getCheckedPassenger(Long tradeId) {
        Trade trade = tradeMapperService.get(tradeId);
        if (trade.getStatus() != TradeStatus.LEADER.getCode()) {
            throw new ByrSkiException(ReturnCode.FORBIDDEN);
        }
        Long activityId = trade.getActivityId();
        Long busId = trade.getBusId();
        List<Trade> totalTrades = tradeMapperService.getTotalList(activityId, busId);
        List<Trade> unCheckedTrades = tradeMapperService.getNotCheckedList(activityId, busId);
        Integer uncheckedPassengerNum = unCheckedTrades.size();
        Integer totalPassengerNum = totalTrades.size();
        Integer checkedPassengerNum = totalPassengerNum - uncheckedPassengerNum;
        return TicketCheckHeadCountVo.builder()
                .uncheckedPassengerNum(uncheckedPassengerNum)
                .checkedPassengerNum(checkedPassengerNum)
                .totalPassengerNum(totalPassengerNum)
                .uncheckedPassengers(fromTradeToEntityList(unCheckedTrades))
                .totalPassengers(fromTradeToEntityList((totalTrades)))
                .build();
    }

    /**
     * 检查订单的分房情况，如果订单有房间id，那么返回房间信息，状态为0
     * 如果票型不包含住宿，返回状态为1
     * 如果票型包含住宿，但是当前不能选房，返回状态为2
     * 如果票型包含住宿，且可以选房，返回状态为3
     * @param tradeId 订单id
     * @return 房间信息
     */
    @Override
    public CheckRoomVo checkRoomAlloc(Long tradeId) {
        Trade trade = tradeMapperService.get(tradeId);
        int status = 2;
        // 未购买拼房产品
        if (!tradeTicketsMapperService.containRoom(tradeId)) {
            return CheckRoomVo.builder()
                    .status(0)
                    .build();
        }
        if (trade.getGoBoarded()) {
            status = 3;
        } else if (trade.getStatus() != TradeStatus.LOCKED.getCode() && trade.getStatus() != TradeStatus.LEADER.getCode()) {
            status = 1;
        }

        if (trade.getRoomId() != null) {
            Room room = roomMapperService.get(trade.getRoomId());
            List<Account> accounts = new ArrayList<>();
            tradeMapperService.getByRoomId(trade.getRoomId()).forEach(trade1 -> {
                Long userId = trade1.getUserId();
                Account account = accountMapperService.getById(userId);
                accounts.add(account);
            });

            return CheckRoomVo.builder()
                    .room(room)
                    .members(accounts)
                    .status(status)
                    .build();
        }
        return CheckRoomVo.builder()
                .status(status)
                .build();
    }

    @Override
    public Room createRoom(Long tradeId) {
        Trade trade = tradeMapperService.get(tradeId);

        // 已经分配房间，不能建房
        if (trade.getRoomId() != null) {
            throw new ByrSkiException(ReturnCode.ROOM_ALREADY_ALLOCATED);
        }
        // 门票不支持拼房
        if (!tradeTicketsMapperService.containRoom(tradeId)) {
            throw new ByrSkiException(ReturnCode.HOTEL_TYPE_NOT_MATCH);
        }
        // 订单状态不对或者已经上车，不能建房
        if ((trade.getStatus() != TradeStatus.LOCKED.getCode() && trade.getStatus() != TradeStatus.LEADER.getCode()) || trade.getGoBoarded()) {
            throw new ByrSkiException(ReturnCode.ROOM_ALLOCATE_TIME_EXCEED);
        }
        String code = tradeUtils.generateNumericVerificationCode();
        RoomTicket roomTicket = getRoomTicketWithTradeId(tradeId);
        Room room = Room.builder()
                .name(roomTicket.getDescription())
                .maxPeopleNum(roomTicket.getMaxPeopleNum())
                .coed(roomTicket.getCoed())
                .peopleNum(1)
                .tradeId(tradeId)
                .ownerId(LoginUser.getLoginUserId())
                .ownerName(accountMapperService.getById(LoginUser.getLoginUserId()).getUsername())
                .code(code)
                .activityId(trade.getActivityId())
                .ticketId(roomTicket.getTicketId())
                .build();
        if (roomMapperService.save(room)) {
            Room byCode = roomMapperService.getByCode(code);
            if (tradeMapperService.setRoomId(tradeId, byCode.getId())) {
                return byCode;
            } else {
                log.error("订单 {} 设置房间id失败", tradeId);
                throw new ByrSkiException(ReturnCode.DATABASE_ERROR);
            }
        }

        throw new ByrSkiException(ReturnCode.FAIL);
    }

    private RoomTicket getRoomTicketWithTradeId(Long tradeId) {
        Long roomTicketId = tradeTicketsMapperService.getRoomTicketIdByTradeId(tradeId);
        RoomTicket roomTicket = null;
        if (roomTicketId != null) {
            roomTicket = (RoomTicket) ticketStrategyFactory.getStrategy(TicketType.ROOM).getTicketById(roomTicketId);
        }
        return roomTicket;
    }

    @Override
    public Room queryRoom(String code) {
        Room room = roomMapperService.getByCode(code);
        if (room == null) {
            throw new ByrSkiException(ReturnCode.ROOM_NOT_EXIST);
        }
        return room;
    }

    @Override
    public Boolean joinRoom(JoinRoomVo joinRoomVo) {
        Room room = roomMapperService.getByCode(joinRoomVo.getCode());
        if (room == null) {
            throw new ByrSkiException(ReturnCode.ROOM_NOT_EXIST);
        }
        Trade myTrade = tradeMapperService.get(joinRoomVo.getTradeId());
        Long myRoomTicketId = tradeTicketsMapperService.getRoomTicketIdByTradeId(joinRoomVo.getTradeId());
        if (!Objects.equals(myRoomTicketId, room.getTicketId())) {
            throw new ByrSkiException(ReturnCode.ROOM_NOT_MATCH);
        }

        boolean sameGender = Objects.equals(accountMapperService.getById(room.getOwnerId()).getGender(), accountMapperService.getById(LoginUser.getLoginUserId()).getGender());
        if (!sameGender && !room.getCoed()) {
            throw new ByrSkiException(ReturnCode.ROOM_GENDER_INFLICTION);
        }
        if (room.getPeopleNum() >= room.getMaxPeopleNum()) {
            throw new ByrSkiException(ReturnCode.ROOM_FULL);
        }

        if (myTrade.getRoomId() != null) {
            throw new ByrSkiException(ReturnCode.ROOM_ALREADY_ALLOCATED);
        }
        myTrade.setRoomId(room.getId());
        if (tradeMapperService.updateById(myTrade)) {
            room.setPeopleNum(room.getPeopleNum() + 1);
            return roomMapperService.updateById(room);
        }
        throw new ByrSkiException(ReturnCode.FAIL);
    }


    @Override
    public Boolean cancelRoom(Long tradeId) {
        Trade trade = tradeMapperService.get(tradeId);
        if (trade.getRoomId() == null) {
            throw new ByrSkiException(ReturnCode.ROOM_NOT_EXIST);
        }
        Room room = roomMapperService.get(trade.getRoomId());
        if (room == null) {
            throw new ByrSkiException(ReturnCode.ROOM_NOT_EXIST);
        }

        // 用户为房主，从room表中删除房间信息
        if (Objects.equals(room.getOwnerId(), LoginUser.getLoginUserId())) {
            tradeMapperService.cancelRoom(room.getId());
            roomMapperService.deleteRoom(room.getId());
        // 用户不为房主，仅减少房间人数
        } else {
            tradeMapperService.cleanRoom(trade.getId());
            roomMapperService.decreaseMember(room.getId(), room.getPeopleNum());
        }
        return true;
    }




    @Override
    public void upgradeItinerary(Long tradeId) {
        Trade trade = tradeMapperService.getWithoutOwner(tradeId);
        if (trade.getStatus() == TradeStatus.LEADER.getCode()) {
            throw new ByrSkiException(ReturnCode.TRADE_LEADER);
        }
        // 更新订单为领队订单
        trade.setStatus(TradeStatus.LEADER.getCode());
        tradeMapperService.updateById(trade);
        // 更新用户为领队用户
        Long userId = trade.getUserId();
        Account account = accountMapperService.getById(userId);
        if (!Objects.equals(account.getIdentity(), UserIdentity.LEADER.getCode())) {
            account.setIdentity(UserIdentity.LEADER.getCode());
            accountMapperService.updateById(account);
        }
    }

    @Override
    public void chooseSki(SkiChoiceVo skiChoiceVo) {
        Trade trade = tradeMapperService.get(skiChoiceVo.getTradeId());
        trade.setSkiChoice(SkiChoice.fromCode(skiChoiceVo.getSkiChoice()).getCode());
        tradeMapperService.updateById(trade);
    }

    @Override
    public void returnItem(Long tradeId) {
        Trade trade = tradeMapperService.get(tradeId);
        trade.setStatus(TradeStatus.RETURN.getCode());
        tradeMapperService.updateById(trade);
    }

    private List<TicketCheckHeadCountVo.Entity> fromTradeToEntityList(List<Trade> tradeList) {
        return tradeList.stream().map(trade -> {
            Account account = accountMapperService.getById(trade.getUserId());
            StationInfo stationInfo = stationInfoMapperService.get(stationMapperService.get(trade.getStationId()).getStationInfoId());
            return TicketCheckHeadCountVo.Entity.builder()
                    .name(account.getUsername())
                    .gender(account.getGender())
                    .phone(account.getPhone())
                    .position(stationInfo.getPosition())
                    .checked(trade.getTicketCheck())
                    .build();
        }).collect(Collectors.toList());
    }

    private HeadCountVo getHeadCountVo(String route, Long activityId, Long busId, List<Trade> notGoBoardedList) {
        List<Trade> totalList = tradeMapperService.getTotalList(activityId, busId);

        int missingPassengerCount = notGoBoardedList.size();
        int totalPassengerCount = totalList.size();

        return HeadCountVo.builder()
                .missingPassengerCount(missingPassengerCount)
                .boardedPassengerCount(totalPassengerCount - missingPassengerCount)
                .totalPassengerCount(totalPassengerCount)
                .unboardedPassengerList(fromTradeList(notGoBoardedList, route))
                .totalPassengerList(fromTradeList(totalList, route))
            .build();
    }

    private List<Passenger> fromTradeList(List<Trade> tradeList, String route) {
         Map<String, Function<Trade, Boolean>> functionMap = new HashMap<>(
                Map.of(
                        "go", Trade::getGoBoarded,
                        "return", Trade::getReturnBoarded
                )
        );
        return tradeList.stream().map(trade -> {
            Account account = accountMapperService.getById(trade.getUserId());
            StationInfo stationInfo = stationInfoMapperService.get(stationMapperService.get(trade.getStationId()).getStationInfoId());
            return Passenger.builder()
                    .name(account.getUsername())
                    .gender(account.getGender())
                    .phone(account.getPhone())
                    .school(stationInfo.getSchool())
                    .campus(stationInfo.getCampus())
                    .location(stationInfo.getLocation())
                    .boarded(functionMap.get(route).apply(trade))
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public void setDepartureBoarded(Long tradeId) {
        tradeMapperService.setDepartureBoarded(tradeId);
    }

    @Override
    public void setReturnBoarded(Long tradeId) {
        Trade trade = tradeMapperService.get(tradeId);
        LocalDateTime returnTime = activityMapperService.get(trade.getActivityId()).getReturnTime();
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(returnTime.minusMinutes(30))) {
            throw new ByrSkiException(ReturnCode.RETURN_TIME_NOT_REACHED);
        }
        tradeMapperService.setReturnBoarded(tradeId);
    }

    /**
     * 获取教程
     * @param tradeId 交易id
     * @return 教程
     */
    @Override
    public Tutorial getTutorial(Long tradeId) {
        return tutorialMapperService.get(tradeMapperService.get(tradeId).getTutorialId());
    }

    /**
     * 获取下一步教程，优先级：status为0 > status与本步骤相等 > status与trade的skiChoice相等
     * @param tradeId 交易id
     * @return 下一步教程
     */
    @Override
    public Tutorial nextTutorial(Long tradeId) {
        Trade trade = tradeMapperService.get(tradeId);
        Integer tradeSkiChoice = trade.getSkiChoice();

        Graph<Tutorial, DefaultEdge> dag = TutorialUtils.getDAG();

        Tutorial currentTutorial = tutorialMapperService.get(trade.getTutorialId());

        List<Tutorial> nextTutorials = getNextTutorials(currentTutorial, dag);

        // Sort the next tutorials by skiChoice priority
        nextTutorials.sort(
                Comparator.comparingInt((Tutorial t) -> t.getSkiChoice() == 0 ? 0 : 1)
                        .thenComparingInt(t -> t.getSkiChoice().equals(tradeSkiChoice) ? 0 : 1)
                        .thenComparingInt(t -> t.getSkiChoice().equals(currentTutorial.getSkiChoice()) ? 0 : 1)
        );

        trade.setTutorialId(nextTutorials.get(0).getId());
        tradeMapperService.updateById(trade);
        // Return the first tutorial based on the sorted priority
        return nextTutorials.isEmpty() ? null : nextTutorials.get(0);
    }

    private List<Tutorial> getNextTutorials(Tutorial currentTutorial, Graph<Tutorial, DefaultEdge> dag) {
        return dag.outgoingEdgesOf(currentTutorial).stream()
                .map(dag::getEdgeTarget)
                .collect(Collectors.toList());
    }


    @Override
    public Tutorial skipTutorial(Long tradeId) {
        Trade trade = tradeMapperService.get(tradeId);
        Long tutorialId = trade.getTutorialId();
        Graph<Tutorial, DefaultEdge> dag = TutorialUtils.getDAG();
        Tutorial currentTutorial = tutorialMapperService.get(tutorialId);

        Tutorial resultTutorial;

        // 如果当前教程的入度为 0，直接返回一个出度为 0 的教程的前一个节点
        if (dag.inDegreeOf(currentTutorial) == 0) {
            resultTutorial = findPreviousTutorialWithOutDegreeZero(dag);
        } else {
            // 继续遍历图，从当前教程开始查找
            if (dag.outDegreeOf(currentTutorial) == 0) {
                return null; // 如果当前教程的出度为 0，直接返回 null
            }
            List<Tutorial> nextTutorials = dag.outgoingEdgesOf(currentTutorial).stream()
                    .map(dag::getEdgeTarget)
                    .toList();
            if (nextTutorials.isEmpty()) {
                return null;
            }

            // 从第一个后续节点开始查找
            resultTutorial = findNextPeriodTutorial(nextTutorials.get(0), dag, trade.getSkiChoice());
        }

        // 如果找到了有效的教程，更新 trade
        if (resultTutorial != null) {
            trade.setTutorialId(resultTutorial.getId());
            tradeMapperService.updateById(trade);
        }

        return resultTutorial;
    }

    private Tutorial findNextPeriodTutorial(Tutorial currentTutorial, Graph<Tutorial, DefaultEdge> dag, Integer tradeSkiChoice) {
        // 获取当前教程的所有后续教程
        List<Tutorial> nextTutorials = dag.outgoingEdgesOf(currentTutorial).stream()
                .map(dag::getEdgeTarget)
                .toList();

        if (nextTutorials.isEmpty()) {
            return null; // 没有找到下一个教程
        }

        if (nextTutorials.size() == 1) {
            Tutorial next = nextTutorials.get(0);
            if (next.getSkiChoice().equals(currentTutorial.getSkiChoice())) {
                // 如果 skiChoice 相同，继续查找下一个教程
                return findNextPeriodTutorial(next, dag, tradeSkiChoice);
            } else {
                // 如果 skiChoice 不同，返回当前的下一个教程
                return next;
            }
        } else {
            // 有多个下一个教程，按照优先级排序
            return nextTutorials.stream()
                    .min(Comparator
                            .comparingInt((Tutorial t) -> t.getSkiChoice() == 0 ? 0 : 1) // 优先级1：skiChoice == 0
                            .thenComparingInt(t -> t.getSkiChoice().equals(tradeSkiChoice) ? 0 : 1) // 优先级2：skiChoice == tradeSkiChoice
                            .thenComparingInt(t -> t.getSkiChoice().equals(currentTutorial.getSkiChoice()) ? 0 : 1) // 优先级3：skiChoice == currentTutorial 的 skiChoice
                    )
                    .orElse(nextTutorials.get(0)); // 如果未找到，返回列表中的第一个
        }
    }

    private Tutorial findPreviousTutorialWithOutDegreeZero(Graph<Tutorial, DefaultEdge> dag) {
        // 遍历图中的所有教程，查找出度为 0 的教程
        for (Tutorial tutorial : dag.vertexSet()) {
            if (dag.outDegreeOf(tutorial) == 0) {
                // 找到该教程的所有入边（前一个教程）
                Set<DefaultEdge> incomingEdges = dag.incomingEdgesOf(tutorial);
                if (!incomingEdges.isEmpty()) {
                    // 如果有入边，返回第一个入边的源节点（即前一个教程）
                    DefaultEdge incomingEdge = incomingEdges.iterator().next();
                    return dag.getEdgeSource(incomingEdge); // 返回前一个教程
                }
            }
        }
        return null; // 如果没有找到符合条件的教程，返回 null
    }


    @Override
    public List<List<TutorialWithImage>> getTutorialList(Long tutorialId) {
        // 获取初始教程和图结构
        Tutorial tutorial = tutorialMapperService.get(tutorialId);
        Graph<Tutorial, DefaultEdge> dag = TutorialUtils.getDAG();

        // 获取当前教程所在板块的边界
        Pair<Tutorial, Tutorial> boundaries = findBoundaries(tutorial, dag);
        log.info("Boundaries: {}", boundaries);
        Tutorial startNode = boundaries.getLeft();

        // 如果起始节点有前置节点，获取所有同级节点
        Set<Tutorial> startNodes = new HashSet<>();
        if (!dag.incomingEdgesOf(startNode).isEmpty()) {
            // 获取前置节点
            Tutorial previousNode = dag.incomingEdgesOf(startNode).stream()
                    .map(dag::getEdgeSource)
                    .findFirst()
                    .orElse(null);

            if (previousNode != null) {
                // 获取前置节点的所有后续节点（即所有同级节点）
                startNodes = dag.outgoingEdgesOf(previousNode).stream()
                        .map(dag::getEdgeTarget)
                        .collect(Collectors.toSet());
            }
        } else {
            // 如果没有前置节点，只处理startNode
            startNodes.add(startNode);
        }

        // 从所有起始节点开始查找路径
        List<List<Tutorial>> tutorials = findAllPaths(startNodes, dag);
        List<List<TutorialWithImage>> res = new ArrayList<>();
        for (List<Tutorial> tutorialList : tutorials) {
            List<TutorialWithImage> tutorialWithImages = tutorialList.stream()
                    .map(t -> TutorialWithImage.builder()
                            .id(t.getId())
                            .skiChoice(t.getSkiChoice())
                            .title(t.getTitle())
                            .subtitle(t.getSubtitle())
                            .content(t.getContent())
                            .videoUrl(t.getVideoUrl())
                            .videoTitle(t.getVideoTitle())
                            .images(tutorialImageMapperService.getByTutorialIdSortByIndex(t.getId()))
                            .build())
                    .toList();
            res.add(tutorialWithImages);
        }
        return res;
    }


    /**
     * 找到当前教程所在板块的起始和结束边界
     */
    private Pair<Tutorial, Tutorial> findBoundaries(Tutorial current, Graph<Tutorial, DefaultEdge> dag) {
        Tutorial startNode = findBoundaryNode(current, dag, true);
        Tutorial endNode = findBoundaryNode(current, dag, false);
        return Pair.of(startNode, endNode);
    }

    /**
     * 查找边界节点（向前或向后）
     * @param isBackward true表示向前查找，false表示向后查找
     */
    private Tutorial findBoundaryNode(Tutorial current, Graph<Tutorial, DefaultEdge> dag, boolean isBackward) {
        Tutorial result = current;
        while (true) {
            Set<DefaultEdge> edges = isBackward ?
                    dag.incomingEdgesOf(result) :
                    dag.outgoingEdgesOf(result);

            if (edges.isEmpty()) {
                return result;
            }

            List<Tutorial> connectedNodes = edges.stream()
                    .map(edge -> isBackward ? dag.getEdgeSource(edge) : dag.getEdgeTarget(edge))
                    .collect(Collectors.toList());

            // 处理单个连接节点的情况
            if (connectedNodes.size() == 1) {
                Tutorial connectedNode = connectedNodes.get(0);
                if (connectedNode.getSkiChoice().equals(result.getSkiChoice())) {
                    result = connectedNode;
                    continue;
                }
                break;
            }

            // 处理多个连接节点的情况
            Optional<Tutorial> nextNode = findNextNodeInMultiple(connectedNodes, result.getSkiChoice());
            if (nextNode.isEmpty()) {
                break;
            }
            result = nextNode.get();
        }
        return result;
    }

    /**
     * 在多个节点中找到下一个合适的节点
     */
    private Optional<Tutorial> findNextNodeInMultiple(List<Tutorial> nodes, Integer currentSkiChoice) {
        // 首先查找具有相同skiChoice的节点
        Optional<Tutorial> sameSkiChoice = nodes.stream()
                .filter(t -> t.getSkiChoice().equals(currentSkiChoice))
                .findFirst();

        if (sameSkiChoice.isPresent()) {
            return sameSkiChoice;
        }

        // 如果没有相同的skiChoice，寻找skiChoice为0的节点
        return nodes.stream()
                .filter(t -> t.getSkiChoice() == 0)
                .findFirst();
    }

    /**
     * 找到所有可能的路径
     * @param startNodes 所有起始节点（同级节点）
     */
    private List<List<Tutorial>> findAllPaths(Set<Tutorial> startNodes, Graph<Tutorial, DefaultEdge> dag) {
        List<List<Tutorial>> result = new ArrayList<>();

        // 对每个起始节点进行处理
        for (Tutorial startNode : startNodes) {
            Set<Tutorial> nextNodes = getNextNodes(startNode, dag);

            for (Tutorial nextNode : nextNodes) {
                List<Tutorial> initialPath = new ArrayList<>();
                initialPath.add(startNode);
                initialPath.add(nextNode);

                // 从每个分支开始寻找路径
                findPathsRecursively(nextNode, dag, initialPath, result);
            }
        }

        return result;
    }

    /**
     * 递归查找路径
     */
    private void findPathsRecursively(Tutorial current,
                                      Graph<Tutorial, DefaultEdge> dag,
                                      List<Tutorial> currentPath,
                                      List<List<Tutorial>> result) {
        Set<DefaultEdge> outgoingEdges = dag.outgoingEdgesOf(current);

        // 处理终点情况
        if (outgoingEdges.isEmpty()) {
            result.add(new ArrayList<>(currentPath));
            return;
        }

        List<Tutorial> nextNodes = outgoingEdges.stream()
                .map(dag::getEdgeTarget)
                .collect(Collectors.toList());

        // 处理单个后续节点的情况
        if (nextNodes.size() == 1) {
            processSingleNextNode(current, nextNodes.get(0), dag, currentPath, result);
            return;
        }

        // 处理多个后续节点的情况
        processMultipleNextNodes(current, nextNodes, dag, currentPath, result);
    }

    /**
     * 处理单个后续节点的情况
     */
    private void processSingleNextNode(Tutorial current,
                                       Tutorial next,
                                       Graph<Tutorial, DefaultEdge> dag,
                                       List<Tutorial> currentPath,
                                       List<List<Tutorial>> result) {
        if (next.getSkiChoice().equals(current.getSkiChoice())) {
            currentPath.add(next);
            findPathsRecursively(next, dag, currentPath, result);
        } else {
            result.add(new ArrayList<>(currentPath));
        }
    }

    /**
     * 处理多个后续节点的情况
     */
    private void processMultipleNextNodes(Tutorial current,
                                          List<Tutorial> nextNodes,
                                          Graph<Tutorial, DefaultEdge> dag,
                                          List<Tutorial> currentPath,
                                          List<List<Tutorial>> result) {
        Optional<Tutorial> sameSkiChoice = nextNodes.stream()
                .filter(t -> t.getSkiChoice().equals(current.getSkiChoice()))
                .findFirst();

        if (sameSkiChoice.isPresent()) {
            Tutorial next = sameSkiChoice.get();
            currentPath.add(next);
            findPathsRecursively(next, dag, currentPath, result);
        } else {
            Optional<Tutorial> zeroSkiChoice = nextNodes.stream()
                    .filter(t -> t.getSkiChoice() == 0)
                    .findFirst();

            if (zeroSkiChoice.isPresent()) {
                Tutorial next = zeroSkiChoice.get();
                currentPath.add(next);
                findPathsRecursively(next, dag, currentPath, result);
            } else {
                result.add(new ArrayList<>(currentPath));
            }
        }
    }

    /**
     * 获取节点的所有后续节点
     */
    private Set<Tutorial> getNextNodes(Tutorial tutorial, Graph<Tutorial, DefaultEdge> dag) {
        return dag.outgoingEdgesOf(tutorial).stream()
                .map(dag::getEdgeTarget)
                .collect(Collectors.toSet());
    }

    @Override
    public List<TradeMeta> getTradeList(Integer status) {
        List<Trade> trades;
        // 对trades列表进行状态处理
        if (status != null && status != TradeStatus.LEADER.getCode()) {
            trades = tradeMapperService.getByUserIdAndStatusSet(LoginUser.getLoginUserId(), STATUS_MAPPING.get(status));
            // 根据状态过滤，状态2要去掉已返程上车的订单，状态3要去掉未返程上车的订单
            if (status == 2) {
                trades.removeIf(Trade::getReturnBoarded);
            } else if (status == 3) {
                trades.removeIf(trade -> !trade.getReturnBoarded());
            }
        } else {
            trades = tradeMapperService.getByUserId(LoginUser.getLoginUserId());
            // 当未传入status时，需要为每个trade映射正确的状态
            for (Trade trade : trades) {
                // 首先找到当前trade.status对应的前端状态
                Integer mappedStatus = null;
                for (Map.Entry<Integer, Set<Integer>> entry : STATUS_MAPPING.entrySet()) {
                    if (entry.getValue().contains(trade.getStatus())) {
                        mappedStatus = entry.getKey();
                        // 对于状态2和3的重叠部分，还需要考虑returnBoarded字段
                        if (mappedStatus == 2 && trade.getReturnBoarded()) {
                            mappedStatus = 3;
                        } else if (mappedStatus == 3 && !trade.getReturnBoarded()) {
                            mappedStatus = 2;
                        }
                        break;
                    }
                }
                if (mappedStatus != null) {
                    trade.setStatus(mappedStatus);
                }
            }
        }
        if (trades.isEmpty()) {
            return new ArrayList<>();
        }
        List<TradeMeta> tradeMetas = new ArrayList<>();
        for (Trade trade : trades) {
            Activity activity = activityMapperService.get(trade.getActivityId());
            ActivityTemplate activityTemplate = activityTemplateMapperService.get(trade.getActivityTemplateId());
            Snowfield snowfield = snowfieldMapperService.get(trade.getSnowfieldId());
            Product product = productManager.getProductById(trade.getProductId());

            Integer displayStatus = status != null ? status : trade.getStatus();

            TradeMeta tradeMeta = new TradeMeta(
                    trade.getId(),
                    activityTemplate.getName(),
                    trade.getType(),
                    snowfield.getCover(),
                    snowfield.getIntro(),
                    DateTimeFormatter.ofPattern("MM月dd日").format(activity.getActivityBeginDate()),
                    product.getOriginalPrice(),
                    trade.getTotal(),
                    displayStatus
            );
            tradeMetas.add(tradeMeta);
        }
        return tradeMetas;
    }

    /**
     * 获取交易详情信息
     *
     * @param tradeId 交易ID
     * @return TradeDetailVo 包含交易详细信息的值对象
     * 该方法根据提供的交易ID检索并组装交易的详细信息，包括：
     * - 交易基本信息（状态、创建时间、总金额等）
     * - 关联的票券信息
     * - 活动模板和活动信息
     * - 雪场信息
     * - 用户账户信息
     * - 租赁信息（如果有）
     * 方法还会根据特定条件调整交易状态：
     * - 如果交易状态为已支付但未分配站点，则状态更新为确认中
     * - 如果交易状态为已锁定且已返回登机，则状态更新为已完成
     * 对于租赁信息，方法会计算总租金和押金。
     *
     * 最后，方法将所有收集的信息封装到TradeDetailVo对象中并返回。
     */

    @Override
    public TradeDetailVo getTradeDetail(Long tradeId) {
        Trade trade = tradeMapperService.get(tradeId);
        
        // 检查是否为多用户订单（通过outTradeNo查询所有相关Trade记录）
        List<Trade> trades = tradeMapperService.getAllByOutTradeNo(trade.getOutTradeNo());
        
        ActivityTemplate activityTemplate = activityTemplateMapperService.get(trade.getActivityTemplateId());
        Activity activity = activityMapperService.get(trade.getActivityId());
        Snowfield snowfield = snowfieldMapperService.get(trade.getSnowfieldId());
        Account account = accountMapperService.getById(trade.getUserId());
        Integer status = trade.getStatus();
        Product product = productManager.getProductById(trade.getProductId());
        
        // 添加租赁信息
        List<RentOrder> rentOrders = rentOrderMapperService.getByTradeId(tradeId);
        List<RentInfo> rentInfos = rentUtils.getRentInfoByTradeId(tradeId);
        int rentPrice = 0;
        int rentDeposit = 0;
        if (rentOrders!= null && !rentOrders.isEmpty()) {
            for (RentOrder rentOrder : rentOrders) {
                RentItem rentItem = rentItemMapperService.getById(rentOrder.getRentItemId());
                rentPrice += rentItem.getPrice();
                rentDeposit += rentItem.getDeposit();
            }
        }
        
        // 计算总金额（如果是多用户订单，累加所有Trade的总金额）
        int totalCost = trades.stream().mapToInt(Trade::getTotal).sum();
        
        // 获取优惠券信息
        Long userCouponId = null;
        TradeDetailVo.CouponInfo couponInfo = null;
        
        // 从第一个Trade记录获取userCouponId（所有Trade记录应该使用相同的优惠券）
        if (trade.getUserCouponId() != null) {
            userCouponId = trade.getUserCouponId();
            try {
                // 通过CouponService获取优惠券详细信息
                couponInfo = couponService.getCouponInfoForTrade(userCouponId);
            } catch (Exception e) {
                // 优惠券信息获取失败，不影响订单详情显示
                log.warn("获取优惠券信息失败，userCouponId: {}, 错误: {}", userCouponId, e.getMessage());
            }
        }
        
        // 构建用户信息列表
        List<TradeDetailVo.UserInfo> users = new ArrayList<>();
        for (Trade t : trades) {
            Account userAccount = accountMapperService.getById(t.getUserId());
            String schoolName = null;
            if (userAccount.getSchoolId() != null) {
                try {
                    schoolName = schoolMapperService.getById(userAccount.getSchoolId()).getName();
                } catch (Exception e) {
                    // School info fetch failed, use null
                }
            }
            
            TradeDetailVo.UserInfo userInfo = TradeDetailVo.UserInfo.builder()
                    .name(userAccount.getUsername())
                    .gender(userAccount.getGender())
                    .phone(userAccount.getPhone())
                    .idCardNumber(userAccount.getIdCardNumber())
                    .schoolId(userAccount.getSchoolId())
                    .schoolName(schoolName)
                    .build();
            users.add(userInfo);
        }

        return TradeDetailVo.builder()
                .tradeId(trade.getId())
                .status(status)
                .payDdl(trade.getCreateTime().plusMinutes(20))
                .activityName(activityTemplate.getName())
                .type(trade.getType())
                .intro(snowfield.getIntro())
                .beginDate(DateTimeFormatter.ofPattern("MM月dd日").format(activity.getActivityBeginDate()))
                .cover(snowfield.getCover())
                .cost(totalCost)  // 使用总金额
                .originalPrice(product.getOriginalPrice())
                .onTradeNo(trade.getOutTradeNo())
                .createTime(trade.getCreateTime())
                .payTime(trade.getPayTime())
                .canRefund(trade.getStatus() == TradeStatus.PAID.getCode())
                .activityId(activity.getId())
                .productId(product.getId())
                .rentPrice(rentPrice)
                .rentDeposit(rentDeposit)
                .rentInfos(rentInfos)
                .userCouponId(userCouponId)  // 优惠券ID
                .couponInfo(couponInfo)      // 优惠券详细信息
                .users(users)  // 多用户信息
                .userCount(trades.size())  // 用户数量
                .build();
    }



    @Override
    public List<Trade> getAdminTradeList() {
        return tradeMapperService.list();
    }

    @Override
    public void updateTrade(Trade trade) {
        tradeMapperService.updateById(trade);
    }
}
