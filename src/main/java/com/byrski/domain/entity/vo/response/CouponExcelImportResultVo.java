package com.byrski.domain.entity.vo.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Excel导入批量发放优惠券结果
 * 
 * @author ByrSki
 * @since 2024-01-01
 */
@Data
@Builder
public class CouponExcelImportResultVo {

    /**
     * 总处理数量
     */
    private Integer totalCount;

    /**
     * 成功发放数量
     */
    private Integer successCount;

    /**
     * 跳过数量（如已达到领取上限）
     */
    private Integer skipCount;

    /**
     * 失败数量
     */
    private Integer failCount;

    /**
     * 无效手机号数量
     */
    private Integer invalidPhoneCount;

    /**
     * 无效手机号列表
     */
    private List<String> invalidPhones;

    /**
     * 成功发放的用户列表
     */
    private List<UserIssueInfo> successUsers;

    /**
     * 跳过原因列表
     */
    private List<String> skipReasons;

    /**
     * 处理结果消息
     */
    private String message;

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
