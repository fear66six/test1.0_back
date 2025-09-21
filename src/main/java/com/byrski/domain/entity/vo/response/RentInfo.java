package com.byrski.domain.entity.vo.response;

import com.byrski.common.utils.PriceSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RentInfo {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long rentItemId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long rentOrderId;
    private String name;
    @JsonSerialize(using = PriceSerializer.class)
    private Integer price;
    @JsonSerialize(using = PriceSerializer.class)
    private Integer deposit;
    private Integer days;
}
