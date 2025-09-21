package com.byrski;

import com.baomidou.mybatisplus.core.toolkit.Sequence;
import com.byrski.domain.entity.BaseTicketEntity;
import com.byrski.domain.entity.dto.Product;
import com.byrski.domain.entity.dto.ticket.RoomTicket;
import com.byrski.domain.entity.dto.ticket.Ticket;
import com.byrski.domain.enums.ProductType;
import com.byrski.domain.enums.RoomType;
import com.byrski.domain.enums.TicketType;
import com.byrski.infrastructure.mapper.impl.SkiTicketMapperService;
import com.byrski.infrastructure.mapper.impl.StationMapperService;
import com.byrski.infrastructure.repository.manager.ProductManager;
import com.byrski.service.TicketService;
import com.byrski.strategy.factory.TicketStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
@Slf4j
public class NewProductServiceTest {

    @Autowired
    private TicketService ticketService;
    @Autowired
    private ProductManager productManager;
    @Autowired
    private TicketStrategyFactory strategyFactory;

    private final Sequence sequence;
    @Autowired
    private SkiTicketMapperService skiTicketMapperService;
    @Autowired
    private StationMapperService stationMapperService;

    public NewProductServiceTest() {
        this.sequence = new Sequence(null);
    }

    @Test
    @Order(1)
    public void testInsertTicket() {
        Ticket ticket = new Ticket();
        ticket.setId(sequence.nextId());
        ticket.setPrice(60000);
        ticket.setType(TicketType.ROOM);
        RoomTicket roomTicket = new RoomTicket();
        roomTicket.setBaseTicket(ticket);
        roomTicket.setRoomType(RoomType.SINGLE_ROOM);
        log.info("Bus ticket: {}", roomTicket);
        RoomTicket busTicket1 = ticketService.saveTicket(roomTicket);
        log.info("Bus ticket1: {}", busTicket1);
    }

    @Test
    @Order(2)
    public void testGetTicket() {
        List<BaseTicketEntity> activityTickets = ticketService.getTicketListByActivityId(1L);
        for (BaseTicketEntity activityTicket : activityTickets) {
            log.info("Activity ticket: {}", activityTicket);
        }
    }

    @Test
    @Order(3)
    public void testProduct() {
        List<BaseTicketEntity> activityTickets = ticketService.getTicketListByActivityId(1875380988571877378L);
        Product product = productManager.buildByTickets(activityTickets);
        log.info("Product: {}", product);
        productManager.saveProduct(product);
        List<BaseTicketEntity> tickets = product.getTickets();
        for (BaseTicketEntity ticket : tickets) {
            strategyFactory.getStrategy(ticket.getBaseTicket().getType()).doLog(ticket);
        }

        for (BaseTicketEntity activityTicket : activityTickets) {
            if (activityTicket.getBaseTicket().getType() == TicketType.BUS) {
                Product product1 = productManager.buildByTicket(activityTicket, ProductType.BUS);
                productManager.saveProduct(product1);
                break;
            }
        }

        for (BaseTicketEntity activityTicket : activityTickets) {
            if (activityTicket.getBaseTicket().getType() == TicketType.SKI) {
                Product product1 = productManager.buildByTicket(activityTicket, ProductType.SKI);
                productManager.saveProduct(product1);
                break;
            }
        }
    }

    @Test
    public void testAlg() {
        List<TicketType> ticketTypes = Arrays.asList(TicketType.SKI, TicketType.SKI, TicketType.BUS, TicketType.BUS);
        byte typeCode = 0b0;
        for (TicketType ticketType : ticketTypes) {
            typeCode |= ticketType.getId();
            log.info("Type Code: {}", typeCode);
        }
        log.info("Type : {}", ProductType.fromId(typeCode));
    }

    @Test
    public void testStation() {
        List<Long> stationIdsByStationInfoId = stationMapperService.getStationIdsByStationInfoId(null);
        if (stationIdsByStationInfoId == null || stationIdsByStationInfoId.isEmpty()) {
            log.info("Station is empty");
        }
    }
}
