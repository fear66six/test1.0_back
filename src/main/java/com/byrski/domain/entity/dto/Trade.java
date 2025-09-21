package com.byrski.domain.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.byrski.common.utils.PriceSerializer;
import com.byrski.domain.enums.ProductType;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@TableName("trade")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Trade {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @TableField("out_trade_no")
    private String outTradeNo;

    @TableField("type")
    private ProductType type;

    @TableField("go_boarded")
    private Boolean goBoarded;

    @TableField("return_boarded")
    private Boolean returnBoarded;

    @TableField("tutorial_id")
    private Long tutorialId;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField(value = "update_time", update = "now()")
    private LocalDateTime updateTime;

    @TableField("pay_time")
    private LocalDateTime payTime;

    @TableField("status")
    private Integer status;

    @TableField("bus_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long busId;

    @TableField("station_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long stationId;

    @TableField("bus_move_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long busMoveId;

    @TableField("product_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private String productId;

    @TableField("activity_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long activityId;

    @TableField("activity_template_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long activityTemplateId;

    @TableField("snowfield_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long snowfieldId;

    @TableField("user_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    @TableField("wxgroup_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wxgroupId;

    @TableField("room_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long roomId;

    @TableField("total")
    @JsonSerialize(using = PriceSerializer.class)
    private Integer total;

    @TableField("ticket_check")
    private Boolean ticketCheck;

    @TableField("cost_rent")
    @JsonSerialize(using = PriceSerializer.class)
    private Integer costRent;

    @TableField("cost_ticket")
    @JsonSerialize(using = PriceSerializer.class)
    private Integer costTicket;

    @TableField("user_count")
    private Integer userCount;

    @TableField("user_coupon_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userCouponId;

    @TableField("discount_amount")
    @JsonSerialize(using = PriceSerializer.class)
    private Integer discountAmount;

    @TableField("coupon_type")
    private String couponType;

    @TableField("coupon_name")
    private String couponName;

    @TableField("original_amount")
    @JsonSerialize(using = PriceSerializer.class)
    private Integer originalAmount;

    @TableField("final_amount")
    @JsonSerialize(using = PriceSerializer.class)
    private Integer finalAmount;

    @TableField("order_group_id")
    private String orderGroupId;

    @TableField("user_sequence")
    private Integer userSequence;

    @TableField("ski_choice")
    private Integer skiChoice;

//
//    public Trade (String outTradeNo, Long stationId, String productId, Long activityId, Long activityTemplateId, Long snowfieldId, Integer status, Integer total, Long userId, Long wxgroupId) {
//        this.outTradeNo = outTradeNo;
//        this.stationId = stationId;
//        this.productId = productId;
//        this.activityId = activityId;
//        this.activityTemplateId = activityTemplateId;
//        this.snowfieldId = snowfieldId;
//        this.status = status;
//        this.total = total;
//        this.userId = userId;
//        this.createTime = LocalDateTime.now();
//        this.updateTime = LocalDateTime.now();
//        this.wxgroupId = wxgroupId;
//    }
}