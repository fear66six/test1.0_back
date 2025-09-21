package com.byrski.service.impl;

import com.byrski.common.exception.ByrSkiException;
import com.byrski.domain.entity.dto.*;
import com.byrski.domain.entity.vo.request.ChooseBusVo;
import com.byrski.domain.entity.vo.request.ChooseStationVo;
import com.byrski.domain.entity.vo.response.AlternativeStationVo;
import com.byrski.domain.enums.ReturnCode;
import com.byrski.domain.enums.TradeStatus;
import com.byrski.infrastructure.mapper.impl.*;
import com.byrski.service.StationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class StationServiceImpl implements StationService{

    private final StationMapperService stationMapperService;
    private final StationInfoMapperService stationInfoMapperService;
    private final TradeMapperService tradeMapperService;
    private final AreaMapperService areaMapperService;
    private final BusMoveMapperService busMoveMapperService;
    private final BusMapperService busMapperService;

    public StationServiceImpl(
            StationMapperService stationMapperService,
            StationInfoMapperService stationInfoMapperService,
            TradeMapperService tradeMapperService,
            AreaMapperService areaMapperService, BusMoveMapperService busMoveMapperService, BusMapperService busMapperService) {
        this.stationMapperService = stationMapperService;
        this.stationInfoMapperService = stationInfoMapperService;
        this.tradeMapperService = tradeMapperService;
        this.areaMapperService = areaMapperService;
        this.busMoveMapperService = busMoveMapperService;
        this.busMapperService = busMapperService;
    }

    @Override
    public List<AlternativeStationVo> getAlternativeBus(Long tradeId) {
        Trade trade = tradeMapperService.get(tradeId);
        Long activityId = trade.getActivityId();
        List<Station> stations = stationMapperService.getByActivityId(activityId);
        List<AlternativeStationVo> alternativeStationVos = new ArrayList<>();
        for (Station station : stations) {
            Long stationInfoId = station.getStationInfoId();
            StationInfo stationInfo = stationInfoMapperService.get(stationInfoId);
            Long areaId = stationInfo.getAreaId();
            Area area = areaMapperService.get(areaId);
            alternativeStationVos.add(AlternativeStationVo.builder()
                    .station(station)
                    .stationInfo(stationInfo)
                    .area(area)
                    .build());
        }
        return alternativeStationVos;
    }

    @Override
    public void chooseStation(ChooseStationVo chooseStationVo) {
        stationMapperService.addChoicePeopleNum(chooseStationVo.getStationId());
        tradeMapperService.updateStationId(chooseStationVo.getTradeId(), chooseStationVo.getStationId());
        tradeMapperService.updateStatus(chooseStationVo.getTradeId(), TradeStatus.LOCKED);
    }

    @Override
    public List<Bus> getStationBusList(Long stationId) {
        List<BusMove> busMoves = busMoveMapperService.getByStationId(stationId);
        List<Bus> buses = new ArrayList<>();
        for (BusMove busMove : busMoves) {
            if (busMove.getRemainNum() <= 0) {
                continue;
            }
            Long busId = busMove.getBusId();
            Bus bus = busMapperService.get(busId);
            if (bus.getMaxPeople() <= bus.getCarryPeople()) {
                continue;
            }
            buses.add(bus);
        }
        return buses;
    }

    @Override
    public Boolean chooseBus(ChooseBusVo vo) {
        Bus bus = busMapperService.get(vo.getBusId());
        Trade trade = tradeMapperService.get(vo.getTradeId());
        if (trade.getBusId() != null) {
            throw new ByrSkiException(ReturnCode.BUS_ALREADY_CHOOSE);
        }
        Long stationId = trade.getStationId();
        if (stationId == null) {
            throw new ByrSkiException(ReturnCode.STATION_NOT_EXIST);
        }
        BusMove busMove = busMoveMapperService.getByBusIdAndStationId(vo.getBusId(), stationId);
        int remainNum = busMove.getRemainNum();
        if (remainNum <= 0) {
            throw new ByrSkiException(ReturnCode.BUS_FULL);
        }
        if (bus.getMaxPeople() <= bus.getCarryPeople()) {
            throw new ByrSkiException(ReturnCode.BUS_FULL);
        }
        tradeMapperService.setBusAndBusMove(vo.getTradeId(), busMove);
        busMove.setRemainNum(remainNum - 1);
        busMoveMapperService.updateById(busMove);
//        busMapperService.addPassenger(vo.getBusId());
        return true;
    }
}
