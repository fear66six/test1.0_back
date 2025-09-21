package com.byrski.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum SkiBoard {

    SKI(0, "单板"),
    SNOWBOARD(1, "双板");

    private final int code;
    private final String description;

    private static final Map<Integer, SkiBoard> CODE_MAP = new HashMap<>();

    static {
        for (SkiBoard enumData : SkiBoard.values()) {
            CODE_MAP.put(enumData.getCode(), enumData);
        }
    }

    public static SkiBoard fromCode(int code) {
        return CODE_MAP.get(code);
    }

}
