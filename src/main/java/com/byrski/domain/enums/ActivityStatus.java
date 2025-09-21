package com.byrski.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum ActivityStatus {

    ACTIVE(0, "可报名"),
    DEADLINE_NOT_LOCKED(1, "已截止未锁票"),
    LOCKED(2, "已锁票"),
    START(3, "已开始"),
    EDITING(4, "编辑中");

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
