package com.byrski.domain.entity.vo.response;

import com.byrski.common.utils.PriceSerializer;
import com.byrski.domain.enums.ProductType;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TradeMeta {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long tradeId;
    private String activityName;
    private ProductType type;
    private String cover;
    private String intro;
    private String beginDate;
    @JsonSerialize(using = PriceSerializer.class)
    private Integer originalPrice;
    @JsonSerialize(using = PriceSerializer.class)
    private Integer cost;
    private Integer status;
}
