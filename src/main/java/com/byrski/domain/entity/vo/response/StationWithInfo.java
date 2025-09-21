package com.byrski.domain.entity.vo.response;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StationWithInfo {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private Integer choicePeopleNum;
    private Integer targetPeopleNum;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long activityId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long stationInfoId;
    private String school;
    private String campus;
    private String location;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long areaId;
    private Integer status;
}
