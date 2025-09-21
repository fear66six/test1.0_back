package com.byrski.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum RentOrderStatus {

    CANCELLED(0, "已取消"),
    UNPAID(1, "未支付"),
    PAID(2, "已支付"),
    REFUNDING(3, "退款中"),
    REFUNDED(4, "已退款");

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
