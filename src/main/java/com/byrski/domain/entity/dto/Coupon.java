package com.byrski.domain.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.byrski.domain.enums.CouponStatus;
import com.byrski.domain.enums.CouponType;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 优惠券实体类
 */
@Data
@TableName("coupon")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Coupon {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 优惠券名称
     */
    @TableField("name")
    private String name;

    /**
     * 优惠券描述
     */
    @TableField("description")
    private String description;

    /**
     * 优惠券类型：0-百分比打折，1-固定金额优惠
     */
    @TableField("type")
    private CouponType type;

    /**
     * 折扣值：
     * - 百分比打折时：折扣百分比(1-90)，支持小数
     * - 固定金额优惠时：优惠金额(元)，支持小数
     */
    @TableField("discount_value")
    private Double discountValue;

    /**
     * 使用门槛金额(元)，0表示无门槛
     */
    @TableField("min_amount")
    private Double minAmount;

    /**
     * 使用门槛人数，0表示无门槛
     */
    @TableField("min_participants")
    private Integer minParticipants;

    /**
     * 适用产品ID，null表示适用于所有产品
     */
    @TableField("product_id")
    private String productId;

    /**
     * 适用活动模板ID，null表示适用于所有活动模板
     */
    @TableField("activity_template_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long activityTemplateId;

    /**
     * 适用雪场ID，null表示适用于所有雪场
     */
    @TableField("snowfield_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long snowfieldId;

    /**
     * 优惠券状态：0-草稿，1-已启用，2-已停用，3-已过期，4-已删除
     */
    @TableField("status")
    private CouponStatus status;

    /**
     * 发放方式：0-页面领取，1-批量导入
     */
    @TableField("issue_method")
    private Integer issueMethod;

    /**
     * 页面领取模式：1-指定用户发放，2-全体用户抢票（仅当发放方式为页面领取时有效）
     */
    @TableField("page_receive_mode")
    private Integer pageReceiveMode;

    /**
     * 发放数量限制，0表示无限制
     */
    @TableField("issue_limit")
    private Integer issueLimit;

    /**
     * 已发放数量
     */
    @TableField("issued_count")
    private Integer issuedCount;

    /**
     * 每人限领数量，0表示无限制
     */
    @TableField("per_user_limit")
    private Integer perUserLimit;

    /**
     * 生效时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 失效时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", update = "now()")
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    @TableField("creator_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long creatorId;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}
