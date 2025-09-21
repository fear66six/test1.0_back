package com.byrski.domain.entity.dto;

import com.byrski.common.utils.PriceSerializer;
import com.byrski.domain.entity.BaseTicketEntity;
import com.byrski.domain.enums.ProductType;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "products")
public class Product {
    @Id
    private String id;

    /**
     * 活动 ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long activityId;

    /**
     * 活动模板 ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long activityTemplateId;

    /**
     * 雪场 ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long snowfieldId;

    /**
     * 产品名称
     */
    private String name;

    /**
     * 产品描述
     */
    private String description;

    /**
     * 产品类型
     */
    private ProductType type;

    /**
     * 产品价格，单位为分
     */
    @JsonSerialize(using = PriceSerializer.class)
    private Integer price;

    /**
     * 产品原价，单位为分
     */
    @JsonSerialize(using = PriceSerializer.class)
    private Integer originalPrice;

    /**
     * 产品销售数量
     */
    private Integer salesCount;

    /**
     * 是否为废弃的产品
     */
    private Boolean deprecated;

    /**
     * 是否为学生票
     */
    private Boolean isStudent;

    /**
     * 门票列表
     */
    private List<BaseTicketEntity> tickets;

    /**
     * 可使用的优惠券ID列表
     */
    private List<Long> availableCouponIds;
}
