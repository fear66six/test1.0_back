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

@TableName("bus")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bus {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @TableField("car_number")
    private String carNumber;

    @TableField("carry_people")
    private Integer carryPeople;

    @TableField("max_people")
    private Integer maxPeople;

    @TableField("route")
    private String route;

    @TableField("activity_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long activityId;

    @TableField("leader_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long leaderId;

    @TableField("driver_phone")
    private String driverPhone;

    @TableField("arrival_time")
    private LocalDateTime arrivalTime;

    @TableField("status")
    private Integer status;

    @TableField("name")
    private String name;
}
