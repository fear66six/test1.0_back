package com.byrski.domain.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 票类型枚举类，包含车票、雪票、房间票
 */
@Getter
@AllArgsConstructor
public enum TicketType implements IEnum<String> {
    BUS("BUS", "车票", (byte) 0b001),
    SKI("SKI", "雪票", (byte) 0b010),
    ROOM("ROOM", "房间票", (byte) 0b100);

    private final String code;
    private final String description;
    private final byte id;

    @Override
    public String getValue() {
        return this.code;
    }
}
