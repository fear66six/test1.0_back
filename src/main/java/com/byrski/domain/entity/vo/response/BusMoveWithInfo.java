package com.byrski.domain.entity.vo.response;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BusMoveWithInfo {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private Integer stationPeopleNum;
    private LocalDateTime time;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long busId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long stationId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long activityId;
    private Boolean goFinished;
    private String school;
    private String campus;
    private String location;
}
