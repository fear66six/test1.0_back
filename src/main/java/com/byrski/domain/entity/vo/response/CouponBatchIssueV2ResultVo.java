package com.byrski.domain.entity.vo.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 优惠券批量发放结果VO V2版本
 */
@Data
@Builder
public class CouponBatchIssueV2ResultVo {

    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 优惠券名称
     */
    private String couponName;

    /**
     * 发放模式
     */
    private Integer pageReceiveMode;

    /**
     * 成功发放数量
     */
    private Integer successCount;

    /**
     * 跳过数量
     */
    private Integer skipCount;

    /**
     * 无效用户ID数量
     */
    private Integer invalidUserIdCount;

    /**
     * 无效手机号数量
     */
    private Integer invalidPhoneCount;

    /**
     * 无效用户ID列表
     */
    private List<String> invalidUserIds;

    /**
     * 无效手机号列表
     */
    private List<String> invalidPhoneNumbers;

    /**
     * 跳过原因列表
     */
    private List<String> skipReasons;

    /**
     * 成功发放的用户信息列表
     */
    private List<UserIssueInfo> successUsers;

    /**
     * 备注
     */
    private String remark;

    /**
     * 用户发放信息
     */
    @Data
    @Builder
    public static class UserIssueInfo {
        private Long userId;
        private String username;
        private String phone;
        private String remark;
    }
}
