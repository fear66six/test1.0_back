package com.byrski.domain.entity.vo.response;

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
public class AreaLowerBoundWithArea {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private Integer lowerLimit;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long activityId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long areaId;
    private String areaName;
    private String cityName;
}