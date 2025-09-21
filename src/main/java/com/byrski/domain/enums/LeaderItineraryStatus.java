package com.byrski.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum LeaderItineraryStatus {
    BEFORE_TRIP(0, "未到行程第一天。不显示上车情况"),
    NOT_ALL_GO_ABOARD(1, "有去程未上车的人。显示去程上车情况，以及验票按钮(万一有人没来，人不齐也能验票)"),
    ALL_GO_ABOARD(2, "去程全部上车。不显示去程上车情况，只显示验票按钮"),
    SKI_END(3, "点击了结束滑雪，开始显示返程上车情况"),
    RETURN_ABOARD(4, "领队点完了车上的人，大巴车启程返回"),
    RETURN_FINISH(5, "车上所有人都送回，领队点行程结束");


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
