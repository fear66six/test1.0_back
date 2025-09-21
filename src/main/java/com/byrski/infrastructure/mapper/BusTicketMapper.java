package com.byrski.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.byrski.domain.entity.dto.ticket.BusTicket;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BusTicketMapper extends BaseMapper<BusTicket> {
}
