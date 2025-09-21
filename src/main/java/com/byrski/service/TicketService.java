package com.byrski.service;

import com.byrski.domain.entity.BaseTicketEntity;

import java.util.List;

public interface TicketService {

    <T extends BaseTicketEntity> T saveTicket(T ticket);

    List<BaseTicketEntity> getTicketListByActivityId(Long activityId);
}
