package com.byrski.domain.entity;

import com.byrski.domain.entity.dto.ticket.BusTicket;
import com.byrski.domain.entity.dto.ticket.Ticket;
import com.byrski.domain.entity.dto.ticket.RoomTicket;
import com.byrski.domain.entity.dto.ticket.SkiTicket;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BusTicket.class, name = "BUS"),
        @JsonSubTypes.Type(value = RoomTicket.class, name = "ROOM"),
        @JsonSubTypes.Type(value = SkiTicket.class, name = "SKI")
})
public interface BaseTicketEntity {
    // 获取基础票信息
    Ticket getBaseTicket();
    
    // 设置基础票信息
    void setBaseTicket(Ticket baseTicket);
    
    // 获取票ID
    Long getTicketId();
    
    // 设置票ID
    void setTicketId(Long ticketId);
    
    // 获取最少使用人数
    default Integer getMinPeople() {
        Ticket baseTicket = getBaseTicket();
        return baseTicket != null ? baseTicket.getMinPeople() : null;
    }
    
    // 获取最多使用人数
    default Integer getMaxPeople() {
        Ticket baseTicket = getBaseTicket();
        return baseTicket != null ? baseTicket.getMaxPeople() : null;
    }
}