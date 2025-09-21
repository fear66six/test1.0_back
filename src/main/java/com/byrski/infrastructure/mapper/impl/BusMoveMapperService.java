package com.byrski.infrastructure.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byrski.domain.entity.dto.BusMove;
import com.byrski.domain.enums.ReturnCode;
import com.byrski.common.exception.ByrSkiException;
import com.byrski.infrastructure.mapper.BusMoveMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BusMoveMapperService extends ServiceImpl<BusMoveMapper, BusMove> {

    public BusMove get(Long busMoveId) {
        BusMove busMove = this.getById(busMoveId);
        if (busMove == null) {
            throw new ByrSkiException(ReturnCode.BUS_MOVE_NOT_EXIST);
        }
        return busMove;
    }

    public BusMove getByBusIdAndStationId(Long busId, Long stationId) {
        BusMove busMove = this.query()
                .eq("bus_id", busId)
                .eq("station_id", stationId)
                .one();
        if (busMove == null) {
            throw new ByrSkiException(ReturnCode.BUS_MOVE_NOT_EXIST);
        }
        return busMove;
    }

    public List<BusMove> getByBusId(Long busId) {
        List<BusMove> busMoves = this.query()
                .eq("bus_id", busId)
                .list();
        if (busMoves == null || busMoves.isEmpty()) {
            throw new ByrSkiException(ReturnCode.BUS_MOVE_NOT_EXIST);
        }
        return busMoves;
    }

    public List<BusMove> getByStationId(Long stationId) {
        return this.lambdaQuery()
                .eq(BusMove::getStationId, stationId)
                .list();
    }

    public List<BusMove> getByActivityId(Long activityId) {
        return this.query()
              .eq("activity_id", activityId)
              .list();
    }

    public void setGoFinished(Long busMoveId) {
        this.lambdaUpdate()
            .eq(BusMove::getId, busMoveId)
            .set(BusMove::getGoFinished, true)
            .update();
    }

    public Map<Long, BusMove> getByIds(Set<Long> busMoveIds) {
        if (busMoveIds == null || busMoveIds.isEmpty()) {
            return Map.of();
        }
        return this.query()
                .in("id", busMoveIds)
                .list()
                .stream()
                .collect(Collectors.toMap(BusMove::getId, busMove -> busMove));
    }
}
