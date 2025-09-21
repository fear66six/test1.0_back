package com.byrski.infrastructure.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byrski.domain.entity.dto.AreaLowerBound;
import com.byrski.infrastructure.mapper.AreaLowerBoundMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AreaLowerBoundMapperService extends ServiceImpl<AreaLowerBoundMapper, AreaLowerBound> {

    public List<AreaLowerBound> getAreaLowerBoundListByActivityId(Long activityId) {
        return lambdaQuery().eq(AreaLowerBound::getActivityId, activityId).list();
    }

    public Boolean addAreaLowerBoundWithInflict(AreaLowerBound areaLowerBound) {
        Long activityId = areaLowerBound.getActivityId();
        Long areaId = areaLowerBound.getAreaId();
        if (lambdaQuery().eq(AreaLowerBound::getActivityId, activityId).eq(AreaLowerBound::getAreaId, areaId).one() != null) {
            return false;
        }
        return save(areaLowerBound);
    }

    public Boolean updateAreaLowerBoundWithInflict(AreaLowerBound areaLowerBound) {
        Long activityId = areaLowerBound.getActivityId();
        Long areaId = areaLowerBound.getAreaId();
        if (lambdaQuery().ne(AreaLowerBound::getId, areaLowerBound.getId()).eq(AreaLowerBound::getActivityId, activityId).eq(AreaLowerBound::getAreaId, areaId).one() != null) {
            return false;
        }
        return this.updateById(areaLowerBound);
    }
}
