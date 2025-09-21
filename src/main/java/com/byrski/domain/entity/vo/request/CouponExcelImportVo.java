package com.byrski.domain.entity.vo.request;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

/**
 * Excel导入批量发放优惠券请求
 * 
 * @author ByrSki
 * @since 2024-01-01
 */
@Data
public class CouponExcelImportVo {

    /**
     * 优惠券ID
     */
    @NotNull(message = "优惠券ID不能为空")
    private Long couponId;

    /**
     * Excel文件Base64编码数据
     */
    @NotNull(message = "Excel文件数据不能为空")
    private String excelBase64Data;

    /**
     * 文件名
     */
    @NotNull(message = "文件名不能为空")
    private String fileName;

    /**
     * 发放数量（每个用户发放几张）
     */
    @NotNull(message = "发放数量不能为空")
    @Min(value = 1, message = "发放数量必须大于0")
    private Integer quantity = 1;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 是否跳过无效手机号（默认跳过）
     */
    private Boolean skipInvalidPhones = true;

    /**
     * 手机号列名（默认"手机号"）
     */
    private String phoneColumnName = "手机号";
}
