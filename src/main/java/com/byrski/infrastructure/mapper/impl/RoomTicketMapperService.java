package com.byrski.infrastructure.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byrski.domain.entity.dto.ticket.RoomTicket;
import com.byrski.infrastructure.mapper.RoomTicketMapper;
import org.springframework.stereotype.Component;

@Component
public class RoomTicketMapperService extends ServiceImpl<RoomTicketMapper, RoomTicket> {
    public RoomTicket getByTicketId(Long ticketId) {
        return this.lambdaQuery().eq(RoomTicket::getTicketId, ticketId).one();
    }
}
