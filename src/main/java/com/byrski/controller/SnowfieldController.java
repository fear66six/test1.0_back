package com.byrski.controller;

import com.byrski.domain.entity.RestBean;
import com.byrski.domain.entity.vo.response.ActivityDateVo;
import com.byrski.domain.entity.vo.response.SnowfieldBriefVo;
import com.byrski.domain.entity.vo.response.SnowfieldDetailVo;
import com.byrski.service.SnowfieldService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/snowfield")
public class SnowfieldController extends AbstractController{

    private final SnowfieldService snowfieldService;

    public SnowfieldController (SnowfieldService snowfieldService) {
        this.snowfieldService = snowfieldService;
    }

    /**
     * 获取滑雪场简要信息列表
     *
     * @return 滑雪场简要信息列表
     */
    @GetMapping("/list")
    public RestBean<List<SnowfieldBriefVo>> list() {
        return handleRequest(null, log, new ExecuteCallbackWithoutReq<>() {
            @Override
            protected List<SnowfieldBriefVo> doInTransactionWithoutReq() throws Exception {
                return snowfieldService.listSnowfieldBrief();
            }
        });
    }

    /**
     * 获取滑雪场详细信息
     *
     * @param snowfieldId 滑雪场ID
     * @return 滑雪场详细信息
     */
    @PostMapping("/detail")
    public RestBean<SnowfieldDetailVo> detail(
            @RequestParam Long snowfieldId,
            @RequestBody(required = false) List<Long> activityIds
    ) {
        return handleRequest(snowfieldId, log, new ExecuteCallbackWithResult<>() {
            @Override
            protected SnowfieldDetailVo doInTransactionWithResult(Long snowfieldId) throws Exception {
                return snowfieldService.getSnowfieldDetail(snowfieldId, activityIds);
            }
        });
    }

    @GetMapping("/date")
    public RestBean<List<ActivityDateVo>> getActivityBeginDate(@RequestParam Long snowfieldId) {
        return handleRequest(null, log, new ExecuteCallbackWithoutReq<>() {
            @Override
            protected List<ActivityDateVo> doInTransactionWithoutReq() throws Exception {
                return snowfieldService.getActivityBeginDate(snowfieldId);
            }
        });
    }

}
