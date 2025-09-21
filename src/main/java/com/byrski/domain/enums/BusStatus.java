package com.byrski.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum BusStatus {

    go(0, "go"),
    arrive(1, "arrive"),
    ski(2, "ski"),
    returned(3, "returned"),
    finish(4, "finish");


    private final int code;
    private final String msg;

    // Code-to-ReturnCode mapping for fast lookup
    private static final Map<Integer, ReturnCode> CODE_MAP = new HashMap<>();

    // Static block to initialize the map
    static {
        for (ReturnCode enumData : ReturnCode.values()) {
            CODE_MAP.put(enumData.getCode(), enumData);
        }
    }

    // Method to get ReturnCode by int code
    public static ReturnCode fromCode(int code) {
        return CODE_MAP.get(code);
    }
}