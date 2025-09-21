package com.byrski.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.byrski.domain.entity.dto.RentOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RentOrderMapper extends BaseMapper<RentOrder> {
}
