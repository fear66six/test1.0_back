package com.byrski.domain.entity.vo.request;

import com.byrski.domain.enums.CouponPageReceiveMode;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 优惠券批量发放请求VO V2版本
 * 支持两种页面领取模式：指定用户发放 和 全体用户抢票
 */
@Data
public class CouponBatchIssueV2Vo {

    /**
     * 优惠券ID
     */
    @NotNull(message = "优惠券ID不能为空")
    private Long couponId;

    /**
     * 发放方式：1-页面领取，2-批量导入
     */
    @NotNull(message = "发放方式不能为空")
    @Min(value = 1, message = "发放方式不正确")
    private Integer issueMethod;

    /**
     * 页面领取模式：1-指定用户发放，2-全体用户抢票
     * 当issueMethod=1时必填
     */
    private Integer pageReceiveMode;

    /**
     * 指定用户ID列表（当pageReceiveMode=1时必填）
     */
    @Size(min = 1, message = "指定用户发放时，用户ID列表不能为空")
    private List<Long> userIds;

    /**
     * 手机号列表（当pageReceiveMode=1时可选）
     */
    private List<String> phoneNumbers;

    /**
     * Excel文件Base64数据（当pageReceiveMode=1时可选）
     */
    private String excelBase64Data;

    /**
     * Excel文件名（当pageReceiveMode=1时可选）
     */
    private String fileName;

    /**
     * Excel中手机号列名（当pageReceiveMode=1时可选）
     */
    private String phoneColumnName;

    /**
     * 发放数量（当pageReceiveMode=2时必填，表示总共发放多少张）
     */
    @Min(value = 1, message = "发放数量必须大于0")
    private Integer quantity;

    /**
     * 是否需要预览（默认true）
     */
    private Boolean needPreview = true;

    /**
     * 备注
     */
    @Size(max = 200, message = "备注长度不能超过200个字符")
    private String remark;

    /**
     * 验证请求参数
     */
    public void validate() {
        if (issueMethod == 1) { // 页面领取
            if (pageReceiveMode == null) {
                throw new IllegalArgumentException("页面领取模式不能为空");
            }
            
            if (pageReceiveMode == CouponPageReceiveMode.SPECIFIC_USERS.getCode()) {
                // 指定用户发放模式
                if (userIds == null || userIds.isEmpty()) {
                    if (phoneNumbers == null || phoneNumbers.isEmpty()) {
                        if (excelBase64Data == null || excelBase64Data.isEmpty()) {
                            throw new IllegalArgumentException("指定用户发放模式需要提供用户ID列表、手机号列表或Excel文件");
                        }
                    }
                }
            } else if (pageReceiveMode == CouponPageReceiveMode.ALL_USERS_COMPETITION.getCode()) {
                // 全体用户抢票模式
                if (quantity == null || quantity <= 0) {
                    throw new IllegalArgumentException("全体用户抢票模式需要指定发放数量");
                }
            }
        }
    }
}
