package com.byrski.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum DocType {


    DETAIL(1, "detail"),
    SCHEDULE(2, "schedule"),
    ATTENTION(3, "attention"),
    LEADER_NOTICE(4, "leader_notice");


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
