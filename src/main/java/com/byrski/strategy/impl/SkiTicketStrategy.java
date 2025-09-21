package com.byrski.strategy.impl;

import com.byrski.domain.entity.dto.ticket.Ticket;
import com.byrski.domain.entity.dto.ticket.SkiTicket;
import com.byrski.domain.enums.TicketType;
import com.byrski.infrastructure.mapper.impl.TicketMapperService;
import com.byrski.infrastructure.mapper.impl.SkiTicketMapperService;
import com.byrski.strategy.TicketStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Transactional(rollbackFor = Exception.class)
public class SkiTicketStrategy implements TicketStrategy<SkiTicket> {

    @Resource
    private SkiTicketMapperService skiTicketMapperService;
    @Resource
    private TicketMapperService ticketMapperService;

    @Override
    public void processTicket(SkiTicket ticket) {
        System.out.println("Processing Ski Ticket: " + ticket.getTicketId());
    }

    @Override
    public void validateTicket(SkiTicket ticket) {
        System.out.println("Validating Ski Ticket: " + ticket.getTicketId());
    }

    @Override
    public TicketType getTicketType() {
        return TicketType.SKI;
    }

    @Override
    public SkiTicket saveTicket(SkiTicket ticket) {
        Long id = ticketMapperService.addTicket(ticket.getBaseTicket());
        ticket.setTicketId(id);
        skiTicketMapperService.save(ticket);
        return ticket;
    }

    @Override
    public List<SkiTicket> getTicketsByActivityId(Long activityId) {
        List<Ticket> baseTickets = ticketMapperService.getTypeTicketsByActivityId(activityId, TicketType.SKI);
        List<SkiTicket> skiTickets = new ArrayList<>();
        for (Ticket baseTicket : baseTickets) {
            SkiTicket skiTicket = skiTicketMapperService.getByTicketId(baseTicket.getId());
            skiTicket.setBaseTicket(baseTicket);
            skiTickets.add(skiTicket);
        }
        return skiTickets;
    }

    @Override
    public void doLog(SkiTicket ticket) {
        log.info("雪票id为:{}, 雪票描述为:{}", ticket.getTicketId(), ticket.getDescription());
    }

    @Override
    public SkiTicket getTicketById(Long ticketId) {
        Ticket baseTicket = ticketMapperService.get(ticketId);
        if (baseTicket.getType() != TicketType.SKI) {
            return null;
        }
        SkiTicket skiTicket = skiTicketMapperService.getByTicketId(ticketId);
        skiTicket.setBaseTicket(baseTicket);
        return skiTicket;
    }
}
