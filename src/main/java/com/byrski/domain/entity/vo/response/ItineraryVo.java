package com.byrski.domain.entity.vo.response;

import com.byrski.domain.enums.ProductType;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class ItineraryVo {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private ProductType type;
    private String name;
    private String beginDate;
    private String ticketIntro;
    private String busMoveTime;
    private String position;
    private Integer itineraryStatus;
}
