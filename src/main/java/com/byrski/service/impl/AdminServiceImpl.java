package com.byrski.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Sequence;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.byrski.common.dispatch.StationGenerator;
import com.byrski.common.utils.*;
import com.byrski.domain.entity.BaseTicketEntity;
import com.byrski.domain.entity.dto.*;
import com.byrski.domain.entity.dto.ticket.RoomTicket;
import com.byrski.domain.entity.dto.ticket.Ticket;
import com.byrski.domain.entity.vo.request.ProductAddVo;
import com.byrski.domain.entity.vo.response.*;
import com.byrski.domain.entity.vo.request.ActivityTemplateVo;
import com.byrski.domain.entity.vo.request.UploadImage;
import com.byrski.domain.enums.*;
import com.byrski.common.exception.ByrSkiException;
import com.byrski.infrastructure.mapper.impl.*;
import com.byrski.infrastructure.repository.DocRepository;
import com.byrski.infrastructure.repository.manager.ProductManager;
import com.byrski.service.AdminService;
import com.byrski.domain.user.LoginUser;
import com.byrski.strategy.TicketStrategy;
import com.byrski.strategy.factory.TicketStrategyFactory;
import com.wechat.pay.java.service.refund.model.Refund;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.random;
import static java.util.Base64.*;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class AdminServiceImpl implements AdminService {
    private final AreaMapperService areaMapperService;
    private final SnowfieldMapperService snowfieldMapperService;
    private final SchoolMapperService schoolMapperService;
    private final StationInfoMapperService stationInfoMapperService;
    private final ActivityTemplateMapperService activityTemplateMapperService;
    private final ActivityMapperService activityMapperService;
    private final TradeMapperService tradeMapperService;
    private final OssUtils ossUtils;
    private final WxGroupMapperService wxGroupMapperService;
    private final StationMapperService stationMapperService;
    private final AreaLowerBoundMapperService areaLowerBoundMapperService;
    private final BusTypeMapperService busTypeMapperService;
    private final RentItemMapperService rentItemMapperService;
    private final BusMapperService busMapperService;
    private final BusMoveMapperService busMoveMapperService;
    private final RentOrderMapperService rentOrderMapperService;
    private final Sequence sequence;
    private final AccountMapperService accountMapperService;
    private final RoomMapperService roomMapperService;
    private final DocRepository docRepository;
    private final DocUtils docUtils;
    private final ProductManager productManager;
    private final TicketStrategyFactory ticketStrategyFactory;
    private final InfoUtils infoUtils;
    private final RentUtils rentUtils;
    private final PayUtils payUtils;
    private final TradeTicketsMapperService tradeTicketsMapperService;
    private final RedisUtils redisUtils;
    private final SnowfieldAccountMapperService snowfieldAccountMapperService;

    public AdminServiceImpl(AreaMapperService areaMapperService, SnowfieldMapperService snowfieldMapperService, SchoolMapperService schoolMapperService, StationInfoMapperService stationInfoMapperService, ActivityTemplateMapperService activityTemplateMapperService, ActivityMapperService activityMapperService, TradeMapperService tradeMapperService, OssUtils ossUtils, WxGroupMapperService wxGroupMapperService, StationMapperService stationMapperService, AreaLowerBoundMapperService areaLowerBoundMapperService, BusTypeMapperService busTypeMapperService, RentItemMapperService rentItemMapperService, BusMapperService busMapperService, BusMoveMapperService busMoveMapperService, RentOrderMapperService rentOrderMapperService, AccountMapperService accountMapperService, RoomMapperService roomMapperService, DocRepository docRepository, DocUtils docUtils, ProductManager productManager, TicketStrategyFactory ticketStrategyFactory, InfoUtils infoUtils, RentUtils rentUtils, PayUtils payUtils, TradeTicketsMapperService tradeTicketsMapperService, RedisUtils redisUtils, SnowfieldAccountMapperService snowfieldAccountMapperService) {
        this.areaMapperService = areaMapperService;
        this.snowfieldMapperService = snowfieldMapperService;
        this.schoolMapperService = schoolMapperService;
        this.stationInfoMapperService = stationInfoMapperService;
        this.activityTemplateMapperService = activityTemplateMapperService;
        this.activityMapperService = activityMapperService;
        this.tradeMapperService = tradeMapperService;
        this.ossUtils = ossUtils;
        this.wxGroupMapperService = wxGroupMapperService;
        this.stationMapperService = stationMapperService;
        this.areaLowerBoundMapperService = areaLowerBoundMapperService;
        this.busTypeMapperService = busTypeMapperService;
        this.rentItemMapperService = rentItemMapperService;
        this.busMapperService = busMapperService;
        this.busMoveMapperService = busMoveMapperService;
        this.rentOrderMapperService = rentOrderMapperService;
        this.sequence = new Sequence((InetAddress)null);
        this.accountMapperService = accountMapperService;
        this.roomMapperService = roomMapperService;
        this.docRepository = docRepository;
        this.docUtils = docUtils;
        this.productManager = productManager;
        this.ticketStrategyFactory = ticketStrategyFactory;
        this.infoUtils = infoUtils;
        this.rentUtils = rentUtils;
        this.payUtils = payUtils;
        this.tradeTicketsMapperService = tradeTicketsMapperService;
        this.redisUtils = redisUtils;
        this.snowfieldAccountMapperService = snowfieldAccountMapperService;
    }

    @Value("${oss.endpoint}")
    String ossEndPoint;
    @Value("${bus.small_capacity}")
    Integer smallCapacity;
    @Value("${bus.large_capacity}")
    Integer largeCapacity;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".gif");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Override
    public List<ActivityWithDetail> getActivityList() {
        List<Activity> activities = getMyActivityList();
        List<ActivityWithDetail> activityWithDetails = new ArrayList<>();
        for (Activity activity : activities) {
            ActivityTemplate activityTemplate = activityTemplateMapperService.get(activity.getActivityTemplateId());
            Snowfield snowfield = snowfieldMapperService.get(activity.getSnowfieldId());
            Area area = areaMapperService.get(snowfield.getAreaId());
            activityWithDetails.add(ActivityWithDetail.builder()
                    .activity(activity)
                    .activityTemplate(activityTemplate)
                    .snowfield(snowfield)
                    .area(area)
                    .build());
        }
        // 通过其中activity的activityBeginDate的降序排序
        activityWithDetails.sort(Comparator.comparing(ActivityWithDetail::getActivity, Comparator.comparing(Activity::getActivityBeginDate).reversed()));
        return activityWithDetails;
    }

    @Override
    public List<Activity> getActivityList(Long id) {
        return activityMapperService.getActivityListByActivityTemplateId(id);
    }

    @Override
    public Activity getActivity(Long id) {
        return activityMapperService.get(id);
    }

    @Override
    public Boolean updateActivity(Activity activity) {
        return activityMapperService.updateById(activity);
    }

    @Override
    public Boolean publishActivity(Long activityId) {
        Activity activity = activityMapperService.get(activityId);
        List<Station> stationList = stationMapperService.getByActivityId(activityId);
        if (stationList == null || stationList.isEmpty()) {
            throw new ByrSkiException(ReturnCode.ACTIVITY_STATION_MISSING);
        }
        WxGroup wxGroup = wxGroupMapperService.getByActivityId(activityId);
        if (wxGroup == null) {
            throw new ByrSkiException(ReturnCode.ACTIVITY_WXGROUP_MISSING);
        }
        List<Product> productList = productManager.getProductsByActivityId(activityId);
        if (productList == null || productList.isEmpty()) {
            throw new ByrSkiException(ReturnCode.ACTIVITY_PRODUCT_MISSING);
        }

        if (activity.getStatus() != ActivityStatus.EDITING.getCode()) {
            throw new ByrSkiException(ReturnCode.ACTIVITY_STATUS_ERROR);
        }
        activityMapperService.publishActivity(activityId);
        return true;
    }

    @Override
    public Boolean addActivity(Activity activity) {
        Long snowfieldId = activityTemplateMapperService.get(activity.getActivityTemplateId()).getSnowfieldId();
        activity.setSnowfieldId(snowfieldId);
        activity.setUserId(LoginUser.getLoginUserId());
        activity.setStatus(ActivityStatus.EDITING.getCode());
        Activity activityWithId = activityMapperService.addActivity(activity);
        Long activityId = activityWithId.getId();
        busTypeMapperService.save(BusType.builder().activityId(activityId).passengerNum(smallCapacity).build());
        busTypeMapperService.save(BusType.builder().activityId(activityId).passengerNum(largeCapacity).build());
        return true;
    }

    @Override
    public List<ActivityTemplateResponseVo> getActivityTemplateList() {

        Account account = accountMapperService.getById(LoginUser.getLoginUserId());
        if (account == null) {
            throw new ByrSkiException(ReturnCode.UNAUTHORIZED);
        }
        if (Objects.equals(account.getIdentity(), UserIdentity.ADMIN.getCode())) {
            // 管理员用户
            List<ActivityTemplate> list = activityTemplateMapperService.list();
            return getVoList(list);
        } else if (Objects.equals(account.getIdentity(), UserIdentity.GUEST.getCode())) {
            // 普通后台用户
            List<Long> mySnowfieldIds = snowfieldAccountMapperService.getMySnowfieldIds(LoginUser.getLoginUserId());
            List<ActivityTemplate> list = activityTemplateMapperService.getActivityTemplateListBySnowfieldIds(mySnowfieldIds);
            return getVoList(list);
        } else {
            throw new ByrSkiException(ReturnCode.UNAUTHORIZED);
        }
    }

    private List<ActivityTemplateResponseVo> getVoList(List<ActivityTemplate> list) {
        List<ActivityTemplateResponseVo> activityTemplateResponseVos = new ArrayList<>();
        for (ActivityTemplate activityTemplate : list) {
            Doc detail = docUtils.getDocByActivityTemplateIdAndType(activityTemplate.getId(), DocType.DETAIL.getCode());
            Doc schedule = docUtils.getDocByActivityTemplateIdAndType(activityTemplate.getId(), DocType.SCHEDULE.getCode());
            Doc attention = docUtils.getDocByActivityTemplateIdAndType(activityTemplate.getId(), DocType.ATTENTION.getCode());
            Doc leaderNotice = docUtils.getDocByActivityTemplateIdAndType(activityTemplate.getId(), DocType.LEADER_NOTICE.getCode());
            activityTemplateResponseVos.add(ActivityTemplateResponseVo.builder()
                    .id(activityTemplate.getId())
                    .durationDays(activityTemplate.getDurationDays())
                    .leaderNotice(Optional.ofNullable(leaderNotice).map(Doc::getContents).orElse(null))
                    .detail(Optional.ofNullable(detail).map(Doc::getContents).orElse(null))
                    .schedule(Optional.ofNullable(schedule).map(Doc::getContents).orElse(null))
                    .attention(Optional.ofNullable(attention).map(Doc::getContents).orElse(null))
                    .notes(activityTemplate.getNotes())
                    .snowfieldId(activityTemplate.getSnowfieldId())
                    .name(activityTemplate.getName())
                    .scheduleLite(activityTemplate.getScheduleLite())
                    .createTime(activityTemplate.getCreateTime())
                    .updateTime(activityTemplate.getUpdateTime())
                    .build());
        }
        return activityTemplateResponseVos;
    }

    @Override
    public List<ActivityTemplateResponseVo> getActivityTemplateList(Long id) {
        List<ActivityTemplate> activityTemplateListBySnowfieldId = activityTemplateMapperService.getActivityTemplateListBySnowfieldId(id);
        return getVoList(activityTemplateListBySnowfieldId);
    }

    @Override
    public ActivityTemplateResponseVo getActivityTemplate(Long id) {
        ActivityTemplate activityTemplate = activityTemplateMapperService.get(id);
        return getVo(activityTemplate);
    }

    private ActivityTemplateResponseVo getVo(ActivityTemplate activityTemplate) {
        Doc detail = docUtils.getDocByActivityTemplateIdAndType(activityTemplate.getId(), DocType.DETAIL.getCode());
        Doc schedule = docUtils.getDocByActivityTemplateIdAndType(activityTemplate.getId(), DocType.SCHEDULE.getCode());
        Doc attention = docUtils.getDocByActivityTemplateIdAndType(activityTemplate.getId(), DocType.ATTENTION.getCode());
        Doc leaderNotice = docUtils.getDocByActivityTemplateIdAndType(activityTemplate.getId(), DocType.LEADER_NOTICE.getCode());
        return ActivityTemplateResponseVo.builder()
                .id(activityTemplate.getId())
                .durationDays(activityTemplate.getDurationDays())
                .leaderNotice(Optional.ofNullable(leaderNotice).map(Doc::getContents).orElse(null))
                .detail(Optional.ofNullable(detail).map(Doc::getContents).orElse(null))
                .schedule(Optional.ofNullable(schedule).map(Doc::getContents).orElse(null))
                .attention(Optional.ofNullable(attention).map(Doc::getContents).orElse(null))
                .notes(activityTemplate.getNotes())
                .snowfieldId(activityTemplate.getSnowfieldId())
                .name(activityTemplate.getName())
                .scheduleLite(activityTemplate.getScheduleLite())
                .createTime(activityTemplate.getCreateTime())
                .updateTime(activityTemplate.getUpdateTime())
                .build();
    }

    @Override
    public Boolean updateActivityTemplate(ActivityTemplateVo activityTemplateVo) {
        docUtils.updateDocByActivityTemplateIdAndType(activityTemplateVo.getId(), DocType.DETAIL.getCode(), activityTemplateVo.getDetail());
        docUtils.updateDocByActivityTemplateIdAndType(activityTemplateVo.getId(), DocType.SCHEDULE.getCode(), activityTemplateVo.getSchedule());
        docUtils.updateDocByActivityTemplateIdAndType(activityTemplateVo.getId(), DocType.ATTENTION.getCode(), activityTemplateVo.getAttention());
        docUtils.updateDocByActivityTemplateIdAndType(activityTemplateVo.getId(), DocType.LEADER_NOTICE.getCode(), activityTemplateVo.getLeaderNotice());
        ActivityTemplate activityTemplate = ActivityTemplate.builder()
                .id(activityTemplateVo.getId())
                .snowfieldId(activityTemplateVo.getSnowfieldId())
                .name(activityTemplateVo.getName())
                .durationDays(activityTemplateVo.getDurationDays())
                .notes(activityTemplateVo.getNotes())
                .build();
        return activityTemplateMapperService.updateById(activityTemplate);
    }

    @Override
    public Boolean addActivityTemplate(ActivityTemplateVo activityTemplateVo) {

        ActivityTemplate activityTemplate = ActivityTemplate.builder()
                .snowfieldId(activityTemplateVo.getSnowfieldId())
                .name(activityTemplateVo.getName())
                .durationDays(activityTemplateVo.getDurationDays())
                .notes(activityTemplateVo.getNotes())
                .build();
        ActivityTemplate activityTemplate1 = activityTemplateMapperService.addActivityTemplate(activityTemplate);
        Doc detail = Doc.builder()
                .activityTemplateId(activityTemplate1.getId())
                .type(DocType.DETAIL.getCode())
                .contents(activityTemplateVo.getDetail())
                .build();
        docRepository.save(detail);
        Doc schedule = Doc.builder()
                .activityTemplateId(activityTemplate1.getId())
                .type(DocType.SCHEDULE.getCode())
                .contents(activityTemplateVo.getSchedule())
                .build();
        docRepository.save(schedule);
        Doc attention = Doc.builder()
                .activityTemplateId(activityTemplate1.getId())
                .type(DocType.ATTENTION.getCode())
                .contents(activityTemplateVo.getAttention())
                .build();
        docRepository.save(attention);
        Doc leaderNotice = Doc.builder()
                .activityTemplateId(activityTemplate1.getId())
                .type(DocType.LEADER_NOTICE.getCode())
                .contents(activityTemplateVo.getLeaderNotice())
                .build();
        docRepository.save(leaderNotice);
        return true;
    }

    @Override
    public List<Snowfield> getSnowfieldList() {
        return snowfieldMapperService.list();
    }

    @Override
    public Snowfield getSnowfield(Long snowfieldId) {
        return snowfieldMapperService.get(snowfieldId);
    }

    @Override
    public Boolean updateSnowfield(Snowfield snowfield) {
        return snowfieldMapperService.updateById(snowfield);
    }

    @Override
    public Boolean addSnowfield(Snowfield snowfield) {
        return snowfieldMapperService.save(snowfield);
    }

    @Override
    public List<BaseTicketEntity> getTicketListByActivityId(Long id) {
        return ticketStrategyFactory.getAllTicketsByActivityId(id);
    }

    @Override
    public Boolean addTicket(BaseTicketEntity ticket) {
        Ticket baseTicket = ticket.getBaseTicket();
        Long activityId = baseTicket.getActivityId();
        if (activityId == null || activityId == 0) {
            throw new ByrSkiException("活动ID不能为空");
        }
        // 最少/最多使用人数校验与默认值
        Integer minPeople = baseTicket.getMinPeople();
        Integer maxPeople = baseTicket.getMaxPeople();
        if (minPeople == null && maxPeople == null) {
            // 默认为单人票：1-1
            baseTicket.setMinPeople(1);
            baseTicket.setMaxPeople(1);
        } else {
            if (minPeople == null || maxPeople == null) {
                throw new ByrSkiException("最少/最多使用人数必须同时提供");
            }
            if (minPeople < 1 || maxPeople < 1) {
                throw new ByrSkiException("最少/最多使用人数必须为正整数");
            }
            if (maxPeople < minPeople) {
                throw new ByrSkiException("最多使用人数不能小于最少使用人数");
            }
        }
        Activity activity = activityMapperService.get(activityId);
        baseTicket.setSnowfieldId(activity.getSnowfieldId());
        baseTicket.setActivityTemplateId(activity.getActivityTemplateId());
        ticket.setBaseTicket(baseTicket);
        return ticketStrategyFactory.getStrategy(baseTicket.getType()).saveTicket(ticket) != null;
    }

    @Override
    public List<Trade> getTradeList() {
        return tradeMapperService.list();
    }

    @Override
    public Boolean updateTrade(Trade trade) {
        return tradeMapperService.updateById(trade);
    }

    @Override
    public List<Area> getAreaList() {
        return areaMapperService.list();
    }

    @Override
    public Boolean addArea(Area area) {
        return areaMapperService.save(area);
    }

    @Override
    public List<School> getSchoolList() {
        return schoolMapperService.list();
    }

    @Override
    public Boolean addSchool(School school) {
        return schoolMapperService.save(school);
    }

    @Override
    public Boolean addStationInfo(StationInfo stationInfo) {
        return stationInfoMapperService.save(stationInfo);
    }

    @Override
    public String uploadImage(UploadImage request) {
        // 验证文件大小（Base64解码前）
        if (request.getBase64Data().length() > 5_000_000) { // 约3.75MB的文件
            throw new ByrSkiException("文件大小不能超过3.75MB");
        }

        // 验证文件类型
        if (!request.getFileType().startsWith("image/")) {
            throw new ByrSkiException("只允许上传图片文件");
        }
        // 将Base64字符串转换为字节数组
        byte[] imageBytes = getDecoder().decode(request.getBase64Data());
        ByteArrayInputStream imageFile = new ByteArrayInputStream(imageBytes);
        // 生成唯一的文件名
        String objectKey = generateObjectKey(request.getFileName());
        // 上传到OSS
        ossUtils.getOssClient().putObject(Const.OSS_BUCKET, objectKey, imageFile);

        return "https://" + Const.OSS_BUCKET + "." + ossEndPoint + "/" + objectKey;
    }

    @Override
    public List<SnowfieldPanel> getSnowfieldPanelList() {
        List<Snowfield> list = snowfieldMapperService.list();
        List<SnowfieldPanel> snowfieldPanelList = new ArrayList<>();
        for (Snowfield snowfield : list) {
            SnowfieldPanel snowfieldPanel = SnowfieldPanel.builder()
                   .snowfield(snowfield)
                   .area(areaMapperService.get(snowfield.getAreaId()))
                   .build();
            snowfieldPanelList.add(snowfieldPanel);
        }
        return snowfieldPanelList;
    }

    @Override
    public List<WxGroup> getWxGroupListByActivityId(Long id) {
        return wxGroupMapperService.getWxGroupListByActivityId(id);
    }

    @Override
    public Boolean addWxGroup(WxGroup wxGroup) {
        return wxGroupMapperService.save(wxGroup);
    }

    @Override
    public Boolean addStation(Station station) {
        Long stationInfoId = station.getStationInfoId();
        Long areaId = stationInfoMapperService.get(stationInfoId).getAreaId();
        station.setAreaId(areaId);
        return stationMapperService.save(station);
    }

    @Override
    public Boolean addAreaLowerBound(AreaLowerBound areaLowerBound) {
        if (areaLowerBoundMapperService.addAreaLowerBoundWithInflict(areaLowerBound)) {
            return true;
        } else {
            throw new ByrSkiException(ReturnCode.OTHER_ERROR.getCode(), "该区域下限已存在");
        }
    }

    @Override
    public Boolean addBusType(BusType busType) {
        return busTypeMapperService.save(busType);
    }

    @Override
    public Boolean addRentItem(RentItem rentItem) {
        return rentItemMapperService.save(rentItem);
    }

    @Override
    public List<RentItem> getRentItemListByActivityId(Long id) {
        return rentItemMapperService.getRentItemListByActivityId(id);
    }

    @Override
    public List<BusType> getBusTypeListByActivityId(Long id) {
        return busTypeMapperService.getBusTypeByActivityId(id);
    }

    @Override
    public List<AreaLowerBoundWithArea> getAreaLowerBoundListByActivityId(Long id) {
        List<AreaLowerBound> areaLowerBounds = areaLowerBoundMapperService.getAreaLowerBoundListByActivityId(id);
        List<AreaLowerBoundWithArea> areaLowerBoundWithAreas = new ArrayList<>();
        for (AreaLowerBound areaLowerBound : areaLowerBounds) {
            Area area = areaMapperService.get(areaLowerBound.getAreaId());
            areaLowerBoundWithAreas.add(AreaLowerBoundWithArea.builder()
                    .id(areaLowerBound.getId())
                    .lowerLimit(areaLowerBound.getLowerLimit())
                    .activityId(areaLowerBound.getActivityId())
                    .areaId(area.getId())
                    .areaName(area.getAreaName())
                    .cityName(area.getCityName())
                    .build());
        }
        return areaLowerBoundWithAreas;
    }

    @Override
    public List<StationWithInfo> getStationListByActivityId(Long id) {
        Set<StationWithInfo> stationInfoSet = new HashSet<>();
        if (id == null) {
            if (Objects.equals(accountMapperService.getById(LoginUser.getLoginUserId()).getIdentity(), UserIdentity.ADMIN.getCode())) {
                // 管理员用户
                List<Station> stations = stationMapperService.list();
                extract2(stationInfoSet, stations);
            } else {
                // 普通后台用户
                List<Long> myActivityIds = activityMapperService.getMyActivityIds(LoginUser.getLoginUserId());
                for (Long activityId : myActivityIds) {
                    extract(stationInfoSet, activityId);
                }
            }
        } else {
            extract(stationInfoSet, id);
        }
        return new ArrayList<>(stationInfoSet);
    }

    private void extract2(Set<StationWithInfo> stationInfoSet, List<Station> stations) {
        for (Station station : stations) {
            StationInfo stationInfo = stationInfoMapperService.get(station.getStationInfoId());
            stationInfoSet.add(StationWithInfo.builder()
                            .id(station.getId())
                            .choicePeopleNum(station.getChoicePeopleNum())
                            .targetPeopleNum(station.getTargetPeopleNum())
                            .activityId(station.getActivityId())
                            .school(stationInfo.getSchool())
                            .campus(stationInfo.getCampus())
                            .location(stationInfo.getLocation())
                            .stationInfoId(stationInfo.getId())
                            .areaId(stationInfo.getAreaId())
                            .status(station.getStatus())
                    .build());
        }
    }

    private void extract(Set<StationWithInfo> stationInfoSet, Long activityId) {
        List<Station> stations = stationMapperService.getByActivityId(activityId);
        extract2(stationInfoSet, stations);
    }

    private StationWithInfo buildStationWithInfo(Station station) {
        StationInfo stationInfo = stationInfoMapperService.get(station.getStationInfoId());
        return StationWithInfo.builder()
                .id(station.getId())
                .areaId(station.getAreaId())
                .choicePeopleNum(station.getChoicePeopleNum())
                .campus(stationInfo.getCampus())
                .targetPeopleNum(station.getTargetPeopleNum())
                .location(stationInfo.getLocation())
                .status(station.getStatus())
                .stationInfoId(station.getStationInfoId())
                .school(stationInfo.getSchool())
                .activityId(station.getActivityId())
                .build();
    }

    @Override
    public Boolean updateBusType(BusType busType) {
        return busTypeMapperService.updateById(busType);
    }

    @Override
    public Snowfield getSnowfieldByActivityId(Long id) {
        return snowfieldMapperService.get(activityMapperService.get(id).getSnowfieldId());
    }

    @Override
    public ActivityTemplateResponseVo getActivityTemplateByActivityId(Long id) {
        ActivityTemplate activityTemplate = activityTemplateMapperService.get(activityMapperService.get(id).getActivityTemplateId());
        return getVo(activityTemplate);
    }

    @Override
    public Boolean updateRentItem(RentItem rentItem) {
        return rentItemMapperService.updateById(rentItem);
    }

    @Override
    public Boolean updateWxGroup(WxGroup wxGroup) {
        return wxGroupMapperService.updateById(wxGroup);
    }

    @Override
    public List<StationInfo> getStationInfoList() {
        return stationInfoMapperService.list();
    }

    @Override
    public Boolean updateStation(Station station) {
        Long stationInfoId = station.getStationInfoId();
        Long areaId = stationInfoMapperService.get(stationInfoId).getAreaId();
        station.setAreaId(areaId);
        return stationMapperService.updateById(station);
    }

    @Override
    public Boolean updateAreaLowerBound(AreaLowerBound areaLowerBound) {
        if (areaLowerBoundMapperService.updateAreaLowerBoundWithInflict(areaLowerBound)) {
            return true;
        } else {
            throw new ByrSkiException(ReturnCode.OTHER_ERROR.getCode(), "该区域下限已存在");
        }
    }

    @Override
    public Page<TradeWithDetail> getTradeWithDetailListByActivityId(Long activityId, int page, int pageSize, Long stationInfoId, Long busId, Integer status, Boolean goBoarded, Boolean returnBoarded, RoomType roomType, String username) {

        List<Long> stationIdList = stationMapperService.getStationIdsByStationInfoId(stationInfoId);
        Page<Trade> tradePage;
        Set<Long> userIdsWithNameFilter = new HashSet<>();
        if (username != null && !username.isEmpty()) {
            userIdsWithNameFilter = accountMapperService.findIdsByUsernameLike(username);
            if (userIdsWithNameFilter.isEmpty()) {
                return new Page<>(page, pageSize);
            }
        }
        if (Objects.equals(accountMapperService.getById(LoginUser.getLoginUserId()).getIdentity(), UserIdentity.ADMIN.getCode())) {
            // 管理员用户
            if (activityId == null) {
                // 查询全部
                tradePage = tradeMapperService.getTradeByActivityIds(List.of(), page, pageSize, stationIdList, busId, status, goBoarded, returnBoarded, roomType, userIdsWithNameFilter);
            } else {
                // 查询单个活动
                tradePage = tradeMapperService.getTradeByActivityIds(List.of(activityId), page, pageSize, stationIdList, busId, status, goBoarded, returnBoarded, roomType, userIdsWithNameFilter);
            }
        } else {
            // 普通后台用户
            if (activityId == null) {
                // 查询全部
                List<Long> myActivityIds = activityMapperService.getMyActivityIds(LoginUser.getLoginUserId());
                tradePage = tradeMapperService.getTradeByActivityIds(myActivityIds, page, pageSize, stationIdList, busId, status, goBoarded, returnBoarded, roomType, userIdsWithNameFilter);
            } else {
                // 查询单个活动
                tradePage = tradeMapperService.getTradeByActivityIds(List.of(activityId), page, pageSize, stationIdList, busId, status, goBoarded, returnBoarded, roomType, userIdsWithNameFilter);
            }
        }

        // 创建新的 Page 对象
        Page<TradeWithDetail> resultPage = new Page<>(page, pageSize);
        // 1. 收集所有需要查询的ID
        Set<Long> userIds = tradePage.getRecords().stream()
                .map(Trade::getUserId)
                .collect(Collectors.toSet());
        Set<Long> stationIds = tradePage.getRecords().stream()
                .map(Trade::getStationId)
                .filter(id -> id != null && id != -1)
                .collect(Collectors.toSet());
        Set<Long> busIds = tradePage.getRecords().stream()
                .map(Trade::getBusId)
                .filter(id -> id != null && id != -1)
                .collect(Collectors.toSet());
        Set<Long> busMoveIds = tradePage.getRecords().stream()
                .map(Trade::getBusMoveId)
                .filter(id -> id != null && id != -1)
                .collect(Collectors.toSet());
        Set<Long> activityIds = tradePage.getRecords().stream()
                .map(Trade::getActivityId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> templateIds = tradePage.getRecords().stream()
                .map(Trade::getActivityTemplateId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

// 2. 批量查询数据
        Map<Long, Account> accountMap = accountMapperService.getByIds(userIds);
        Map<Long, Station> stationMap = stationMapperService.getByIds(stationIds);
        Map<Long, Bus> busMap = busMapperService.getByIds(busIds);
        Map<Long, BusMove> busMoveMap = busMoveMapperService.getByIds(busMoveIds);
        Map<Long, Activity> activityMap = activityMapperService.getByIds(activityIds);
        Map<Long, ActivityTemplate> templateMap = activityTemplateMapperService.getByIds(templateIds);
        Map<Long, List<RentInfo>> rentInfoMap = rentUtils.getRentInfoMapByTradeIds(
                tradePage.getRecords().stream()
                        .map(Trade::getId)
                        .collect(Collectors.toList())
        );

        TicketStrategy<RoomTicket> strategy = ticketStrategyFactory.getStrategy(TicketType.ROOM);
        Map<Long, RoomTicket> tradeIdToRoomTicketMap = tradeTicketsMapperService.getRoomTicketIdMapByTradeIds(
                        tradePage.getRecords().stream()
                                .map(Trade::getId)
                                .collect(Collectors.toList())
                ).entrySet().stream()
                .filter(entry -> entry.getValue() != null)  // 过滤掉 null 的 ticketId
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            try {
                                return strategy.getTicketById(entry.getValue());
                            } catch (Exception e) {
                                throw new ByrSkiException("Failed to get RoomTicket for tradeId: " + entry.getKey());
                            }
                        },
                        (existing, replacement) -> existing,
                        HashMap::new
                ));
        // 3. 使用查询到的数据构建结果
        List<TradeWithDetail> tradeWithDetails = tradePage.getRecords().stream()
                .map(trade -> {
                    Account account = accountMap.get(trade.getUserId());
                    StationWithInfo stationWithInfo = null;
                    if (trade.getStationId() != null && trade.getStationId() != -1) {
                        Station station = stationMap.get(trade.getStationId());
                        if (station != null) {
                            stationWithInfo = buildStationWithInfo(station);
                        }
                    }

                    UserInfoVo userInfoVo = account != null ? UserInfoVo.builder()
                            .id(account.getId())
                            .name(account.getUsername())
                            .phone(account.getPhone())
                            .gender(account.getGender())
                            .build() : null;
                    return TradeWithDetail.builder()
                            .stationWithInfo(stationWithInfo)
                            .bus(trade.getBusId() != null && trade.getBusId() != -1 ? busMap.get(trade.getBusId()) : null)
                            .busMove(trade.getBusMoveId() != null && trade.getBusMoveId() != -1 ? busMoveMap.get(trade.getBusMoveId()) : null)
                            .trade(trade)
                            .userInfoVo(userInfoVo)
                            .activity(trade.getActivityId() != null ? activityMap.get(trade.getActivityId()) : null)
                            .activityName(trade.getActivityTemplateId() != null ? templateMap.get(trade.getActivityTemplateId()).getName() : null)
                            .rentInfos(rentInfoMap.getOrDefault(trade.getId(), Collections.emptyList()))
                            .roomTicket(tradeIdToRoomTicketMap.get(trade.getId()))
                            .build();
                })
                .collect(Collectors.toList());

        // 设置分页信息
        resultPage.setRecords(tradeWithDetails);
        resultPage.setTotal(tradePage.getTotal());
        resultPage.setCurrent(page);
        resultPage.setSize(pageSize);
        return resultPage;
    }

    private void validateFile(String fileName, long size, String fileType) {
        String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("不支持的文件类型");
        }
        if (size > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小超过限制");
        }
    }

    private String generateObjectKey(String originalFilename) {
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String timestamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
        return String.format("images/%s/%s%s",
                timestamp,
                UUID.randomUUID(),
                fileExtension);
    }

    public void adminTestGenerateData(Long activityId) {
        cleanTestData(activityId);
        generateStation(activityId);
        generateTrade(activityId);
    }

    @Override
    public List<Bus> getBusListByActivityId(Long id) {
        if (id == null) {
            if (accountMapperService.getById(LoginUser.getLoginUserId()).getIdentity().equals(UserIdentity.ADMIN.getCode())) {
                // 是管理员
                return busMapperService.list();
            } else {
                List<Long> myActivityIds = activityMapperService.getMyActivityIds(LoginUser.getLoginUserId());
                return busMapperService.getBusListByActivityIds(myActivityIds);
            }
        } else {
            return busMapperService.getByActivityId(id);
        }
    }

    @Override
    public List<BusMoveWithInfo> getBusMoveListByActivityId(Long id) {
        List<BusMove> busMoves = busMoveMapperService.getByActivityId(id);
        List<BusMoveWithInfo> busMoveWithInfos = new ArrayList<>();
        for (BusMove busMove : busMoves) {
            Bus bus = busMapperService.get(busMove.getBusId());
            Station station = stationMapperService.get(busMove.getStationId());
            StationInfo stationInfo = stationInfoMapperService.get(station.getStationInfoId());
            busMoveWithInfos.add(BusMoveWithInfo.builder()
                    .id(busMove.getId())
                    .stationPeopleNum(busMove.getStationPeopleNum())
                            .time(busMove.getTime())
                            .busId(busMove.getBusId())
                            .goFinished(busMove.getGoFinished())
                            .activityId(busMove.getActivityId())
                            .stationId(busMove.getStationId())
                            .school(stationInfo.getSchool())
                            .campus(stationInfo.getCampus())
                            .location(stationInfo.getLocation())
                    .build());
        }
        return busMoveWithInfos;
    }

    @Override
    public Boolean updateBusMove(BusMove busMove) {
        return busMoveMapperService.updateById(busMove);
    }

    @Override
    public String getUserName() {
        return accountMapperService.getById(LoginUser.getLoginUserId()).getUsername();
    }

    @Override
    public String getUserEmail() {
        return accountMapperService.getById(LoginUser.getLoginUserId()).getEmail();
    }

    @Override
    public List<RoomVo> getRoomList(Long activityId) {
        List<RoomVo> roomVos = new ArrayList<>();
        roomMapperService.getRoomListByActivityId(activityId).forEach(room -> {
            Long roomId = room.getId();
            List<Trade> trades = tradeMapperService.getByRoomId(roomId);
            List<UserInfoVo> users = new ArrayList<>();
            trades.forEach(trade -> {
                Account account = accountMapperService.getById(trade.getUserId());
                users.add(UserInfoVo.builder()
                        .id(account.getId())
                        .name(account.getUsername())
                        .phone(account.getPhone())
                        .gender(account.getGender())
                        .build());
            });
            roomVos.add(RoomVo.builder()
                    .room(room)
                    .members(users)
                    .build());
        });
        return roomVos;
    }

    @Override
    public void updateRoomNumber(Long activityId) {
        List<Room> rooms = roomMapperService.getRoomListByActivityId(activityId);
        int i = 1;
        for (Room room : rooms) {
            room.setNumber(i++);
            roomMapperService.updateById(room);
        }
    }

    @Override
    public ByteArrayResource exportRoomList(Long activityId) throws IOException {
        // 1. 先更新房间编号
        updateRoomNumber(activityId);

        // 2. 获取更新后的房间列表
        List<RoomVo> rooms = this.getRoomList(activityId);
        // 3. 创建工作簿
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("房间列表");
            int colNum = 0;
            // 创建表头
            Row headerRow = sheet.createRow(colNum);
            headerRow.createCell(colNum++).setCellValue("房间编号");
            headerRow.createCell(colNum++).setCellValue("房间加入码");
            headerRow.createCell(colNum++).setCellValue("发起人");
            headerRow.createCell(colNum++).setCellValue("联系电话");
            headerRow.createCell(colNum++).setCellValue("酒店");
            headerRow.createCell(colNum++).setCellValue("房间人数");
            headerRow.createCell(colNum++).setCellValue("成员姓名");

            // 填充数据
            int rowNum = 1;
            for (RoomVo room : rooms) {
                colNum = 0;
                Row row = sheet.createRow(rowNum++);
                row.createCell(colNum++).setCellValue(room.getRoom().getNumber());
                row.createCell(colNum++).setCellValue(room.getRoom().getCode());
                Account owner = accountMapperService.getById(room.getRoom().getOwnerId());
                row.createCell(colNum++).setCellValue(owner.getUsername() + "(" + Gender.fromCode(owner.getGender()).getDescription() + ")");
                row.createCell(colNum++).setCellValue(owner.getPhone());
                row.createCell(colNum++).setCellValue(room.getRoom().getName());
                row.createCell(colNum++).setCellValue(room.getRoom().getPeopleNum());
                StringBuilder members = new StringBuilder();
                for (UserInfoVo user : room.getMembers()) {
                    members.append(user.getName())
                            .append("(")
                            .append(user.getPhone())
                            .append(")")
                            .append("; ");
                }
                row.createCell(colNum++).setCellValue(members.toString());
            }

            // 自动调整列宽
            for (int i = 0; i < colNum; i++) {
                sheet.autoSizeColumn(i);
            }

            // 转换为字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayResource(outputStream.toByteArray());
        }
    }

    @Override
    public ByteArrayResource exportTradeList(Long id) {
        List<Trade> trades;
        if (id == null) {
            if (accountMapperService.getById(LoginUser.getLoginUserId()).getIdentity().equals(UserIdentity.ADMIN.getCode())) {
                // 管理员用户
                trades = tradeMapperService.getAllTradeByActivityIds(List.of());
            } else {
                List<Long> myActivityIds = activityMapperService.getMyActivityIds(LoginUser.getLoginUserId());
                trades = tradeMapperService.getAllTradeByActivityIds(myActivityIds);
            }
        } else {
            trades = tradeMapperService.getAllTradeByActivityIds(List.of(id));
        }

        // 创建工作簿
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Trade列表");

            // 创建表头
            Row headerRow = sheet.createRow(0);
            int colNum = 0;

            // 基本信息
            headerRow.createCell(colNum++).setCellValue("订单号");
            headerRow.createCell(colNum++).setCellValue("类型");
            headerRow.createCell(colNum++).setCellValue("姓名");
            headerRow.createCell(colNum++).setCellValue("身份证号");
            headerRow.createCell(colNum++).setCellValue("手机号");
            headerRow.createCell(colNum++).setCellValue("性别");
            headerRow.createCell(colNum++).setCellValue("学校");
            headerRow.createCell(colNum++).setCellValue("上车点");
            headerRow.createCell(colNum++).setCellValue("实付金额");
            headerRow.createCell(colNum++).setCellValue("酒店");
            headerRow.createCell(colNum++).setCellValue("租赁金额");
            headerRow.createCell(colNum++).setCellValue("租赁内容");

            // 填充数据
            int rowNum = 1;
            for (Trade trade : trades) {
                Row row = sheet.createRow(rowNum++);
                colNum = 0;
                Account user = accountMapperService.getById(trade.getUserId());
                RoomTicket roomTicket = getRoomTicketWithTradeId(trade.getId());
                // 基本信息
                row.createCell(colNum++).setCellValue(trade.getOutTradeNo() != null ? trade.getOutTradeNo() : "");
                row.createCell(colNum++).setCellValue(trade.getType() != null ? ProductType.fromCode(trade.getType().getCode()).getDescription() : "");
                row.createCell(colNum++).setCellValue(user.getUsername() != null ? user.getUsername() : "");
                row.createCell(colNum++).setCellValue(user.getIdCardNumber() != null ? user.getIdCardNumber() : "");
                row.createCell(colNum++).setCellValue(user.getPhone() != null ? user.getPhone() : "");
                row.createCell(colNum++).setCellValue(user.getGender() != null ? Gender.fromCode(user.getGender()).getDescription() : "");
                row.createCell(colNum++).setCellValue(user.getSchoolId() != null ? schoolMapperService.getById(user.getSchoolId()).getName() : "");
                row.createCell(colNum++).setCellValue((trade.getStationId() != null && trade.getStationId() != -1) ? stationInfoMapperService.get(stationMapperService.get(trade.getStationId()).getStationInfoId()).getPosition() : "");
                row.createCell(colNum++).setCellValue(trade.getTotal() != null ? String.format("%.2f", trade.getTotal() / 100.0) : "");
                row.createCell(colNum++).setCellValue(roomTicket != null ? roomTicket.getRoomType().getDescription() : "");
                row.createCell(colNum++).setCellValue(trade.getCostRent() != null ? String.format("%.2f", trade.getCostRent() / 100.0) : "");
                List<RentOrder> byTradeId = rentOrderMapperService.getByTradeId(trade.getId());
                StringBuilder rentItems = new StringBuilder();
                for (RentOrder rentOrder : byTradeId) {
                    RentItem rentItem = rentItemMapperService.get(rentOrder.getRentItemId());
                    rentItems.append(rentItem.getName()).append(";");
                }
                row.createCell(colNum++).setCellValue(rentItems.toString());

            }

            // 自动调整列宽
            for (int i = 0; i < colNum; i++) {
                sheet.autoSizeColumn(i);
            }

            // 设置冻结窗格，固定表头
            sheet.createFreezePane(0, 1);

            // 创建单元格样式
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // 应用表头样式
            for (int i = 0; i < colNum; i++) {
                Cell cell = headerRow.getCell(i);
                cell.setCellStyle(headerStyle);
            }

            // 转换为字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayResource(outputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
    public Page<UserInfoVo> getUserInfoVoList(long page, long pageSize, String name, Boolean isStudent, String phone) {
        // 创建分页对象
        Page<Account> accountPage = new Page<>(page, pageSize);

        // 创建查询条件（只查询普通用户）
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Account::getIdentity, UserIdentity.USER.getCode());
        wrapper.ne(Account::getIsActive, false);
        if (name != null) {
            wrapper.like(Account::getUsername, name);
        }
        if (isStudent != null) {
            wrapper.eq(Account::getIsStudent, isStudent);
        }
        if (phone != null) {
            wrapper.eq(Account::getPhone, phone);
        }

        // 执行分页查询
        Page<Account> accountResult = accountMapperService.page(accountPage, wrapper);

        // 转换为UserInfoVo的Page对象
        Page<UserInfoVo> userInfoVoPage = new Page<>(
                accountResult.getCurrent(),
                accountResult.getSize(),
                accountResult.getTotal()
        );

        // 转换数据
        List<UserInfoVo> userInfoVos = accountResult.getRecords().stream()
                .map(account -> infoUtils.getUserInfoVoById(account.getId()))
                .collect(Collectors.toList());

        userInfoVoPage.setRecords(userInfoVos);

        return userInfoVoPage;
    }

    @Override
    public Page<UserInfoVo> getBlackList(long page, long pageSize, String name, Boolean isStudent) {
        // 创建分页对象
        Page<Account> accountPage = new Page<>(page, pageSize);

        // 创建查询条件（只查询普通用户）
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Account::getIdentity, UserIdentity.USER.getCode(), UserIdentity.LEADER.getCode());
        wrapper.ne(Account::getIsActive, true);
        if (name != null) {
            wrapper.like(Account::getUsername, name);
        }
        if (isStudent != null) {
            wrapper.eq(Account::getIsStudent, isStudent);
        }

        // 执行分页查询
        Page<Account> accountResult = accountMapperService.page(accountPage, wrapper);

        // 转换为UserInfoVo的Page对象
        Page<UserInfoVo> userInfoVoPage = new Page<>(
                accountResult.getCurrent(),
                accountResult.getSize(),
                accountResult.getTotal()
        );

        // 转换数据
        List<UserInfoVo> userInfoVos = accountResult.getRecords().stream()
                .map(account -> infoUtils.getUserInfoVoById(account.getId()))
                .collect(Collectors.toList());

        userInfoVoPage.setRecords(userInfoVos);

        return userInfoVoPage;
    }

    @Override
    public Page<LeaderInfoVo> getLeaderInfoVoList(long page, long pageSize, String name, Boolean isStudent) {
        // 创建分页对象
        Page<Account> accountPage = new Page<>(page, pageSize);

        // 创建查询条件（只查询领队用户）
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Account::getIdentity, UserIdentity.LEADER.getCode());
        wrapper.ne(Account::getIsActive, false);

        if (name != null) {
            wrapper.like(Account::getUsername, name);
        }
        if (isStudent != null) {
            wrapper.eq(Account::getIsStudent, isStudent);
        }

        // 执行分页查询
        Page<Account> accountResult = accountMapperService.page(accountPage, wrapper);

        // 转换为LeaderInfoVo的Page对象
        Page<LeaderInfoVo> leaderInfoVoPage = new Page<>(
                accountResult.getCurrent(),
                accountResult.getSize(),
                accountResult.getTotal()
        );

        // 转换数据
        List<LeaderInfoVo> leaderInfoVos = accountResult.getRecords().stream()
                .map(account -> infoUtils.getLeaderInfoVoById(account.getId()))
                .collect(Collectors.toList());

        leaderInfoVoPage.setRecords(leaderInfoVos);

        return leaderInfoVoPage;
    }

    @Override
    public LeaderInfoVo getLeader(Long userId) {
        return infoUtils.getLeaderInfoVoById(userId);
    }

    @Override
    public List<LeaderInfoVo> getLeaderListByActivityId(Long id) {
        List<Trade> trades = tradeMapperService.getTradeByActivityIdWithLeader(id);
        Set<Long> userIds = trades.stream()
                .map(Trade::getUserId)
                .filter(accountMapperService::isLeader)
                .collect(Collectors.toSet());
        return userIds.stream().map(infoUtils::getLeaderInfoVoById).toList();
    }

    @Override
    public Boolean updateBus(Bus bus) {
        Long leaderId = bus.getLeaderId();
        if (leaderId != null) {
            Long activityId = bus.getActivityId();
            Long busId = bus.getId();
            tradeMapperService.updateLeaderTrade(leaderId, activityId, busId);
        }
        return busMapperService.updateById(bus);
    }

    @Override
    public Boolean removeLeaderFromBus(Long busId) {
        Bus bus = busMapperService.get(busId);
        Long leaderId = bus.getLeaderId();
        Long activityId = bus.getActivityId();
        tradeMapperService.removeLeaderTrade(leaderId, activityId, busId);
        return busMapperService.removeLeader(busId);
    }

    @Override
    public Account getAccount(Long id) {
        return accountMapperService.getById(id);
    }

    @Override
    public Boolean reverseBlack(Long userId) {
        Account account = accountMapperService.getById(userId);
        account.setIsActive(!account.getIsActive());
        return accountMapperService.updateById(account);
    }

    @Override
    public Boolean updateLeader(Long userId) {
        Account byId = accountMapperService.getById(userId);
        if (byId.getIdentity().equals(UserIdentity.LEADER.getCode())) {
            byId.setIdentity(UserIdentity.USER.getCode());
            return accountMapperService.updateById(byId);
        } else {
            byId.setIdentity(UserIdentity.LEADER.getCode());
            return accountMapperService.updateById(byId);
        }
    }

    @Override
    public Refund cancelTrade(Long tradeId) {
        return payUtils.getRefund(tradeMapperService.getWithoutOwner(tradeId));
    }

    @Override
    public Bus getBus(Long id) {
        return busMapperService.get(id);
    }

    private String formatPrice(Integer price) {
        if (price == null) return "";
        return String.format("%.2f", price / 100.0);
    }

    @Override
    public Product getProduct(String productId) {
        return productManager.getProductById(productId);
    }

    @Override
    public List<Product> getProductListByActivityId(Long activityId) {
        return productManager.findProductsByActivityId(activityId);
    }

    @Override
    public Boolean addProduct(ProductAddVo productAddVo) {
        List<BaseTicketEntity> tickets = new ArrayList<>();
        byte typeCode = 0b0;
        for (Long ticketId : productAddVo.getTicketIds()) {
            BaseTicketEntity ticket = ticketStrategyFactory.getTicketById(ticketId);
            tickets.add(ticket);
            byte id = ticket.getBaseTicket().getType().getId();
            typeCode |= id;
        }
        ProductType type = ProductType.fromId(typeCode);
        Product product = productManager.buildByTickets(tickets);
        product.setType(type);
        product.setIsStudent(productAddVo.getIsStudent());
        product.setName(productAddVo.getName());
        product.setDescription(productAddVo.getDescription());
        return productManager.saveProduct(product) != null;
    }

    @Override
    public List<Snowfield> getSnowfieldListWithActivityList() {
        List<Activity> activities = getMyActivityList();
        Set<Snowfield> snowfields = new HashSet<>();
        for (Activity activity : activities) {
            Snowfield snowfield = snowfieldMapperService.get(activity.getSnowfieldId());
            snowfields.add(snowfield);
        }
        return new ArrayList<>(snowfields);
    }

    private List<Activity> getMyActivityList() {
        Account account = accountMapperService.getById(LoginUser.getLoginUserId());
        List<Activity> activities = new ArrayList<>();
        if (Objects.equals(account.getIdentity(), UserIdentity.ADMIN.getCode())) {
            activities = activityMapperService.list();
        } else if (Objects.equals(account.getIdentity(), UserIdentity.GUEST.getCode())) {
            activities = activityMapperService.listByUserId(LoginUser.getLoginUserId());
        }
        return activities;
    }

    private void cleanTestData(Long activityId) {
        tradeMapperService.getTradeByActivityId(activityId).forEach(trade -> {
            rentOrderMapperService.lambdaUpdate().eq(RentOrder::getTradeId, trade.getId()).remove();
            tradeMapperService.removeById(trade.getId());
        });
        log.info("Trade和RentOder测试信息已删除");
        if (busMapperService.lambdaUpdate().eq(Bus::getActivityId, activityId).remove())
            log.info("Bus测试信息已删除");
        if (busMoveMapperService.lambdaUpdate().eq(BusMove::getActivityId, activityId).remove())
            log.info("BusMove测试信息已删除");
        if (stationMapperService.lambdaUpdate().eq(Station::getActivityId, activityId).remove())
            log.info("Station测试信息已删除");
        if (areaLowerBoundMapperService.lambdaUpdate().eq(AreaLowerBound::getActivityId, activityId).remove())
            log.info("AreaLowerBound测试信息已删除");
        if (activityMapperService.lambdaUpdate().eq(Activity::getId, activityId).set(Activity::getCurrentParticipant, 0).update())
            log.info("Activity参与人数信息已清零");
    }

    private void generateStation(Long activityId) {
        log.info("开始生成测试站点信息");
        // 确定站点数量为8-12之间的随机数
        int siteCount = (new Random().nextInt(5) + 8);
        Random rand = new Random();
        // 权重：Low (50%), Medium Low (30%), Medium High (15%), High (5%)
        int[] weights = {30, 30, 25, 15};
        int[][] ranges = {
                {5, 12},  // Low
                {13, 35}, // Medium Low
                {35, 60}, // Medium High
                {60, 120} // High
        };
        for (int i = 0; i < siteCount; i++) {
            int people = StationGenerator.getPeople(rand, weights, ranges);
            Long stationInfoId = (long) (new Random().nextInt(5) + 1);
            Long areaId = stationInfoMapperService.get(stationInfoId).getAreaId();
            Station station = Station.builder()
                    .targetPeopleNum(people)
                    .activityId(activityId)
                    .stationInfoId(stationInfoId)
                    // 随机生成1-5之间的areaId
                    .areaId(areaId)
                    .build();
            stationMapperService.save(station);
            log.info("Station {} with target at {}", station.getId(), station.getTargetPeopleNum());
        }
    }

    private void generateTrade(Long activityId) {
        log.info("开始生成测试交易信息");
        Activity activity = activityMapperService.get(activityId);
        List<Station> stations = stationMapperService.getByActivityId(activityId);
        List<Product> products = productManager.findProductsByActivityId(activityId);
        if (products == null ||products.isEmpty()) {
            log.error("No product found for activity {}", activityId);
            return;
        }

        for (Station station : stations) {
            // 随机生成选择人数
            Integer targetPeopleNum = station.getTargetPeopleNum();
            int minChoicePeopleNum = Math.max(0, targetPeopleNum - 5);
            int maxChoicePeopleNum = targetPeopleNum + 10;
            // 设定选择该station的人数在targetPeopleNum - 5到targetPeopleNum + 10之间
            int choicePeopleNum = (int) (minChoicePeopleNum + random() * (maxChoicePeopleNum - minChoicePeopleNum + 1));
            stationMapperService.addChoicePeopleNum(station.getId(), choicePeopleNum);

            List<RentItem> rentItems = rentItemMapperService.getRentItemListByActivityId(activityId);
            Integer durationDays = activityTemplateMapperService.get(activity.getActivityTemplateId()).getDurationDays();
            log.info("Station {} with choice at {}", station.getId(), choicePeopleNum);
            // 对每一个station都生成choicePeopleNum个trade，每个trade随机选择ticket和rentItem
            for (int i = 0; i < choicePeopleNum; i++) {
                Long tradeId = sequence.nextId();
                Product randomProduct = products.get(new Random().nextInt(products.size()));
                RentItem rentItem = new RentItem();
                int costTicket = randomProduct.getPrice();
                int costRent = 0;
                if (rentItems != null && !rentItems.isEmpty()) {
                    rentItem = rentItems.get(new Random().nextInt(rentItems.size()));
                    costRent = rentItem.getPrice();
                    RentOrder rentOrder = RentOrder.builder()
                            .tradeId(tradeId)
                            .rentDay(durationDays)
                            .rentItemId(rentItem.getId())
                            .userId(999L)
                            .build();
                    rentOrderMapperService.save(rentOrder);
                }
                Integer total = costTicket + costRent + rentItem.getDeposit();

                productManager.addSale(randomProduct.getId());
                activityMapperService.addCurrentParticipant(activityId);
                Trade trade = Trade.builder()
                        .id(tradeId)
                        .outTradeNo("test_trade_no_" + sequence.nextId())
                        .stationId(station.getId())
                        .status(TradeStatus.PAID.getCode())
                        .productId(randomProduct.getId())
                        .activityId(activityId)
                        .activityTemplateId(activity.getActivityTemplateId())
                        .snowfieldId(activity.getSnowfieldId())
                        .userId(999L)
                        .wxgroupId(999L)
                        .total(total)
                        .ticketCheck(false)
                        .costRent(costRent)
                        .costTicket(costTicket)
                        .build();
                tradeMapperService.save(trade);
            }
        }
    }

}
