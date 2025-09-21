package com.byrski.domain.entity.vo.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TradeVo {

    /**
     * 用户openid
     */
    String openid;

    /**
     * 商品描述
     */
    String description;

    /**
     * 商品价格，单位为分
     */
    Integer total;

    /**
     * 订单数据库id
     */
    Long tradeId;

    /**
     * 订单号
     */
    String outTradeNo;
}
