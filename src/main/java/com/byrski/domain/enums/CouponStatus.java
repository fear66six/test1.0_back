package com.byrski.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 优惠券状态枚举
 */
@Getter
@AllArgsConstructor
public enum CouponStatus {

    DRAFT(0, "草稿"),
    ACTIVE(1, "已启用"),
    INACTIVE(2, "已停用"),
    EXPIRED(3, "已过期"),
    DELETED(4, "已删除");

    private final int code;
    private final String description;

    private static final Map<Integer, CouponStatus> CODE_MAP = new HashMap<>();

    static {
        for (CouponStatus status : CouponStatus.values()) {
            CODE_MAP.put(status.getCode(), status);
        }
    }

    public static CouponStatus fromCode(int code) {
        return CODE_MAP.get(code);
    }
}
