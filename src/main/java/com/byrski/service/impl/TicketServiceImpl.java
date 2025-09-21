package com.byrski.service.impl;

import com.byrski.domain.entity.BaseTicketEntity;
import com.byrski.service.TicketService;
import com.byrski.strategy.TicketStrategy;
import com.byrski.strategy.factory.TicketStrategyFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TicketServiceImpl implements TicketService {

    @Resource
    private TicketStrategyFactory ticketStrategyFactory;
    @Resource
    private TicketStrategyFactory strategyFactory;

    /**
     * 保存票据
     * @param ticket 票据
     * @return 保存后的票据
     * @param <T> 票据类型
     */
    @Override
    public  <T extends BaseTicketEntity> T saveTicket(T ticket) {
        TicketStrategy<T> strategy = strategyFactory.getStrategy(ticket.getBaseTicket().getType());
        return strategy.saveTicket(ticket);
    }

    /**
     * 根据活动ID获取对应的票据列表
     * @param activityId 活动ID
     * @return 指定活动的票据列表
     */
    @Override
    public List<BaseTicketEntity> getTicketListByActivityId(Long activityId) {
        return ticketStrategyFactory.getAllTicketsByActivityId(activityId);
    }
}
