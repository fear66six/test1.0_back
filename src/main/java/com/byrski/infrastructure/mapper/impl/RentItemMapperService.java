package com.byrski.infrastructure.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byrski.domain.entity.dto.RentItem;
import com.byrski.infrastructure.mapper.RentItemMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RentItemMapperService extends ServiceImpl<RentItemMapper, RentItem> {

    public List<RentItem> getByActivityId(Long activityId) {
        return this.lambdaQuery().eq(RentItem::getActivityId, activityId).list();
    }

    public List<RentItem> getRentItemListByActivityId(Long id) {
        return this.lambdaQuery().eq(RentItem::getActivityId, id).list();
    }

    public RentItem get(Long id) {
        return this.getById(id);
    }
}
