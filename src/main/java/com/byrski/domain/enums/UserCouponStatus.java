package com.byrski.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户优惠券状态枚举
 */
@Getter
@AllArgsConstructor
public enum UserCouponStatus {

    UNUSED(0, "未使用"),
    USED(1, "已使用"),
    EXPIRED(2, "已过期"),
    INVALID(3, "已失效");

    private final int code;
    private final String description;

    private static final Map<Integer, UserCouponStatus> CODE_MAP = new HashMap<>();

    static {
        for (UserCouponStatus status : UserCouponStatus.values()) {
            CODE_MAP.put(status.getCode(), status);
        }
    }

    public static UserCouponStatus fromCode(int code) {
        return CODE_MAP.get(code);
    }
}
