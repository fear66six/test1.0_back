package com.byrski.domain.entity.vo.response;

import com.byrski.common.utils.PriceSerializer;
import com.byrski.domain.entity.BaseTicketEntity;
import com.byrski.domain.entity.dto.ServiceButton;
import com.byrski.domain.enums.ProductType;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductDetailVo {

    private Product product;
    @SerializedName("user")
    private UserInfoVo user;

    @Data
    @Builder
    public static class Product {
        private String productId;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long activityId;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long activityTemplateId;
        private String activityName;
        private String name;
        private String description;
        private ProductType type;
        private Boolean isStudent;
        @JsonSerialize(using = PriceSerializer.class)
        private Integer price;
        @JsonSerialize(using = PriceSerializer.class)
        private Integer originalPrice;
        private FromDate2Date fromDate2Date;
        private String cover;
        private List<ServiceButton> serviceButtons;
        private List<RentInfo> rentInfos;
        private List<BaseTicketEntity> roomTickets;
        private List<BaseTicketEntity> tickets;
    }
}
