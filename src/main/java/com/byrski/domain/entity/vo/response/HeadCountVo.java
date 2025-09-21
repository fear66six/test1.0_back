package com.byrski.domain.entity.vo.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HeadCountVo {

    private Integer thisStationMissingPassengerCount;
    private Integer thisStationTotalPassengerCount;
    private Integer missingPassengerCount;
    private Integer boardedPassengerCount;
    private Integer totalPassengerCount;
    private List<Passenger> unboardedPassengerList;
    private List<Passenger> totalPassengerList;

}
