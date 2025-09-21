package com.byrski.domain.entity.dto.ticket;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.byrski.domain.entity.BaseTicketEntity;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("bus_tickets")
public class BusTicket implements BaseTicketEntity {

    /**
     * 基础票数据
     */
    @TableField(exist = false)
    private Ticket baseTicket;

    /**
     * 车票id
     */
    @TableId(value = "ticket_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ticketId;

    /**
     * 车牌号
     */
    @TableField("description")
    private String description;

}
