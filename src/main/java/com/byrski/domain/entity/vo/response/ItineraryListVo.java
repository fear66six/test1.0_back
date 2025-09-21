package com.byrski.domain.entity.vo.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ItineraryListVo {

    private List<ItineraryVo> itineraryList;
    private List<ItineraryVo> leaderItineraryList;

}
