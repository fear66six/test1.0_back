package com.byrski.domain.entity.vo.request;

import com.byrski.domain.enums.CouponType;
import lombok.Data;

import jakarta.validation.constraints.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.byrski.common.utils.PriceDeserializer;
import java.time.LocalDateTime;

/**
 * 优惠券添加请求VO
 */
@Data
public class CouponAddVo {

    /**
     * 优惠券名称
     */
    @NotBlank(message = "优惠券名称不能为空")
    @Size(max = 100, message = "优惠券名称长度不能超过100个字符")
    private String name;

    /**
     * 优惠券描述
     */
    @Size(max = 500, message = "优惠券描述长度不能超过500个字符")
    private String description;

    /**
     * 优惠券类型：0-百分比打折，1-固定金额优惠
     */
    @NotNull(message = "优惠券类型不能为空")
    private CouponType type;

    /**
     * 折扣值：百分比打折时为折扣百分比(1-90)，固定金额优惠时为优惠金额(元)
     */
    @NotNull(message = "折扣值不能为空")
    @DecimalMin(value = "0.01", message = "折扣值必须大于0")
    @DecimalMax(value = "90.0", message = "折扣值不能超过90%")
    private Double discountValue;

    /**
     * 使用门槛金额(元)，0表示无门槛
     */
    @DecimalMin(value = "0.0", message = "使用门槛金额不能为负数")
    private Double minAmount = 0.0;

    /**
     * 使用门槛人数，0表示无门槛
     */
    @Min(value = 0, message = "使用门槛人数不能为负数")
    private Integer minParticipants = 0;

    /**
     * 适用产品ID，null表示适用于所有产品
     */
    private String productId;

    /**
     * 适用活动模板ID，null表示适用于所有活动模板
     */
    private Long activityTemplateId;

    /**
     * 适用雪场ID，null表示适用于所有雪场
     */
    private Long snowfieldId;

    /**
     * 发放方式：1-页面领取，2-批量导入
     */
    @NotNull(message = "发放方式不能为空")
    @Min(value = 1, message = "发放方式不正确")
    @Max(value = 2, message = "发放方式不正确")
    private Integer issueMethod;

    /**
     * 页面领取模式：1-指定用户发放，2-全体用户抢票（仅当发放方式为页面领取时有效）
     */
    private Integer pageReceiveMode;

    /**
     * 发放数量限制，0表示无限制
     */
    @Min(value = 0, message = "发放数量限制不能为负数")
    private Integer issueLimit = 0;

    /**
     * 每人限领数量，0表示无限制
     */
    @Min(value = 0, message = "每人限领数量不能为负数")
    private Integer perUserLimit = 0;

    /**
     * 生效时间
     */
    @NotNull(message = "生效时间不能为空")
    private LocalDateTime startTime;

    /**
     * 失效时间
     */
    @NotNull(message = "失效时间不能为空")
    private LocalDateTime endTime;

    /**
     * 备注
     */
    @Size(max = 200, message = "备注长度不能超过200个字符")
    private String remark;
}
