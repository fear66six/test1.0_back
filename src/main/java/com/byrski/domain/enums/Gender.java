package com.byrski.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum Gender {
    MALE(0, "男"),
    FEMALE(1, "女"),
    UNKNOWN(2, "未知"),
    HELICOPTER(3, "直升机"),
    WALMART(4, "沃尔玛购物袋");

    private final int code;
    private final String description;

    private static final Map<Integer, Gender> CODE_MAP = new HashMap<>();

    static {
        for (Gender enumData : Gender.values()) {
            CODE_MAP.put(enumData.getCode(), enumData);
        }
    }

    public static Gender fromCode(int code) {
        return CODE_MAP.get(code);
    }
}
