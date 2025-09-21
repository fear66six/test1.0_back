package com.byrski.common.validator;


import com.byrski.common.exception.ByrSkiException;
import com.byrski.domain.entity.dto.Coupon;
import com.byrski.domain.entity.dto.UserCoupon;
import com.byrski.domain.enums.CouponStatus;
import com.byrski.domain.enums.CouponType;
import com.byrski.domain.enums.ReturnCode;
import com.byrski.domain.enums.UserCouponStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 优惠券验证器
 * 统一处理优惠券相关的验证逻辑
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CouponValidator {
    

    
    /**
     * 验证优惠券基本信息
     * @param coupon 优惠券
     */
    public void validateCouponBasic(Coupon coupon) {
        if (coupon == null) {
            throw new ByrSkiException(ReturnCode.COUPON_NOT_EXIST);
        }
        
        // 验证优惠券状态
        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            throw new ByrSkiException(ReturnCode.COUPON_INACTIVE);
        }
        
        // 验证时间有效性
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartTime()) || now.isAfter(coupon.getEndTime())) {
            throw new ByrSkiException(ReturnCode.COUPON_EXPIRED);
        }
    }
    
    /**
     * 验证优惠券使用条件
     * @param coupon 优惠券
     * @param amount 订单金额
     * @param participants 参与人数
     */
    public void validateCouponUsageConditions(Coupon coupon, Double amount, Integer participants) {
        // 验证金额门槛
        if (coupon.getMinAmount() != null && coupon.getMinAmount() > 0) {
            if (amount < coupon.getMinAmount()) {
                log.warn("优惠券金额门槛验证失败，优惠券ID: {}, 要求金额: {}, 实际金额: {}", 
                    coupon.getId(), coupon.getMinAmount(), amount);
                throw new ByrSkiException(ReturnCode.COUPON_THRESHOLD_NOT_MET);
            }
        }
        
        // 验证参与人数门槛
        if (coupon.getMinParticipants() != null && coupon.getMinParticipants() > 0) {
            if (participants < coupon.getMinParticipants()) {
                log.warn("优惠券参与人数门槛验证失败，优惠券ID: {}, 要求人数: {}, 实际人数: {}", 
                    coupon.getId(), coupon.getMinParticipants(), participants);
                throw new ByrSkiException(ReturnCode.COUPON_THRESHOLD_NOT_MET);
            }
        }
    }
    
    /**
     * 验证用户优惠券状态
     * @param userCoupon 用户优惠券
     * @param userId 用户ID
     */
    public void validateUserCouponStatus(UserCoupon userCoupon, Long userId) {
        if (userCoupon == null) {
            throw new ByrSkiException(ReturnCode.USER_COUPON_NOT_EXIST);
        }
        
        // 验证用户优惠券状态
        if (userCoupon.getStatus() != UserCouponStatus.UNUSED) {
            log.warn("用户优惠券状态验证失败，用户优惠券ID: {}, 状态: {}", 
                userCoupon.getId(), userCoupon.getStatus());
            throw new ByrSkiException(ReturnCode.COUPON_ALREADY_USED);
        }
        
        // 验证用户ID是否匹配
        if (!userCoupon.getUserId().equals(userId)) {
            log.warn("用户优惠券用户ID验证失败，用户优惠券ID: {}, 优惠券用户ID: {}, 当前用户ID: {}", 
                userCoupon.getId(), userCoupon.getUserId(), userId);
            throw new ByrSkiException(ReturnCode.FORBIDDEN);
        }
    }
    
    /**
     * 验证优惠券产品适用性
     * @param coupon 优惠券
     * @param productId 产品ID
     */
    public void validateCouponProductApplicability(Coupon coupon, String productId) {
        if (coupon.getProductId() != null && !coupon.getProductId().equals(productId)) {
            log.warn("优惠券产品适用性验证失败，优惠券ID: {}, 优惠券适用产品: {}, 订单产品: {}", 
                coupon.getId(), coupon.getProductId(), productId);
            throw new ByrSkiException(ReturnCode.COUPON_PRODUCT_NOT_MATCH);
        }
    }
    
    /**
     * 验证优惠券折扣值
     * @param type 优惠券类型
     * @param discountValue 折扣值
     */
    public void validateDiscountValue(CouponType type, Double discountValue) {
        if (type == CouponType.PERCENTAGE) {
            if (discountValue < 0.01 || discountValue > 90.0) {
                throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), 
                    "百分比折扣值必须在0.01-90之间");
            }
        } else if (type == CouponType.FIXED_AMOUNT) {
            if (discountValue < 0.01 || discountValue > 1000.0) {
                throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), 
                    "固定金额优惠值必须在0.01-1000之间");
            }
        }
    }
    
    /**
     * 验证优惠券时间设置
     * @param startTime 开始时间
     * @param endTime 结束时间
     */
    public void validateCouponTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime.isAfter(endTime)) {
            throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "生效时间不能晚于失效时间");
        }
        
        // 验证有效期是否超过最大限制
        long daysBetween = java.time.Duration.between(startTime, endTime).toDays();
        if (daysBetween > 365) {
            throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), 
                "优惠券有效期不能超过365天");
        }
    }
    
    /**
     * 验证优惠券发放限制
     * @param coupon 优惠券
     * @param quantity 发放数量
     */
    public void validateIssueLimit(Coupon coupon, Integer quantity) {
        if (coupon.getIssueLimit() > 0 && 
            coupon.getIssuedCount() + quantity > coupon.getIssueLimit()) {
            log.warn("优惠券发放数量限制验证失败，优惠券ID: {}, 已发放: {}, 本次发放: {}, 限制: {}", 
                coupon.getId(), coupon.getIssuedCount(), quantity, coupon.getIssueLimit());
            throw new ByrSkiException(ReturnCode.COUPON_QUANTITY_EXCEEDED);
        }
    }
    
    /**
     * 验证用户领取限制
     * @param currentCount 当前领取数量
     * @param perUserLimit 每人限制
     */
    public void validateUserReceiveLimit(Long currentCount, Integer perUserLimit) {
        if (perUserLimit != null && perUserLimit > 0 && currentCount >= perUserLimit) {
            log.warn("用户优惠券领取限制验证失败，当前领取: {}, 限制: {}", currentCount, perUserLimit);
            throw new ByrSkiException(ReturnCode.COUPON_QUANTITY_EXCEEDED.getCode(), 
                "您已达到该优惠券的领取上限");
            }
        }
    }
