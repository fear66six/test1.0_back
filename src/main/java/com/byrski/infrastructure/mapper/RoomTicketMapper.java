package com.byrski.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.byrski.domain.entity.dto.ticket.RoomTicket;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoomTicketMapper extends BaseMapper<RoomTicket> {
}
