package com.byrski.infrastructure.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byrski.domain.entity.dto.BusType;
import com.byrski.infrastructure.mapper.BusTypeMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BusTypeMapperService extends ServiceImpl<BusTypeMapper, BusType> {

    public List<BusType> getBusTypeByActivityId(Long activityId) {
        return this.lambdaQuery()
                .eq(BusType::getActivityId, activityId)
                .list();
    }
}
