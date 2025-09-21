package com.byrski.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.byrski.domain.entity.BaseTicketEntity;
import com.byrski.domain.entity.dto.*;
import com.byrski.domain.entity.vo.request.ProductAddVo;
import com.byrski.domain.entity.vo.response.*;
import com.byrski.domain.entity.vo.request.ActivityTemplateVo;
import com.byrski.domain.entity.vo.request.UploadImage;
import com.byrski.domain.enums.RoomType;
import com.wechat.pay.java.service.refund.model.Refund;
import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;
import java.util.List;

public interface AdminService {

    List<ActivityWithDetail> getActivityList();

    List<Activity> getActivityList(Long id);

    Activity getActivity(Long id);

    Boolean updateActivity(Activity activity);

    Boolean publishActivity(Long activityId);

    Boolean addActivity(Activity activity);

    List<ActivityTemplateResponseVo> getActivityTemplateList();

    List<ActivityTemplateResponseVo> getActivityTemplateList(Long id);

    ActivityTemplateResponseVo getActivityTemplate(Long id);

    Boolean updateActivityTemplate(ActivityTemplateVo activityTemplateVo);

    Boolean addActivityTemplate(ActivityTemplateVo activityTemplateVo);

    List<Snowfield> getSnowfieldList();

    Snowfield getSnowfield(Long snowfieldId);

    Boolean updateSnowfield(Snowfield snowfield);

    Boolean addSnowfield(Snowfield snowfield);

    List<BaseTicketEntity> getTicketListByActivityId(Long id);

    Boolean addTicket(BaseTicketEntity ticket);

    List<Trade> getTradeList();

    Boolean updateTrade(Trade trade);

    List<Area> getAreaList();

    Boolean addArea(Area area);

    List<School> getSchoolList();

    Boolean addSchool(School school);

    Boolean addStationInfo(StationInfo stationInfo);

    String uploadImage(UploadImage request);

    List<SnowfieldPanel> getSnowfieldPanelList();

    List<WxGroup> getWxGroupListByActivityId(Long id);

    Boolean addWxGroup(WxGroup wxGroup);

    Boolean addStation(Station station);

    Boolean addAreaLowerBound(AreaLowerBound areaLowerBound);

    Boolean addBusType(BusType busType);

    Boolean addRentItem(RentItem rentItem);

    List<RentItem> getRentItemListByActivityId(Long id);

    List<BusType> getBusTypeListByActivityId(Long id);

    List<AreaLowerBoundWithArea> getAreaLowerBoundListByActivityId(Long id);

    List<StationWithInfo> getStationListByActivityId(Long id);

    Boolean updateBusType(BusType busType);

    Snowfield getSnowfieldByActivityId(Long id);

    ActivityTemplateResponseVo getActivityTemplateByActivityId(Long id);

    Boolean updateRentItem(RentItem rentItem);

    Boolean updateWxGroup(WxGroup wxGroup);

    List<StationInfo> getStationInfoList();

    Boolean updateStation(Station station);

    Boolean updateAreaLowerBound(AreaLowerBound areaLowerBound);

    Page<TradeWithDetail> getTradeWithDetailListByActivityId(Long id, int page, int pageSize, Long stationInfoId, Long busId, Integer status, Boolean goBoarded, Boolean returnBoarded, RoomType roomType, String username);

    void adminTestGenerateData(Long activityId);

    List<Bus> getBusListByActivityId(Long id);

    List<BusMoveWithInfo> getBusMoveListByActivityId(Long id);

    Boolean updateBusMove(BusMove busMove);

    Boolean updateBus(Bus bus);

    String getUserName();

    String getUserEmail();

    List<RoomVo> getRoomList(Long activityId);

    void updateRoomNumber(Long activityId);

    ByteArrayResource exportRoomList(Long activityId) throws IOException;

    Product getProduct(String productId);

    List<Product> getProductListByActivityId(Long activityId);

    Boolean addProduct(ProductAddVo productAddVo);

    List<Snowfield> getSnowfieldListWithActivityList();

    ByteArrayResource exportTradeList(Long id);

    Page<UserInfoVo> getUserInfoVoList(long page, long pageSize, String name, Boolean isStudent, String phone);

    Page<UserInfoVo> getBlackList(long page, long pageSize, String name, Boolean isStudent);

    Page<LeaderInfoVo> getLeaderInfoVoList(long page, long pageSize, String name, Boolean isStudent);

    Boolean updateLeader(Long userId);

    Refund cancelTrade(Long tradeId);

    Bus getBus(Long id);

    LeaderInfoVo getLeader(Long userId);

    List<LeaderInfoVo> getLeaderListByActivityId(Long id1);

    Boolean removeLeaderFromBus(Long busId);

    Account getAccount(Long id);

    Boolean reverseBlack(Long userId);

}
