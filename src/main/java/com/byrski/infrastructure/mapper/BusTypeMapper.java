package com.byrski.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.byrski.domain.entity.dto.BusType;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BusTypeMapper extends BaseMapper<BusType> {
}
