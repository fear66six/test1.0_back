package com.byrski.strategy.impl;

import com.byrski.common.exception.ByrSkiException;
import com.byrski.domain.entity.dto.ticket.Ticket;
import com.byrski.domain.entity.dto.ticket.RoomTicket;
import com.byrski.domain.enums.TicketType;
import com.byrski.infrastructure.mapper.impl.TicketMapperService;
import com.byrski.infrastructure.mapper.impl.RoomTicketMapperService;
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
public class RoomTicketStrategy implements TicketStrategy<RoomTicket> {

    @Resource
    private RoomTicketMapperService roomTicketMapperService;
    @Resource
    private TicketMapperService ticketMapperService;

    @Override
    public void processTicket(RoomTicket ticket) {
        System.out.println("Processing Room Ticket: " + ticket.getTicketId());
    }

    @Override
    public void validateTicket(RoomTicket ticket) {
        System.out.println("Validating Room Ticket: " + ticket.getTicketId());
    }

    @Override
    public TicketType getTicketType() {
        return TicketType.ROOM;
    }

    @Override
    public RoomTicket saveTicket(RoomTicket ticket) {
        Long id = ticketMapperService.addTicket(ticket.getBaseTicket());
        ticket.setTicketId(id);
        roomTicketMapperService.save(ticket);
        return ticket;
    }

    @Override
    public List<RoomTicket> getTicketsByActivityId(Long activityId) {
        List<Ticket> baseTickets = ticketMapperService.getTypeTicketsByActivityId(activityId, TicketType.ROOM);
        List<RoomTicket> roomTickets = new ArrayList<>();
        for (Ticket baseTicket : baseTickets) {
            RoomTicket roomTicket = roomTicketMapperService.getByTicketId(baseTicket.getId());
            roomTicket.setBaseTicket(baseTicket);
            roomTickets.add(roomTicket);
        }
        return roomTickets;
    }

    @Override
    public void doLog(RoomTicket ticket) {
        log.info("房票id为:{}, 房间类型:{}, 房间描述:{}", ticket.getTicketId(), ticket.getRoomType().getDescription(), ticket.getDescription());
    }

    @Override
    public RoomTicket getTicketById(Long ticketId) {
        Ticket baseTicket = ticketMapperService.get(ticketId);
        if (baseTicket.getType() != TicketType.ROOM) {
            return null;
        }
        RoomTicket roomTicket = roomTicketMapperService.getByTicketId(ticketId);
        roomTicket.setBaseTicket(baseTicket);
        return roomTicket;
    }
}
