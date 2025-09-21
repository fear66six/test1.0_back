package com.byrski.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单状态枚举
 */
@Getter
@AllArgsConstructor
public enum TradeStatus {

    CANCELLED(0, "已取消(交易关闭)"),
    UNPAID(1, "待付款"),
    PAID(2, "已付款"),
    LOCKED(3, "已锁票"),
    REFUNDING(4, "发起退款中"),
    REFUNDED(5, "已退款"),
    DELETED(6, "已删除"),
    CONFIRMING(7, "待确认上车点"),
    RETURN(8, "已归还"),
    LEADER(100, "领队专属")
    ;


    private final int code;
    private final String description;

    private static final Map<Integer, TradeStatus> CODE_MAP = new HashMap<>();

    static {
        for (TradeStatus enumData : TradeStatus.values()) {
            CODE_MAP.put(enumData.getCode(), enumData);
        }
    }

    public static TradeStatus fromCode(int code) {
        return CODE_MAP.get(code);
    }
}
