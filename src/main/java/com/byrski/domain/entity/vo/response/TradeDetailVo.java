package com.byrski.domain.entity.vo.response;

import com.byrski.common.utils.PriceSerializer;
import com.byrski.domain.enums.ProductType;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class TradeDetailVo {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long tradeId;
    private Integer status;
    private LocalDateTime payDdl;
    private String activityName;
    private ProductType type;
    private String intro;
    private String beginDate;
    private String cover;
    @JsonSerialize(using = PriceSerializer.class)
    private Integer cost;
    @JsonSerialize(using = PriceSerializer.class)
    private Integer originalPrice;
    private String onTradeNo;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private Boolean canRefund;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long activityId;
    private String productId;
    @JsonSerialize(using = PriceSerializer.class)
    private Integer rentPrice;
    @JsonSerialize(using = PriceSerializer.class)
    private Integer rentDeposit;
    private List<RentInfo> rentInfos;
    
    // 优惠券信息
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userCouponId;  // 用户选择的优惠券ID
    private CouponInfo couponInfo;  // 优惠券详细信息
    
    // 多用户支持字段
    private List<UserInfo> users;
    private Integer userCount;
    
    @Data
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private String name;
        private Integer gender;
        private String phone;
        private String idCardNumber;
        private Long schoolId;
        private String schoolName;
    }
    
    @Data
    @AllArgsConstructor
    @Builder
    public static class CouponInfo {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long couponId;
        private String couponName;
        private String couponDescription;
        private Double discountAmount;  // 优惠金额
        private Integer discountType;    // 优惠类型：1-固定金额，2-百分比
        private Double discountRate;     // 折扣率（百分比时使用）
        private LocalDateTime validStartTime;
        private LocalDateTime validEndTime;
        private Double minAmount;       // 最低使用金额
        private Integer minUserCount;    // 最低用户数量
    }
}
