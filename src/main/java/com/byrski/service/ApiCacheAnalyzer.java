package com.byrski.service;

import com.byrski.common.utils.ApiStatsRedisUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ApiCacheAnalyzer {
    private static final long DEFAULT_FREQUENCY_THRESHOLD = 1000; // 每天调用次数阈值
    
    private final ApiStatsRedisUtils apiStatsRedisUtils;
    
    public ApiCacheAnalyzer(ApiStatsRedisUtils apiStatsRedisUtils) {
        this.apiStatsRedisUtils = apiStatsRedisUtils;
    }
    
    /**
     * 分析需要缓存的API
     */
    public List<CacheRecommendation> analyzeApiCacheNeeds() {
        List<ApiStatsRedisUtils.ApiCallStats> highFrequencyApis = 
            apiStatsRedisUtils.getHighFrequencyApis(DEFAULT_FREQUENCY_THRESHOLD);
            
        return highFrequencyApis.stream()
            .map(stats -> new CacheRecommendation(
                stats.getPath(),
                stats.getMethod(),
                stats.getCount(),
                calculatePriority(stats.getCount()),
                generateCacheStrategy(stats.getPath(), stats.getCount())
            ))
            .collect(Collectors.toList());
    }
    
    private int calculatePriority(long callCount) {
        if (callCount >= 10000) return 1; // 最高优先级
        if (callCount >= 5000) return 2;
        if (callCount >= 1000) return 3;
        return 4;
    }
    
    private String generateCacheStrategy(String path, long callCount) {
        StringBuilder strategy = new StringBuilder();
        if (callCount >= 10000) {
            strategy.append("建议使用本地缓存+Redis二级缓存");
        } else if (callCount >= 5000) {
            strategy.append("建议使用Redis缓存，TTL建议设置为5分钟");
        } else {
            strategy.append("建议使用Redis缓存，TTL建议设置为15分钟");
        }
        return strategy.toString();
    }
    
    @Data
    @AllArgsConstructor
    public static class CacheRecommendation {
        private String path;
        private String method;
        private long dailyCallCount;
        private int priority;
        private String cacheStrategy;
    }
}