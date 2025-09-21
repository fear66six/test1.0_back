package com.byrski.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import static com.byrski.common.utils.Const.*;

@Slf4j
@Component
public class IpBlacklistManager {

    @Resource
    private RedisUtils redisUtils;

    @Value("${security.ip.max-requests-per-minute:600}")
    private int maxRequestsPerMinute;

    @Value("${security.ip.block-duration-minutes:30}")
    private int blockDurationMinutes;

    /**
     * 检查IP是否被拉黑
     */
    public boolean isIpBlocked(String ip) {
        String blacklistKey = IP_BLACKLIST_KEY + ip;
        String blockedKey = IP_BLOCKED_KEY + ip;
        
        return Boolean.TRUE.equals(redisUtils.exist(blacklistKey)) 
            || Boolean.TRUE.equals(redisUtils.exist(blockedKey));
    }

    /**
     * 记录IP访问并检查是否需要临时拉黑
     */
    public boolean checkAndRecordIpRequest(String ip) {
        String countKey = IP_REQUEST_COUNT_KEY + ip;
        
        // 增加访问计数
        Long count = redisUtils.incr(countKey);
        if (count == 1) {
            // 设置1分钟过期
            redisUtils.expire(countKey, 60);
        }

        // 检查是否超过访问限制
        if (count > maxRequestsPerMinute) {
            blockIpTemporarily(ip);
            return false;
        }
        
        return true;
    }

    /**
     * 临时拉黑IP
     */
    public void blockIpTemporarily(String ip) {
        String blockedKey = IP_BLOCKED_KEY + ip;
        redisUtils.set(blockedKey, "1", blockDurationMinutes * 60L);
        log.warn("IP {} has been temporarily blocked for {} minutes due to excessive requests", 
                ip, blockDurationMinutes);
    }

    /**
     * 永久拉黑IP
     */
    public void blacklistIp(String ip) {
        String blacklistKey = IP_BLACKLIST_KEY + ip;
        redisUtils.set(blacklistKey, "1");
        log.warn("IP {} has been permanently blacklisted", ip);
    }

    /**
     * 解除IP拉黑
     */
    public void removeFromBlacklist(String ip) {
        String blacklistKey = IP_BLACKLIST_KEY + ip;
        String blockedKey = IP_BLOCKED_KEY + ip;
        
        redisUtils.delete(blacklistKey);
        redisUtils.delete(blockedKey);
        log.info("IP {} has been removed from blacklist", ip);
    }
}