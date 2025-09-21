package com.byrski.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum SkiChoice {

    // SkiChoice enum
    GENERAL(0, "通用"),
    SNOWBOARD(1, "单板"),
    SKI(2, "双板");

    private final int code;
    private final String description;

    private static final Map<Integer, ItineraryStatus> CODE_MAP = new HashMap<>();

    static {
        for (ItineraryStatus status : ItineraryStatus.values()) {
            CODE_MAP.put(status.getCode(), status);
        }
    }

    public static ItineraryStatus fromCode(int code) {
        return CODE_MAP.get(code);
    }
}
