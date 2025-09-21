package com.byrski.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 优惠券发放方式枚举
 */
@Getter
@AllArgsConstructor
public enum CouponIssueMethod {
    
    /**
     * 页面领取 - 用户通过小程序页面主动领取
     */
    PAGE_RECEIVE(1, "页面领取"),
    
    /**
     * 批量导入 - 后台系统通过手机号列表批量发放
     */
    BATCH_IMPORT(2, "批量导入");

    private final int code;
    private final String description;

    private static final Map<Integer, CouponIssueMethod> CODE_MAP = new HashMap<>();

    static {
        for (CouponIssueMethod method : values()) {
            CODE_MAP.put(method.getCode(), method);
        }
    }

    public static CouponIssueMethod fromCode(int code) {
        return CODE_MAP.get(code);
    }
}
