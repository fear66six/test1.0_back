package com.byrski.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.byrski.domain.entity.dto.ticket.Ticket;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TicketMapper extends BaseMapper<Ticket> {
}
