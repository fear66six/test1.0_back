package com.byrski.domain.enums;

import lombok.Getter;

@Getter
public enum BusinessNoPrefix {
    TRADE("out_trade_no_"),
    REFUND("on_refund_no_"),
    ROOM("room_no_");
    
    private final String prefix;
    
    BusinessNoPrefix(String prefix) {
        this.prefix = prefix;
    }
}