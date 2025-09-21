package com.byrski.domain.entity.vo.response;

import com.byrski.domain.entity.dto.Product;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
public class SnowfieldDetailVo {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String name;

    private String cover;

    private String intro;

    private LocalTime openingTime;

    private LocalTime closingTime;

    private String location;

    private String slope;

    private List<ProductWithDateVo> products;

}
