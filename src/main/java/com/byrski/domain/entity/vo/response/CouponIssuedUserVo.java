package com.byrski.domain.entity.vo.response;

import com.byrski.domain.enums.UserCouponStatus;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Data;

/**
 * 优惠券已发放用户响应VO（简化版）
 */
@Data
@Builder
public class CouponIssuedUserVo {

    /**
     * 用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 用户优惠券状态
     */
    private UserCouponStatus status;

    /**
     * 发放模式
     */
    private String issueMode;
}
