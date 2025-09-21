package com.byrski.infrastructure.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byrski.domain.entity.dto.Bus;
import com.byrski.domain.enums.ReturnCode;
import com.byrski.common.exception.ByrSkiException;
import com.byrski.infrastructure.mapper.BusMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class BusMapperService extends ServiceImpl<BusMapper, Bus> {

    public Bus get(Long busId) {
        Bus bus = this.getById(busId);
        if (bus == null) {
            throw new ByrSkiException(ReturnCode.BUS_NOT_EXIST);
        }
        return bus;
    }

    public List<Bus> getByActivityId(Long activityId) {
        return this.lambdaQuery().eq(Bus::getActivityId, activityId).list();
    }

    public List<Bus> getBusListByActivityIds(List<Long> activityIds) {
        if (activityIds == null || activityIds.isEmpty()) {
            return Collections.emptyList();
        }
        return this.lambdaQuery()
                .in(Bus::getActivityId, activityIds)
                .list();
    }

    public Map<Long, Bus> getByIds(Set<Long> busIds) {
        if (busIds == null || busIds.isEmpty()) {
            return Map.of();
        }
        return this.lambdaQuery()
                .in(Bus::getId, busIds)
                .list()
                .stream()
                .collect(Collectors.toMap(Bus::getId, bus -> bus));
    }

    public Boolean removeLeader(Long busId) {
        return this.lambdaUpdate()
                .eq(Bus::getId, busId)
                .set(Bus::getLeaderId, null)
                .update();
    }

    public Boolean addLeader(Long busId, Long leaderId) {
        return this.lambdaUpdate()
                .eq(Bus::getId, busId)
                .set(Bus::getLeaderId, leaderId)
                .update();
    }

    public void addPassenger(Long busId) {
        this.lambdaUpdate()
                .eq(Bus::getId, busId)
                .setSql("carry_people = carry_people + 1")
                .update();
    }

//    public void setGoFinished(Long busId) {
//        this.lambdaUpdate()
//               .eq(Bus::getId, busId)
//               .set(Bus::getGoFinished, true)
//               .update();;
//    }
//
//    public void setSkiFinished(Long busId) {
//        this.lambdaUpdate()
//             .eq(Bus::getId, busId)
//             .set(Bus::getSkiFinished, true)
//             .update();
//    }
//
//    public void setReturnFinished(Long busId) {
//        this.lambdaUpdate()
//           .eq(Bus::getId, busId)
//           .set(Bus::getReturnFinished, true)
//           .update();
//    }
}
