package com.byrski.infrastructure.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byrski.domain.entity.dto.StationInfo;
import com.byrski.domain.enums.ReturnCode;
import com.byrski.common.exception.ByrSkiException;
import com.byrski.infrastructure.mapper.StationInfoMapper;
import org.apache.ibatis.annotations.Options;
import org.springframework.stereotype.Component;

@Component
public class StationInfoMapperService extends ServiceImpl<StationInfoMapper, StationInfo> {

    public StationInfo get(Long stationInfoId) {
        StationInfo stationInfo = this.getById(stationInfoId);
        if (stationInfo == null) {
            throw new ByrSkiException(ReturnCode.STATION_INFO_NOT_EXIST);
        }
        return stationInfo;
    }

    @Options(useGeneratedKeys = true, keyProperty = "id")
    public StationInfo addStationInfo(StationInfo stationInfo) {
        this.save(stationInfo);
        return stationInfo;
    }
}
