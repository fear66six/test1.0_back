package com.byrski.domain.entity.vo.response;

import com.byrski.common.utils.PriceSerializer;
import com.byrski.domain.enums.CouponStatus;
import com.byrski.domain.enums.CouponType;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 优惠券响应VO
 */
@Data
@Builder
public class CouponVo {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String name;

    private String description;

    private CouponType type;

    private Double discountValue;

    private Double minAmount;

    private Integer minParticipants;

    private String productId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long activityTemplateId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long snowfieldId;

    private CouponStatus status;

    private Integer issueMethod;

    private Integer pageReceiveMode;

    private Integer issueLimit;

    private Integer issuedCount;

    private Integer perUserLimit;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long creatorId;

    private String remark;

    // 扩展字段
    private String productName;
    private String activityTemplateName;
    private String snowfieldName;
    private String creatorName;
}
