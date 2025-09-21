package com.byrski.domain.entity.vo.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
public class AlternativeBusVo {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long busId;
    private Integer vacantSeatNum;
    private String carNumber;
    private LocalDateTime busMoveTime;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long stationId;

}
