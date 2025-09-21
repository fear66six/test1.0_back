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

@Data
@Builder
@TableName("room")
public class Room {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @TableField("coed")
    private Boolean coed;
    @TableField("name")
    private String name;
    @TableField("people_num")
    private Integer peopleNum;
    @TableField("max_people_num")
    private Integer maxPeopleNum;
    @TableField("number")
    private Integer number;
    @TableField("code")
    private String code;
    @TableField("trade_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long tradeId;
    @TableField("activity_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long activityId;
    @TableField("owner_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ownerId;
    @TableField("owner_name")
    private String ownerName;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;
    @TableField("ticket_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ticketId;
}
