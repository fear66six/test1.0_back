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
import java.util.List;

@Data
@Builder
@TableName("activity_template")
public class ActivityTemplate {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @TableField("duration_days")
    private Integer durationDays;

    @Deprecated
    @TableField("notes")
    private String notes;

    @Deprecated
    @TableField("schedule_lite")
    private String scheduleLite;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField(value = "update_time", update = "now()")
    private LocalDateTime updateTime;

    @TableField("snowfield_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long snowfieldId;

    @TableField("name")
    private String name;
}