package com.byrski.domain.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoomType implements IEnum<String> {

    STANDARD_TWIN("STANDARD_TWIN", "标准双床房"),
    STANDARD_DOUBLE("STANDARD_DOUBLE", "标准大床房"),
    DELUXE_SUITE("DELUXE_SUITE", "豪华套房"),
    FAMILY_SUITE("FAMILY_SUITE", "家庭套房"),
    HOSTEL_BED("HOSTEL_BED", "青旅床位"),
    SINGLE_ROOM("SINGLE_ROOM", "单人间"),
    TRIPLE_ROOM("TRIPLE_ROOM", "三人间"),
    OTHER("OTHER", "其他");

    private final String code;
    private final String description;

    @Override
    public String getValue() {
        return this.code;
    }
}
