package com.byrski.domain.entity.vo.response;

import com.byrski.common.utils.PriceSerializer;
import com.byrski.domain.enums.CouponType;
import com.byrski.domain.enums.UserCouponStatus;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户优惠券响应VO
 */
@Data
@Builder
public class UserCouponVo {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long couponId;

    private UserCouponStatus status;

    private LocalDateTime receiveTime;

    private LocalDateTime useTime;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long tradeId;

    private Double discountAmount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String remark;

    // 优惠券信息
    private String couponName;
    private String couponDescription;
    private CouponType couponType;
    private Double couponDiscountValue;
    private LocalDateTime couponStartTime;
    private LocalDateTime couponEndTime;
    
    private Double minAmount;

    // 订单信息
    private String tradeOutTradeNo;
    private String productName;
}
