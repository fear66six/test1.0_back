package com.byrski.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.byrski.domain.entity.BaseTicketEntity;
import com.byrski.domain.entity.RestBean;
import com.byrski.domain.entity.dto.*;
import com.byrski.domain.entity.vo.request.ProductAddVo;
import com.byrski.domain.entity.vo.response.*;
import com.byrski.domain.entity.vo.request.ActivityTemplateVo;
import com.byrski.domain.entity.vo.request.UploadImage;
import com.byrski.domain.enums.RoomType;
import com.byrski.service.*;
import com.wechat.pay.java.service.refund.model.Refund;
import org.springframework.core.io.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AdminController extends AbstractController {

    @Autowired
    private AdminService adminService;
    @Autowired
    private ActivityService activityService;

    @GetMapping("/activity/list")
    public RestBean<List<ActivityWithDetail>> getActivityList() {
        return handleRequest(null, log, new ExecuteCallbackWithoutReq<>() {
            @Override
            public List<ActivityWithDetail> doInTransactionWithoutReq() {
                return adminService.getActivityList();
            }
        });
    }

    @GetMapping("/activity/list/{id}")
    public RestBean<List<Activity>> getActivityListByActivityTemplateId(@PathVariable("id") Long id) {
        return handleRequest(id, log, id1 -> adminService.getActivityList(id1));
    }

    @GetMapping("/activity")
    public RestBean<Activity> getActivity(@RequestParam Long id) {
        return handleRequest(id, log, id1 -> adminService.getActivity(id1));
    }

    @GetMapping("/activity/snowfield")
    public RestBean<Snowfield> getSnowfieldByActivityId(@RequestParam Long id) {
        return handleRequest(id, log, id1 -> adminService.getSnowfieldByActivityId(id1));
    }

    @GetMapping("/activity/activity/template")
    public RestBean<ActivityTemplateResponseVo> getActivityTemplateByActivityId(@RequestParam Long id) {
        return handleRequest(id, log, id1 -> adminService.getActivityTemplateByActivityId(id1));
    }

    @PostMapping("/activity/update")
    public RestBean<Boolean> updateActivity(@RequestBody Activity activity) {
        return handleRequest(activity, log, activity1 -> adminService.updateActivity(activity1));
    }

    @PostMapping("/activity/publish")
    public RestBean<Boolean> publishActivity(@RequestParam Long id) {
        return handleRequest(id, log, id1 -> adminService.publishActivity(id1));
    }

    @PutMapping("/activity/add")
    public RestBean<Boolean> addActivity(@RequestBody Activity activity) {
        return handleRequest(activity, log, activity1 -> adminService.addActivity(activity1));
    }

    @GetMapping("/activity/template/list")
    public RestBean<List<ActivityTemplateResponseVo>> getActivityTemplateList() {
        return handleRequest(null, log, new ExecuteCallbackWithoutReq<>() {
            @Override
            public List<ActivityTemplateResponseVo> doInTransactionWithoutReq() {
                return adminService.getActivityTemplateList();
            }
        });
    }

    @GetMapping("/activity/template/list/{id}")
    public RestBean<List<ActivityTemplateResponseVo>> getActivityTemplateListBySnowfieldId(@PathVariable("id") Long id) {
        return handleRequest(id, log, id1 -> adminService.getActivityTemplateList(id1));
    }

    @GetMapping("/activity/template")
    public RestBean<ActivityTemplateResponseVo> getActivityTemplate(@RequestParam Long id) {
        return handleRequest(id, log, request -> adminService.getActivityTemplate(id));
    }

    @PostMapping("/activity/template/update")
    public RestBean<Boolean> updateActivityTemplate(@RequestBody ActivityTemplateVo activityTemplateVo) {
        return handleRequest(activityTemplateVo, log, activityTemplateVo1 -> adminService.updateActivityTemplate(activityTemplateVo1));
    }

    @PutMapping("/activity/template/add")
    public RestBean<Boolean> addActivityTemplate(@RequestBody ActivityTemplateVo activityTemplateVo) {
        return handleRequest(activityTemplateVo, log, activityTemplate -> adminService.addActivityTemplate(activityTemplateVo));
    }

    @GetMapping("/snowfield/list")
    public RestBean<List<Snowfield>> getSnowfieldList() {
        return handleRequest(null, log, new ExecuteCallbackWithoutReq<>() {
            @Override
            protected List<Snowfield> doInTransactionWithoutReq() throws Exception {
                return adminService.getSnowfieldList();
            }
        });
    }

    @GetMapping("/snowfield/activity/list")
    public RestBean<List<Snowfield>> getSnowfieldListWithActivityList() {
        return handleRequest(null, log, new ExecuteCallbackWithoutReq<>() {
            @Override
            protected List<Snowfield> doInTransactionWithoutReq() throws Exception {
                return adminService.getSnowfieldListWithActivityList();
            }
        });
    }

    @GetMapping("/snowfield")
    public RestBean<Snowfield> getSnowfield(@RequestParam Long id) {
        return handleRequest(id, log, id1 -> adminService.getSnowfield(id1));
    }

    @GetMapping("/snowfield/panel")
    public RestBean<List<SnowfieldPanel>> getSnowfieldPanelList() {
        return handleRequest(null, log, new ExecuteCallbackWithoutReq<>() {
            @Override
            protected List<SnowfieldPanel> doInTransactionWithoutReq() throws Exception {
                return adminService.getSnowfieldPanelList();
            }
        });
    }

    @PostMapping("/snowfield/update")
    public RestBean<Boolean> updateSnowfield(@RequestBody Snowfield snowfield) {
        return handleRequest(snowfield, log, snowfield1 -> adminService.updateSnowfield(snowfield1));
    }

    @PutMapping("/snowfield/add")
    public RestBean<Boolean> addSnowfield(@RequestBody Snowfield snowfield) {
        return handleRequest(snowfield, log, snowfield1 -> adminService.addSnowfield(snowfield1));
    }

    @GetMapping("/ticket/list/{id}")
    public RestBean<List<BaseTicketEntity>> getTicketListByActivityId(@PathVariable("id") Long id) {
        return handleRequest(id, log, activityId -> adminService.getTicketListByActivityId(activityId));
    }

    @PutMapping("/ticket/add")
    public RestBean<Boolean> addTicket(@RequestBody BaseTicketEntity ticket) {
        return handleRequest(ticket, log, ticket1 -> adminService.addTicket(ticket1));
    }

    @GetMapping("/product")
    public RestBean<Product> getProduct(@RequestParam String id) {
        return handleRequest(id, log, id1 -> adminService.getProduct(id1));
    }

    @GetMapping("/product/list/{id}")
    public RestBean<List<Product>> getProductListByActivityId(@PathVariable("id") Long id) {
        return handleRequest(id, log, activityId -> adminService.getProductListByActivityId(activityId));
    }

    @PutMapping("/product/add")
    public RestBean<Boolean> addProduct(@RequestBody ProductAddVo productAddVo) {
        return handleRequest(productAddVo, log, productAddVo1 -> adminService.addProduct(productAddVo1));
    }


    @GetMapping("/trade/list")
    public RestBean<List<Trade>> getTradeList() {
        return handleRequest(null, log, new ExecuteCallbackWithoutReq<>() {
            @Override
            public List<Trade> doInTransactionWithoutReq() {
                return adminService.getTradeList();
            }
        });
    }

    @GetMapping({"/trade/detail/list/{id}", "/trade/detail/list"})
    public RestBean<Page<TradeWithDetail>> getTradeWithDetailListByActivityId(
            @PathVariable(value = "id", required = false) Long id,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "stationInfoId", required = false) Long stationInfoId,
            @RequestParam(value = "busId", required = false) Long busId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "goBoarded", required = false) Boolean goBoarded,
            @RequestParam(value = "returnBoarded", required = false) Boolean returnBoarded,
            @RequestParam(value = "roomType", required = false) RoomType roomType,
            @RequestParam(value = "username", required = false) String username
    ) {
        return handleRequest(id, log, id1 -> adminService.getTradeWithDetailListByActivityId(id1, page, pageSize, stationInfoId, busId, status, goBoarded, returnBoarded, roomType, username));
    }

    @PostMapping("/trade/update")
    public RestBean<Boolean> updateTrade(@RequestBody Trade trade) {
        return handleRequest(trade, log, trade1 -> adminService.updateTrade(trade1));
    }

    @PostMapping("/cancel")
    public RestBean<Refund> cancel(@RequestParam Long tradeId) {
        return handleRequest(tradeId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public Refund doInTransactionWithResult(Long tradeId) {
                return adminService.cancelTrade(tradeId);
            }
        });
    }

    @GetMapping("/area/list")
    public RestBean<List<Area>> getAreaList() {
        return handleRequest(null, log, new ExecuteCallbackWithoutReq<>() {
            @Override
            public List<Area> doInTransactionWithoutReq() {
                return adminService.getAreaList();
            }
        });
    }

    @PutMapping("/area/add")
    public RestBean<Boolean> addArea(@RequestBody Area area) {
        return handleRequest(area, log, area1 -> adminService.addArea(area1));
    }

    @GetMapping("/school/list")
    public RestBean<List<School>> getSchoolList() {
        return handleRequest(null, log, new ExecuteCallbackWithoutReq<>() {
            @Override
            public List<School> doInTransactionWithoutReq() {
                return adminService.getSchoolList();
            }
        });
    }

    @PutMapping("/school/add")
    public RestBean<Boolean> addSchool(@RequestBody School school) {
        return handleRequest(school, log, school1 -> adminService.addSchool(school1));
    }

    @PutMapping("/station/info/add")
    public RestBean<Boolean> addStationInfo(@RequestBody StationInfo stationInfo) {
        return handleRequest(stationInfo, log, stationInfo1 -> adminService.addStationInfo(stationInfo1));
    }

    @GetMapping("/station/info/list")
    public RestBean<List<StationInfo>> getStationInfoList() {
        return handleRequest(null, log, new ExecuteCallbackWithoutReq<>() {
            @Override
            public List<StationInfo> doInTransactionWithoutReq() {
                return adminService.getStationInfoList();
            }
        });
    }

    @PutMapping("/station/add")
    public RestBean<Boolean> addStation(@RequestBody Station station) {
        return handleRequest(station, log, station1 -> adminService.addStation(station1));
    }

    @PostMapping("/station/update")
    public RestBean<Boolean> updateStation(@RequestBody Station station) {
        return handleRequest(station, log, station1 -> adminService.updateStation(station1));
    }

    @GetMapping({"/station/list", "/station/list/{id}"})
    public RestBean<List<StationWithInfo>> getStationListByActivityId(@PathVariable(required = false) Long id) {
        return handleRequest(id, log, id1 -> adminService.getStationListByActivityId(id1));
    }

    @PutMapping("/area/lower/bound/add")
    public RestBean<Boolean> addAreaLowerBound(@RequestBody AreaLowerBound areaLowerBound) {
        return handleRequest(areaLowerBound, log, areaLowerBound1 -> adminService.addAreaLowerBound(areaLowerBound1));
    }

    @PostMapping("/area/lower/bound/update")
    public RestBean<Boolean> updateAreaLowerBound(@RequestBody AreaLowerBound areaLowerBound) {
        return handleRequest(areaLowerBound, log, areaLowerBound1 -> adminService.updateAreaLowerBound(areaLowerBound1));
    }

    @GetMapping("/area/lower/bound/list/{id}")
    public RestBean<List<AreaLowerBoundWithArea>> getAreaLowerBoundListByActivityId(@PathVariable("id") Long id) {
        return handleRequest(id, log, id1 -> adminService.getAreaLowerBoundListByActivityId(id1));
    }

    @PostMapping("/bus/update")
    public RestBean<Boolean> updateBus(@RequestBody Bus bus) {
        return handleRequest(bus, log, bus1 -> adminService.updateBus(bus1));
    }

    @PutMapping("/bus/remove/leader")
    public RestBean<Boolean> removeLeaderFromBus(@RequestParam Long busId) {
        return handleRequest(busId, log, busId1 -> adminService.removeLeaderFromBus(busId1));
    }

    @GetMapping({"/bus/list/{id}", "/bus/list"})
    public RestBean<List<Bus>> getBusListByActivityId(@PathVariable(value = "id", required = false) Long id) {
        return handleRequest(id, log, id1 -> adminService.getBusListByActivityId(id1));
    }

    @GetMapping("/bus/{id}")
    public RestBean<Bus> getBus(@PathVariable("id") Long id) {
        return handleRequest(id, log, id1 -> adminService.getBus(id1));
    }

    @PostMapping("/bus/move/update")
    public RestBean<Boolean> updateBusMove(@RequestBody BusMove busMove) {
        return handleRequest(busMove, log, busMove1 -> adminService.updateBusMove(busMove1));
    }

    @GetMapping("/bus/move/list/{id}")
    public RestBean<List<BusMoveWithInfo>> getBusMoveListByActivityId(@PathVariable("id") Long id) {
        return handleRequest(id, log, id1 -> adminService.getBusMoveListByActivityId(id1));
    }

    @PutMapping("/bus/type/add")
    public RestBean<Boolean> addBusType(@RequestBody BusType busType) {
        return handleRequest(busType, log, busType1 -> adminService.addBusType(busType1));
    }

    @PostMapping("/bus/type/update")
    public RestBean<Boolean> updateBusType(@RequestBody BusType busType) {
        return handleRequest(busType, log, busType1 -> adminService.updateBusType(busType1));
    }

    @GetMapping("/bus/type/list/{id}")
    public RestBean<List<BusType>> getBusTypeListByActivityId(@PathVariable("id") Long id) {
        return handleRequest(id, log, id1 -> adminService.getBusTypeListByActivityId(id1));
    }

    @PutMapping("/rent/item/add")
    public RestBean<Boolean> addRentItem(@RequestBody RentItem rentItem) {
        return handleRequest(rentItem, log, rentItem1 -> adminService.addRentItem(rentItem1));
    }

    @PostMapping("/rent/item/update")
    public RestBean<Boolean> updateRentItem(@RequestBody RentItem rentItem) {
        return handleRequest(rentItem, log, rentItem1 -> adminService.updateRentItem(rentItem1));
    }

    @GetMapping("/rent/item/list/{id}")
    public RestBean<List<RentItem>> getRentItemListByActivityId(@PathVariable("id") Long id) {
        return handleRequest(id, log, id1 -> adminService.getRentItemListByActivityId(id1));
    }

    @PutMapping("/wxgroup/add")
    public RestBean<Boolean> addWxGroup(@RequestBody WxGroup wxGroup) {
        return handleRequest(wxGroup, log, wxGroup1 -> adminService.addWxGroup(wxGroup1));
    }

    @PostMapping("/wxgroup/update")
    public RestBean<Boolean> updateWxGroup(@RequestBody WxGroup wxGroup) {
        return handleRequest(wxGroup, log, wxGroup1 -> adminService.updateWxGroup(wxGroup1));
    }

    @GetMapping("/wxgroup/list/{id}")
    public RestBean<List<WxGroup>> getWxGroupListByActivityId(@PathVariable("id") Long id) {
        return handleRequest(id, log, id1 -> adminService.getWxGroupListByActivityId(id1));
    }

    @PostMapping("/upload")
    public RestBean<String> uploadImage(@RequestBody UploadImage uploadImage) {
        return handleRequest(uploadImage, log, request -> adminService.uploadImage(request));
    }

    @GetMapping("/data")
    public RestBean<Void> adminTestGenerateData(@RequestParam Long activityId) {
        return handleRequest(activityId, log, activityId1 -> {
            adminService.adminTestGenerateData(activityId1);
            return null;
        });
    }

    @GetMapping("/dead")
    public RestBean<Void> adminTestDead(@RequestParam Long activityId) {
        return handleRequest(activityId, log, activityId1 -> {
            activityService.TestDead(activityId1);
            return null;
        });
    }

    @GetMapping("/lock")
    public RestBean<Void> adminTestLock(@RequestParam Long activityId) {
        return handleRequest(activityId, log, activityId1 -> {
            activityService.TestLock(activityId1);
            return null;
        });
    }

    @GetMapping("/room/{id}")
    public RestBean<List<RoomVo>> getRoomList(@PathVariable("id") Long activityId) {
        return handleRequest(activityId, log, activityId1 -> adminService.getRoomList(activityId1));
    }

    @PostMapping("/room/export/{id}")
    public ResponseEntity<Resource> exportRoomList(@PathVariable("id") Long activityId) throws IOException {
        // 生成Excel文件
        ByteArrayResource resource = adminService.exportRoomList(activityId);

        // 设置响应头
        String filename = "room-list-" + activityId + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    @PostMapping({"/trade/export/{id}", "/trade/export"})
    public ResponseEntity<Resource> exportTradeList(@PathVariable(value = "id", required = false) Long id) throws IOException {
        // 生成Excel文件
        ByteArrayResource resource = adminService.exportTradeList(id);

        String filename = id != null ? "trade-list-" + id + ".xlsx" : "trade-list.xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    @GetMapping("/user/list")
    public RestBean<Page<UserInfoVo>> getUserList(
            @RequestParam(required = false, defaultValue = "1") long page,
            @RequestParam(required = false, defaultValue = "10") long pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean isStudent,
            @RequestParam(required = false) String phone) {
        return handleRequest(null, log, new ExecuteCallbackWithoutReq<>() {
            @Override
            public Page<UserInfoVo> doInTransactionWithoutReq() {
                return adminService.getUserInfoVoList(page, pageSize, name, isStudent, phone);
            }
        });
    }

    @GetMapping("/blacklist/list")
    public RestBean<Page<UserInfoVo>> getBlackList(
            @RequestParam(required = false, defaultValue = "1") long page,
            @RequestParam(required = false, defaultValue = "10") long pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean isStudent) {
        return handleRequest(null, log, new ExecuteCallbackWithoutReq<>() {
            @Override
            public Page<UserInfoVo> doInTransactionWithoutReq() {
                return adminService.getBlackList(page, pageSize, name, isStudent);
            }
        });
    }

    @GetMapping("/user/detail/{id}")
    public RestBean<Account> getUserDetail(@PathVariable("id") Long id) {
        return handleRequest(id, log, id1 -> adminService.getAccount(id1));
    }


    @GetMapping("/leader/list")
    public RestBean<Page<LeaderInfoVo>> getLeaderList(
            @RequestParam(required = false, defaultValue = "1") long page,
            @RequestParam(required = false, defaultValue = "10") long pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean isStudent) {
        return handleRequest(null, log, new ExecuteCallbackWithoutReq<>() {
            @Override
            public Page<LeaderInfoVo> doInTransactionWithoutReq() {
                return adminService.getLeaderInfoVoList(page, pageSize, name, isStudent);
            }
        });
    }

    @GetMapping("/leader/list/{id}")
    public RestBean<List<LeaderInfoVo>> getLeaderListByActivityId(@PathVariable("id") Long id) {
        return handleRequest(id, log, id1 -> adminService.getLeaderListByActivityId(id1));
    }

    @GetMapping("/leader")
    public RestBean<LeaderInfoVo> getLeader(@RequestParam Long id) {
        return handleRequest(id, log, id1 -> adminService.getLeader(id1));
    }

    @PostMapping("/leader/update")
    public RestBean<Boolean> updateLeader(@RequestParam Long userId) {
        return handleRequest(userId, log, userId1 -> adminService.updateLeader(userId1));
    }

    @PutMapping("/blacklist/reverse")
    public RestBean<Boolean> addBlacklist(@RequestParam Long userId) {
        return handleRequest(userId, log, userId1 -> adminService.reverseBlack(userId1));
    }
}
