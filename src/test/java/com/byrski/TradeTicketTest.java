package com.byrski;

import com.byrski.infrastructure.mapper.impl.TradeTicketsMapperService;
import com.byrski.service.ActivityService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class TradeTicketTest {

    @Autowired
    private TradeTicketsMapperService tradeTicketsMapperService;
    @Autowired
    private ActivityService activityService;

    @Test
    public void testTradeTicket() {
        activityService.TestBegin(1890734837121257473L);
    }

}
