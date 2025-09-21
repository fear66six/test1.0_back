package com.byrski.service;

import com.byrski.domain.entity.dto.Bus;
import com.byrski.domain.entity.vo.request.ChooseBusVo;
import com.byrski.domain.entity.vo.request.ChooseStationVo;
import com.byrski.domain.entity.vo.response.AlternativeStationVo;

import java.util.List;

public interface StationService {

    List<AlternativeStationVo> getAlternativeBus(Long tradeId);

    void chooseStation(ChooseStationVo chooseStationVo);

    List<Bus> getStationBusList(Long stationId);

    Boolean chooseBus(ChooseBusVo vo);
}
