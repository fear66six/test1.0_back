package com.byrski.domain.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("leader_itinerary")
public class LeaderItinerary {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("bus_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long busId;

    @TableField("station_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long stationId;

    @TableField("leader_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long leaderId;

}
