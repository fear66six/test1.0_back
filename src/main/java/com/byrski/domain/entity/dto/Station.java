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
@TableName("station")
@Builder
public class Station {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @TableField("choice_peoplenum")
    private Integer choicePeopleNum;
    @TableField("target_peoplenum")
    private Integer targetPeopleNum;
    @TableField("activity_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long activityId;
    @TableField("station_info_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long stationInfoId;
    @TableField("area_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long areaId;
    @TableField("status")
    private Integer status;
}
