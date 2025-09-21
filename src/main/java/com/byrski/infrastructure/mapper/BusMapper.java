package com.byrski.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.byrski.domain.entity.dto.Bus;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BusMapper extends BaseMapper<Bus> {
}
