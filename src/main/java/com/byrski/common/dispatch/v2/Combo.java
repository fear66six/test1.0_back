package com.byrski.common.dispatch.v2;


import lombok.Data;

@Data
public class Combo {
    private int largeBusCount;
    private int smallBusCount;

    public Combo(int largeBusCount, int smallBusCount) {
        this.largeBusCount = largeBusCount;
        this.smallBusCount = smallBusCount;
    }

    public int getLargeBusCount() {
        return largeBusCount;
    }

    public int getSmallBusCount() {
        return smallBusCount;
    }


}
