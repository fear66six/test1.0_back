package com.byrski.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum ItineraryStatus {
    INVALID_STATION(-1, "报名截止，且上车点无效，需要调用获取替换的上车点"),
    UNLOCKED_TRADE(0, "未锁票"),
    BERORE_TRIP(1, "去程未上车且不在活动当天"),
    NEAR_TRIP(2, "去程未上车且在活动当天"),
    TICKET_UNCHECK(3, "去程已上车，进入验票阶段，显示验票页面"),
    TICKET_CHECKED(4, "去程已上车，已验票，教程第一步"),
    GUIDING(5, "去程已上车，已验票，教程在进行"),
    GUIDE_FINISH(6, "教程完成，但领队未结束滑雪，不给用户展示归还雪具的指引"),
    GUIDE_FINISH_SHOW(7, "教程完成，且领队点击了结束滑雪，给他们显示20那个指引，也就是归还雪具"),
    RETURNED(8, "雪具归还"),
    RETURNED_BOARDED(9, "返程已上车，行程完成"),
    CHOOSE_BUS(10, "已锁票，还没有所属车辆");

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