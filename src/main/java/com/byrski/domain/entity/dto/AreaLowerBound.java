package com.byrski.domain.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@TableName("area_lower_bound")
public class AreaLowerBound {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @TableField("lower_limit")
    private Integer lowerLimit;
    @TableField("activity_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long activityId;
    @TableField("area_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long areaId;
}
