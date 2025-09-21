package com.byrski.domain.entity.vo.response;

import com.byrski.domain.entity.dto.Area;
import com.byrski.domain.entity.dto.Station;
import com.byrski.domain.entity.dto.StationInfo;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AlternativeStationVo {
    private Station station;
    private StationInfo stationInfo;
    private Area area;
}
