package com.byrski.infrastructure.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byrski.domain.entity.dto.ticket.BusTicket;
import com.byrski.infrastructure.mapper.BusTicketMapper;
import org.springframework.stereotype.Component;

@Component
public class BusTicketMapperService extends ServiceImpl<BusTicketMapper, BusTicket> {
    public BusTicket getByTicketId(Long ticketId) {
        return this.lambdaQuery().eq(BusTicket::getTicketId, ticketId).one();
    }
}
