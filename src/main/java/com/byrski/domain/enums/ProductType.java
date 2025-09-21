package com.byrski.domain.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductType implements IEnum<String> {

    SKI("SKI", "雪票", (byte) 0b010),
    ROOM("ROOM", "房间票", (byte) 0b100),
    BUS("BUS", "车票", (byte) 0b001),
    PACKAGE("PACKAGE", "套餐票", (byte) 0b011),
    UNKNOWN("UNKNOWN", "未知", (byte) 0b000);

    private final String code;
    private final String description;
    private final byte id;

    @Override
    public String getValue() {
        return this.code;
    }

    public static ProductType fromId(byte id) {
        for (ProductType type : ProductType.values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return null;
    }

    public static ProductType fromCode(String code) {
        for (ProductType type : ProductType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
