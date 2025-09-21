package com.byrski.common.listener;

import com.byrski.common.event.CouponEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 优惠券事件监听器
 * 处理优惠券相关的事件，如领取、使用、退还、过期等
 */
@Slf4j
@Component
public class CouponEventListener {
    
    /**
     * 处理优惠券领取事件
     */
    @EventListener
    @Async
    public void handleCouponReceived(CouponEvent.CouponReceivedEvent event) {
        log.info("处理优惠券领取事件 - 优惠券ID: {}, 用户ID: {}, 优惠券名称: {}, 时间: {}", 
            event.getCouponId(), event.getUserId(), event.getCouponName(), event.getEventTimestamp());
        
        // 这里可以添加业务逻辑，如：
        // 1. 发送通知给用户
        // 2. 记录用户行为日志
        // 3. 更新统计数据
        // 4. 触发其他业务流程
    }
    
    /**
     * 处理优惠券使用事件
     */
    @EventListener
    @Async
    public void handleCouponUsed(CouponEvent.CouponUsedEvent event) {
        log.info("处理优惠券使用事件 - 优惠券ID: {}, 用户ID: {}, 订单ID: {}, 优惠金额: {}, 时间: {}", 
            event.getCouponId(), event.getUserId(), event.getTradeId(), event.getDiscountAmount(), event.getEventTimestamp());
        
        // 这里可以添加业务逻辑，如：
        // 1. 更新优惠券使用统计
        // 2. 记录交易日志
        // 3. 触发营销活动
        // 4. 发送使用确认通知
    }
    
    /**
     * 处理优惠券退还事件
     */
    @EventListener
    @Async
    public void handleCouponRefunded(CouponEvent.CouponRefundedEvent event) {
        log.info("处理优惠券退还事件 - 优惠券ID: {}, 用户ID: {}, 订单ID: {}, 时间: {}", 
            event.getCouponId(), event.getUserId(), event.getTradeId(), event.getEventTimestamp());
        
        // 这里可以添加业务逻辑，如：
        // 1. 更新优惠券状态
        // 2. 记录退还原因
        // 3. 发送退还确认通知
        // 4. 更新统计数据
    }
    
    /**
     * 处理优惠券过期事件
     */
    @EventListener
    @Async
    public void handleCouponExpired(CouponEvent.CouponExpiredEvent event) {
        log.info("处理优惠券过期事件 - 优惠券ID: {}, 用户ID: {}, 时间: {}", 
            event.getCouponId(), event.getUserId(), event.getEventTimestamp());
        
        // 这里可以添加业务逻辑，如：
        // 1. 更新优惠券状态为过期
        // 2. 发送过期提醒通知
        // 3. 清理过期数据
        // 4. 更新统计数据
    }
}
