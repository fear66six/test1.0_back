package com.byrski.domain.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
@TableName("wxgroup")
public class WxGroup {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @TableField("activity_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long activityId;
    @TableField("qrcode")
    private String qrCode;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField(value = "update_time", update = "now()")
    private LocalDateTime updateTime;
}
