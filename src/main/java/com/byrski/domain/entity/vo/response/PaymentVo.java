package com.byrski.domain.entity.vo.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVo {
    private String appId;
    private String timeStamp;
    private String nonceStr;
    private String packageVal; // 加密的支付凭证，包含金额等信息
    private String signType;
    private String paySign;
    private Long tradeId;
    
    // 新增字段：订单金额信息
    private Integer totalAmount; // 总金额（分）
    private Integer originalAmount; // 原始金额（分）
    private Integer discountAmount; // 优惠金额（分）
    private Integer userCount; // 用户数量
}
