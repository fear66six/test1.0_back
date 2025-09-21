package com.byrski.common.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.byrski.common.utils.Const;

@Component
@Slf4j
public class ApiStatsRedisUtils {

    private final RedisUtils redisUtils;

    public ApiStatsRedisUtils(RedisUtils redisUtils) {
        this.redisUtils = redisUtils;
    }

    /**
     * 记录API调用
     */
    public void recordApiCall(String path, String method) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String dailyKey = getDailyKey(today);
        String pathKey = getPathKey(path, method);

        try {
            // 使用Redis事务确保操作的原子性
            redisUtils.executeInTransaction(template -> {
                // 增加当天统计
                redisUtils.hincrBy(dailyKey, pathKey, 1);
                // 增加总计统计
                redisUtils.hincrBy(Const.API_TOTAL_STATS_KEY, pathKey, 1);
            });

            // 设置过期时间
            redisUtils.expire(dailyKey, Const.STATS_EXPIRE_DAYS * 24 * 60 * 60);
        } catch (Exception e) {
            log.error("Failed to record API call: path={}, method={}", path, method, e);
        }
    }

    /**
     * 获取某天的API调用统计
     */
    public Map<String, Long> getDailyStats(LocalDate date) {
        String dailyKey = getDailyKey(date.format(DateTimeFormatter.ISO_DATE));
        return convertToLongMap(redisUtils.hgetAll(dailyKey));
    }

    /**
     * 获取API总调用统计
     */
    public Map<String, Long> getTotalStats() {
        return convertToLongMap(redisUtils.hgetAll(Const.API_TOTAL_STATS_KEY));
    }

    /**
     * 获取指定API的调用次数
     */
    public long getApiCallCount(String path, String method) {
        String pathKey = getPathKey(path, method);
        String count = redisUtils.hget(Const.API_TOTAL_STATS_KEY, pathKey);
        return count != null ? Long.parseLong(count) : 0;
    }

    /**
     * 获取高频调用的API列表
     */
    public List<ApiCallStats> getHighFrequencyApis(long threshold) {
        Map<Object, Object> totalStats = redisUtils.hgetAll(Const.API_TOTAL_STATS_KEY);
        return totalStats.entrySet().stream()
            .map(entry -> {
                String[] parts = entry.getKey().toString().split(":");
                long count = Long.parseLong(entry.getValue().toString());
                return new ApiCallStats(parts[0], parts[1], count);
            })
            .filter(stats -> stats.getCount() >= threshold)
            .sorted(Comparator.comparingLong(ApiCallStats::getCount).reversed())
            .collect(Collectors.toList());
    }

    private String getDailyKey(String date) {
        return Const.API_DAILY_STATS_KEY + date;
    }

    private String getPathKey(String path, String method) {
        return path + ":" + method;
    }

    private Map<String, Long> convertToLongMap(Map<Object, Object> source) {
        Map<String, Long> result = new HashMap<>();
        source.forEach((k, v) -> result.put(k.toString(), Long.parseLong(v.toString())));
        return result;
    }

    @Data
    @AllArgsConstructor
    public static class ApiCallStats {
        private String path;
        private String method;
        private long count;
    }
}
