package com.byrski.domain.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("snowfield")
public class Snowfield {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("location")
    private String location;

    @TableField("opening_time")
    private LocalTime openingTime;

    @TableField("closing_time")
    private LocalTime closingTime;

    @TableField("phone")
    private String phone;

    @TableField("intro")
    private String intro;

    @TableField("cover")
    private String cover;

    @TableField("slope")
    private String slope;

    @TableField("detail")
    private String detail;

    @TableField("detailpic")
    private String detailpic;

    @TableField("website")
    private String website;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField(value = "update_time", update = "now()")
    private LocalDateTime updateTime;

    @TableField("area_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long areaId;
}