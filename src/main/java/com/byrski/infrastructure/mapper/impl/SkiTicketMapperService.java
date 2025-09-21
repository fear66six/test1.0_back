package com.byrski.infrastructure.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byrski.domain.entity.dto.ticket.SkiTicket;
import com.byrski.infrastructure.mapper.SkiTicketMapper;
import org.springframework.stereotype.Component;

@Component
public class SkiTicketMapperService extends ServiceImpl<SkiTicketMapper, SkiTicket> {

    public SkiTicket getByTicketId(Long ticketId) {
        return this.lambdaQuery().eq(SkiTicket::getTicketId, ticketId).one();
    }

}
