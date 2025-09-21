package com.byrski.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 优惠券类型枚举
 */
@Getter
@AllArgsConstructor
public enum CouponType {

    PERCENTAGE(0, "百分比打折"),
    FIXED_AMOUNT(1, "固定金额优惠");

    private final int code;
    private final String description;

    private static final Map<Integer, CouponType> CODE_MAP = new HashMap<>();

    static {
        for (CouponType type : CouponType.values()) {
            CODE_MAP.put(type.getCode(), type);
        }
    }

    public static CouponType fromCode(int code) {
        return CODE_MAP.get(code);
    }
}
