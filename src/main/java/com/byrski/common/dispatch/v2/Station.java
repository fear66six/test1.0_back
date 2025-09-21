package com.byrski.common.dispatch.v2;


import lombok.Data;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
public class Station {

    Long id;
    int pnum;
    double latitude;
    double longitude;

    int cluster;

    public Station(Long id, int pnum,double longitude, double latitude) {
        this.id = id;
        this.pnum = pnum;
        this.latitude = latitude;
        this.longitude = longitude;
    }

}


