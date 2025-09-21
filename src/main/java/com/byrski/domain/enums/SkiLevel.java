package com.byrski.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum SkiLevel {

    BEGINNER(0, "小白"),
    INTERMEDIATE(1, "新手"),
    ADVANCED(2, "走刃"),
    EXPERT(3, "大佬");

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
