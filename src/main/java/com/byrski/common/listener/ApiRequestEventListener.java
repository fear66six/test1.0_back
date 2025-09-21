package com.byrski.common.listener;

import com.byrski.common.utils.ApiStatsRedisUtils;
import com.byrski.domain.entity.ApiRequestEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ApiRequestEventListener {
    
    private final ApiStatsRedisUtils apiStatsRedisUtils;
    
    public ApiRequestEventListener(ApiStatsRedisUtils apiStatsRedisUtils) {
        this.apiStatsRedisUtils = apiStatsRedisUtils;
    }
    
    @Async
    @EventListener
    public void handleApiRequest(ApiRequestEvent event) {
        apiStatsRedisUtils.recordApiCall(event.getPath(), event.getMethod());
    }
}