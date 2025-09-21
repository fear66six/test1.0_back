package com.byrski.domain.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@TableName("bus_move")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusMove {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @TableField("station_peoplenum")
    private Integer stationPeopleNum;

    @TableField("remain_num")
    private Integer remainNum;

    @TableField("time")
    private LocalDateTime time;

    @TableField("bus_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long busId;

    @TableField("station_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long stationId;

    @TableField("activity_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long activityId;

    @TableField("go_finished")
    private Boolean goFinished;
}
