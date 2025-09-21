package com.byrski.common.dispatch.v2;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.*;

@Data
public class ClusArea {

    private List<Station> stations;
    private double[] centerCoor;
    private List<Integer> ids;
    private int mergeCnt;
    private List<Bus> busList;
    private double seatRate;
    // 修改
    private Map<Long, Integer> stationDict;

    private double distance;

    public ClusArea(List<Integer> ids) {
        this.stations = new ArrayList<>();
        this.centerCoor = null;
        this.ids = ids != null ? ids : new ArrayList<>();
        this.mergeCnt = 0;
        this.busList = new ArrayList<>();
    }

    public void append(Station station) {
        this.stations.add(station);
    }

    public void updateCenter() {

    }

    // 需修改
    public Map<Long, Integer> generateStationDict() {
        this.stationDict = new HashMap<>();
        for (Station station : this.stations) {
            this.stationDict.put(station.getId(), station.getPnum());
        }
        return this.stationDict;
    }

    public void mergeArea(ClusArea clusArea) {
        this.stations.addAll(clusArea.stations);
        this.ids.addAll(clusArea.getIds());

        this.mergeCnt++;

        double lng1 = this.centerCoor[0];
        double lat1 = this.centerCoor[1];
        double lng2 = clusArea.centerCoor[0];
        double lat2 = clusArea.centerCoor[1];

        int pnum1 = this.stations.stream().mapToInt(Station::getPnum).sum();
        int pnum2 = clusArea.stations.stream().mapToInt(Station::getPnum).sum();

        int totalPnum = pnum1 + pnum2;
        double lngC = (lng1 * pnum1 + lng2 * pnum2) / totalPnum;
        double latC = (lat1 * pnum1 + lat2 * pnum2) / totalPnum;

        this.centerCoor = new double[]{Math.round(lngC * 1e6) / 1e6, Math.round(latC * 1e6) / 1e6};
    }

    public int getTotalPnum() {
        return this.stations.stream().mapToInt(Station::getPnum).sum();
    }

    public double calcDistance(ClusArea clusArea) {
        double radius=6371000;

        double lon1 = clusArea.centerCoor[0];
        double lat1 = clusArea.centerCoor[1];
        double lon2 = this.centerCoor[0];
        double lat2 = this.centerCoor[1];

        double phi1 = toRadians(lat1);
        double phi2 = toRadians(lat2);
        double lambda1 = toRadians(lon1);
        double lambda2 = toRadians(lon2);

        double deltaPhi = phi2 - phi1;
        double deltaLambda = lambda2 - lambda1;

        double a = pow(sin(deltaPhi / 2), 2)
                + cos(phi1) * cos(phi2) * pow(sin(deltaLambda / 2), 2);
        double c = 2 * asin(sqrt(a));

        double distance = radius * c;
        this.distance = distance;
        return distance;
    }







}
