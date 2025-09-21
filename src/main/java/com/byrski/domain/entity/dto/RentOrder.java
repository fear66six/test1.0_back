package com.byrski.domain.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@TableName("rent_order")
public class RentOrder {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @TableField("status")
    private Integer status;
    @TableField("rent_day")
    private Integer rentDay;
    @TableField("trade_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long tradeId;
    @TableField("rent_item_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long rentItemId;
    @TableField("user_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;
    @TableField("pay_time")
    private LocalDateTime payTime;

}
