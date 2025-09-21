package com.byrski.domain.entity.vo.response;

import com.byrski.common.utils.PriceSerializer;
import com.byrski.domain.entity.BaseTicketEntity;
import com.byrski.domain.enums.ProductType;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductWithDateVo {

    private String id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long activityId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long activityTemplateId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long snowfieldId;
    private String activityName;
    private String name;
    private String description;
    private ProductType type;
    private Boolean isStudent;
    @JsonSerialize(using = PriceSerializer.class)
    private Integer price;
    private FromDate2Date fromDate2Date;
    private List<BaseTicketEntity> tickets;
}
