package com.byrski.controller;

import com.byrski.domain.entity.RestBean;
import com.byrski.domain.entity.vo.response.ActivityDetailVo;
import com.byrski.domain.entity.vo.response.HomePageVo;
import com.byrski.service.ActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/activity")
public class ActivityController extends AbstractController{

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping("/detail")
    public RestBean<ActivityDetailVo> getActivityDetail(@RequestParam Long activityId) {
        return handleRequest(activityId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public ActivityDetailVo doInTransactionWithResult(Long activityId) {
                return activityService.getActivityDetail(activityId);
            }
        });
    }

    @GetMapping("/home")
    public RestBean<HomePageVo> getHomePage() {
        return handleRequest(null, log, new ExecuteCallbackWithoutReq<>() {
            @Override
            protected HomePageVo doInTransactionWithoutReq() throws Exception {
                return activityService.getHomePage();
            }
        });
    }

}
