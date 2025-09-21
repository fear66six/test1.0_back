package com.byrski.domain.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 优惠券用户可领取关联表
 * 用于存储页面领取类型优惠券与用户的关联关系
 */
@Data
@TableName("coupon_user_receivable")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CouponUserReceivable {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 优惠券ID
     */
    @TableField("coupon_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long couponId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /**
     * 发放模式：1-指定用户发放，2-全体用户抢票
     */
    @TableField("receive_mode")
    private Integer receiveMode;

    /**
     * 是否已领取
     */
    @TableField("is_received")
    private Boolean isReceived;

    /**
     * 领取时间
     */
    @TableField("receive_time")
    private LocalDateTime receiveTime;

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
     * 备注
     */
    @TableField("remark")
    private String remark;
}
