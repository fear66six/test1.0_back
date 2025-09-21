package com.byrski.infrastructure.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byrski.domain.entity.dto.TradeTickets;
import com.byrski.domain.enums.TicketType;
import com.byrski.infrastructure.mapper.TradeTicketsMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TradeTicketsMapperService extends ServiceImpl<TradeTicketsMapper, TradeTickets> {

    public boolean saveTradeTickets(Long tradeId, Long ticketId, TicketType type) {
        return this.save(TradeTickets.builder().tradeId(tradeId).ticketId(ticketId).type(type).build());
    }

    public List<Long> getTicketIdsByTradeId(Long tradeId) {
        return this.lambdaQuery().eq(TradeTickets::getTradeId, tradeId).list().stream().map(TradeTickets::getTicketId).toList();
    }

    public boolean containRoom(Long tradeId) {
        return this.lambdaQuery().eq(TradeTickets::getTradeId, tradeId).eq(TradeTickets::getType, TicketType.ROOM).count() > 0;
    }

    public Long getRoomTicketIdByTradeId(Long tradeId) {
        TradeTickets ticket = this.lambdaQuery()
                .eq(TradeTickets::getTradeId, tradeId)
                .eq(TradeTickets::getType, TicketType.ROOM)
                .one();
        return ticket != null ? ticket.getTicketId() : null;
    }

    public Map<Long, Long> getRoomTicketIdMapByTradeIds(List<Long> collect) {
        if (collect == null || collect.isEmpty()) {
            return new HashMap<>();
        }
        return this.lambdaQuery().in(TradeTickets::getTradeId, collect).eq(TradeTickets::getType, TicketType.ROOM).list().stream().collect(Collectors.toMap(TradeTickets::getTradeId, TradeTickets::getTicketId));
    }
}
