package com.byrski.domain.entity.dto.ticket;

import com.baomidou.mybatisplus.annotation.*;
import com.byrski.common.utils.PriceDeserializer;
import com.byrski.common.utils.PriceSerializer;
import com.byrski.domain.enums.TicketType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tickets")
public class Ticket {

    /**
     * 票的id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 票价，单位为分
     */
    @TableField(value = "price")
    @JsonSerialize(using = PriceSerializer.class)
    @JsonDeserialize(using = PriceDeserializer.class)
    private Integer price;

    /**
     * 销量
     */
    @TableField("sales_count")
    private Integer salesCount;

    /**
     * 原价
     */
    @TableField("original_price")
    @JsonSerialize(using = PriceSerializer.class)
    @JsonDeserialize(using = PriceDeserializer.class)
    private Integer originalPrice;

    /**
     * 票的类型，包括车票、雪票、房间票
     */
    @TableField("type")
    @EnumValue
    private TicketType type;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    /**
     * 活动id
     */
    @TableField("activity_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long activityId;

    /**
     * 活动模板id
     */
    @TableField("activity_template_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long activityTemplateId;

    /**
     * 雪场id
     */
    @TableField("snowfield_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long snowfieldId;

    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 最少使用人数
     */
    @TableField("min_people")
    private Integer minPeople;

    /**
     * 最多使用人数
     */
    @TableField("max_people")
    private Integer maxPeople;

}
