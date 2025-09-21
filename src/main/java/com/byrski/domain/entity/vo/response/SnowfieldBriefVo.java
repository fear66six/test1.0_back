package com.byrski.domain.entity.vo.response;

import com.byrski.common.utils.PriceSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class SnowfieldBriefVo {
    @SerializedName("id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @SerializedName("name")
    private String name;
    @SerializedName("cover")
    private String cover;
    @SerializedName("intro")
    private String intro;
    @SerializedName("min_price")
    @JsonSerialize(using = PriceSerializer.class)
    private Integer minPrice;
}
