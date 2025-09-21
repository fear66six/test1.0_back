package com.byrski.domain.entity.vo.request;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 优惠券批量发放请求VO
 */
@Data
public class CouponBatchIssueVo {

    /**
     * 优惠券ID
     */
    @NotNull(message = "优惠券ID不能为空")
    private Long couponId;

    /**
     * 发放方式：1-页面领取，2-批量导入
     */
    private Integer issueMethod = 2; // 默认批量导入

    /**
     * 页面领取模式：1-指定用户发放，2-全体用户抢票
     * 仅当 issueMethod 为 1 (页面领取) 时有效
     */
    private Integer pageReceiveMode;

    /**
     * 用户ID列表（用于后台勾选用户）
     */
    private List<Long> userIds;

    /**
     * 手机号列表（用于批量导入）
     */
    private List<String> phoneNumbers;

    /**
     * Excel Base64 数据（可选，与 phoneNumbers 合并使用）
     */
    private String excelBase64Data;

    /**
     * Excel 文件名（可选）
     */
    private String fileName;

    /**
     * Excel 手机号列名（可选，默认“手机号”）
     */
    private String phoneColumnName;

    /**
     * 发放数量
     */
    @NotNull(message = "发放数量不能为空")
    private Integer quantity = 1;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 是否需要预览：true-创建预览，false-直接发放
     */
    private Boolean needPreview = true;
}
