package com.byrski.strategy.impl;

import com.byrski.domain.entity.dto.ticket.BusTicket;
import com.byrski.domain.entity.dto.ticket.Ticket;
import com.byrski.domain.enums.TicketType;
import com.byrski.infrastructure.mapper.impl.BusTicketMapperService;
import com.byrski.infrastructure.mapper.impl.TicketMapperService;
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
public class BusTicketStrategy implements TicketStrategy<BusTicket> {

    @Resource
    private BusTicketMapperService busTicketMapperService;
    @Resource
    private TicketMapperService ticketMapperService;

    @Override
    public void processTicket(BusTicket ticket) {
        System.out.println("Processing Bus Ticket: " + ticket.getTicketId());
    }

    @Override
    public void validateTicket(BusTicket ticket) {
        System.out.println("Validating Bus Ticket: " + ticket.getTicketId());
    }

    @Override
    public TicketType getTicketType() {
        return TicketType.BUS;
    }

    @Override
    public BusTicket saveTicket(BusTicket ticket) {
        Long id = ticketMapperService.addTicket(ticket.getBaseTicket());
        ticket.setTicketId(id);
        busTicketMapperService.save(ticket);
        return ticket;
    }

    @Override
    public List<BusTicket> getTicketsByActivityId(Long activityId) {
        List<Ticket> baseTickets = ticketMapperService.getTypeTicketsByActivityId(activityId, TicketType.BUS);
        List<BusTicket> busTickets = new ArrayList<>();
        for (Ticket baseTicket : baseTickets) {
            BusTicket busTicket = busTicketMapperService.getByTicketId(baseTicket.getId());
            busTicket.setBaseTicket(baseTicket);
            busTickets.add(busTicket);
        }
        return busTickets;
    }

    @Override
    public void doLog(BusTicket ticket) {
        log.info("车票id为:{}, 描述信息为:{}", ticket.getTicketId(), ticket.getDescription());
    }

    @Override
    public BusTicket getTicketById(Long ticketId) {
        Ticket baseTicket = ticketMapperService.get(ticketId);
        if (baseTicket.getType() != TicketType.BUS) {
            return null;
        }
        BusTicket busTicket = busTicketMapperService.getByTicketId(ticketId);
        busTicket.setBaseTicket(baseTicket);
        return busTicket;
    }


}
