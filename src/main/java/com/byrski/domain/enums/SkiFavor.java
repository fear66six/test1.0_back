package com.byrski.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 滑雪风格
 */
@Getter
@AllArgsConstructor
public enum SkiFavor {
    BASIC(0, "基础"),
    CARVING(1, "刻滑"),
    BUTTER(2, "平花"),
    PARK(3, "公园"),
    OFFPISTE(4, "野雪");

    private final int code;
    private final String description;

    private static final Map<Integer, SkiFavor> CODE_MAP = new HashMap<>();

    static {
        for (SkiFavor enumData : SkiFavor.values()) {
            CODE_MAP.put(enumData.getCode(), enumData);
        }
    }

    public static SkiFavor fromCode(int code) {
        return CODE_MAP.get(code);
    }
}
