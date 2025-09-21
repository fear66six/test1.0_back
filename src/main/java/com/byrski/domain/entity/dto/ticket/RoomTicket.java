package com.byrski.domain.entity.dto.ticket;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.byrski.domain.entity.BaseTicketEntity;
import com.byrski.domain.enums.RoomType;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("room_tickets")
public class RoomTicket implements BaseTicketEntity {

    /**
     * 基础票数据
     */
    @TableField(exist = false)
    private Ticket baseTicket;

    /**
     * 房票id
     */
    @TableId(value = "ticket_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ticketId;

    /**
     * 房间类型
     */
    @TableField("type")
    @EnumValue
    private RoomType roomType;

    /**
     * 房间描述
     */
    @TableField("description")
    private String description;

    /**
     * 房间最大人数
     */
    @TableField("max_people_num")
    private Integer maxPeopleNum;

    /**
     * 是否支持混住
     */
    @TableField("coed")
    private Boolean coed;

}
