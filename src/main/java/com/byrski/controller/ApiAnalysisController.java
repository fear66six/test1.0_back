package com.byrski.controller;

import com.byrski.common.utils.ApiStatsRedisUtils;
import com.byrski.domain.entity.RestBean;
import com.byrski.service.ApiCacheAnalyzer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
public class ApiAnalysisController {
    
    private final ApiCacheAnalyzer apiCacheAnalyzer;
    private final ApiStatsRedisUtils apiStatsRedisUtils;
    
    public ApiAnalysisController(ApiCacheAnalyzer apiCacheAnalyzer, ApiStatsRedisUtils apiStatsRedisUtils) {
        this.apiCacheAnalyzer = apiCacheAnalyzer;
        this.apiStatsRedisUtils = apiStatsRedisUtils;
    }
    
    @GetMapping("/cache-recommendations")
    public RestBean<List<ApiCacheAnalyzer.CacheRecommendation>> getCacheRecommendations() {
        return RestBean.success(apiCacheAnalyzer.analyzeApiCacheNeeds());
    }

    @GetMapping("/daily")
    public RestBean<Map<String, Long>> getDailyStats(@RequestParam LocalDate date) {
        return RestBean.success(apiStatsRedisUtils.getDailyStats(date));
    }

    @GetMapping("/total")
    public RestBean<Map<String, Long>> getTotalStats() {
        return RestBean.success(apiStatsRedisUtils.getTotalStats());
    }
}