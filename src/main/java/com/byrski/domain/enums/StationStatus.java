package com.byrski.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum StationStatus {

    VALID(1, "有效"),
    INVALID(0, "无效");

    private final int code;
    private final String description;

    private static final Map<Integer, SkiLevel> CODE_MAP = new HashMap<>();

    static {
        for (SkiLevel enumData : SkiLevel.values()) {
            CODE_MAP.put(enumData.getCode(), enumData);
        }
    }

    public static SkiLevel fromCode(int code) {
        return CODE_MAP.get(code);
    }
}
