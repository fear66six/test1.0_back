package com.byrski.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * 优惠券事件基类
 */
@Getter
public abstract class CouponEvent extends ApplicationEvent {
    
    private final Long couponId;
    private final Long userId;
    private final LocalDateTime eventTimestamp;
    
    protected CouponEvent(Object source, Long couponId, Long userId) {
        super(source);
        this.couponId = couponId;
        this.userId = userId;
        this.eventTimestamp = LocalDateTime.now();
    }
    
    /**
     * 优惠券领取事件
     */
    @Getter
    public static class CouponReceivedEvent extends CouponEvent {
        private final String couponName;
        
        public CouponReceivedEvent(Object source, Long couponId, Long userId, String couponName) {
            super(source, couponId, userId);
            this.couponName = couponName;
        }
    }
    
    /**
     * 优惠券使用事件
     */
    @Getter
    public static class CouponUsedEvent extends CouponEvent {
        private final Long tradeId;
        private final Double discountAmount;
        
        public CouponUsedEvent(Object source, Long couponId, Long userId, Long tradeId, Double discountAmount) {
            super(source, couponId, userId);
            this.tradeId = tradeId;
            this.discountAmount = discountAmount;
        }
    }
    
    /**
     * 优惠券退还事件
     */
    @Getter
    public static class CouponRefundedEvent extends CouponEvent {
        private final Long tradeId;
        
        public CouponRefundedEvent(Object source, Long couponId, Long userId, Long tradeId) {
            super(source, couponId, userId);
            this.tradeId = tradeId;
        }
    }
    
    /**
     * 优惠券过期事件
     */
    @Getter
    public static class CouponExpiredEvent extends CouponEvent {
        public CouponExpiredEvent(Object source, Long couponId, Long userId) {
            super(source, couponId, userId);
        }
    }
}
