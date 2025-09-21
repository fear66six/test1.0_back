package com.byrski.infrastructure.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byrski.common.exception.ByrSkiException;
import com.byrski.domain.entity.dto.ticket.Ticket;
import com.byrski.domain.enums.ReturnCode;
import com.byrski.domain.enums.TicketType;
import com.byrski.infrastructure.mapper.TicketMapper;
import org.apache.ibatis.annotations.Options;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TicketMapperService extends ServiceImpl<TicketMapper, Ticket> {

    public Ticket get(Long ticketId) {
        Ticket ticket = this.getById(ticketId);
        if (ticket == null) {
            throw new ByrSkiException(ReturnCode.TICKET_NOT_EXIST);
        }
        return ticket;
    }

    @Options(useGeneratedKeys = true, keyProperty = "id")
    public Long addTicket(Ticket ticket) {
        this.save(ticket);
        return ticket.getId();
    }

    public List<Ticket> getBaseTicketsByActivityId(Long activityId) {
        return this.lambdaQuery().eq(Ticket::getActivityId, activityId).list();
    }

    public List<Ticket> getTypeTicketsByActivityId(Long activityId, TicketType type) {
        return this.lambdaQuery().eq(Ticket::getActivityId, activityId).eq(Ticket::getType, type).list();
    }

    /**
     * 增加票销量
     * @param ticketId 票 ID
     */
    public void addSale(Long ticketId) {
        this.addSalesCount(ticketId, 1);
    }

    /**
     * 减少票销量
     * @param ticketId 票 ID
     */
    public void subSale(Long ticketId) {
        this.subSalesCount(ticketId, -1);
    }

    /**
     * 增加票销量
     * @param ticketId 票 ID
     * @param count 销量变化量
     */
    public void addSalesCount(Long ticketId, Integer count) {
        this.lambdaUpdate().eq(Ticket::getId, ticketId).setSql("sales_count = sales_count + " + count).update();
    }

    /**
     * 增加票销量
     * @param ticketId 票 ID
     * @param count 销量变化量
     */
    public void subSalesCount(Long ticketId, Integer count) {
        this.lambdaUpdate().eq(Ticket::getId, ticketId).setSql("sales_count = sales_count - " + count).update();
    }
}
