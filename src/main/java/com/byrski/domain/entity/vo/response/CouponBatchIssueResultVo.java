package com.byrski.domain.entity.vo.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 优惠券批量发放结果响应VO
 */
@Data
@Builder
public class CouponBatchIssueResultVo {

    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 优惠券名称
     */
    private String couponName;

    /**
     * 成功发放数量
     */
    private Integer successCount;

    /**
     * 跳过数量（已达到领取上限等）
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
     * 跳过的原因列表
     */
    private List<String> skipReasons;

    /**
     * 成功发放的用户信息
     */
    private List<UserIssueInfo> successUsers;

    /**
     * 用户发放信息
     */
    @Data
    @Builder
    public static class UserIssueInfo {
        /**
         * 用户ID
         */
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
         * 发放时间
         */
        private String issueTime;
    }
}
