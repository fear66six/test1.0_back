package com.byrski.task;

import com.byrski.service.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 优惠券定时任务
 */
@Component
@Slf4j
public class CouponTask {

    private final CouponService couponService;

    public CouponTask(CouponService couponService) {
        this.couponService = couponService;
    }

    /**
     * 每天凌晨2点执行，更新过期优惠券状态
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void updateExpiredCoupons() {
        log.info("开始执行优惠券过期状态更新任务");
        try {
            couponService.updateExpiredCoupons();
            log.info("优惠券过期状态更新任务执行完成");
        } catch (Exception e) {
            log.error("优惠券过期状态更新任务执行失败", e);
        }
    }
}
