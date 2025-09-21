package com.byrski.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 优惠券页面领取模式枚举
 */
@Getter
@AllArgsConstructor
public enum CouponPageReceiveMode {
    
    /**
     * 指定用户发放 - 管理员指定具体用户ID列表，只有这些用户能领取
     */
    SPECIFIC_USERS(1, "指定用户发放"),
    
    /**
     * 全体用户抢票 - 所有登录小程序的用户都能领取（不超领取上限的前提下）
     */
    ALL_USERS_COMPETITION(2, "全体用户抢票");

    private final int code;
    private final String description;

    private static final Map<Integer, CouponPageReceiveMode> CODE_MAP = new HashMap<>();

    static {
        for (CouponPageReceiveMode mode : values()) {
            CODE_MAP.put(mode.getCode(), mode);
        }
    }

    public static CouponPageReceiveMode fromCode(int code) {
        return CODE_MAP.get(code);
    }
}
