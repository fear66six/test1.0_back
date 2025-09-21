package com.byrski.common.dispatch.v2;

import java.util.*;

public class testDataGenerator {

//    private static final Map<Long, double[]> areaLocDict = Map.ofEntries(
//            Map.entry("BUPT_hai", new double[]{116.358104, 39.961554}),
//            Map.entry("PKU_hai", new double[]{116.310656, 39.999944}),
//            Map.entry("BIT_hai", new double[]{116.318640, 39.963716}),
//            Map.entry("BFSU_hai", new double[]{116.317111, 39.955667}),
//            Map.entry("BJTU_hai", new double[]{116.336481, 39.951748}),
//            Map.entry("CUFE_hai", new double[]{116.342425, 39.959852}),
//            Map.entry("RUC_hai", new double[]{116.318452, 39.970864}),
//            Map.entry("USTB_hai", new double[]{116.358933, 39.992507}),
//            Map.entry("BUAA_hai", new double[]{116.340649, 39.983210}),
//            Map.entry("CNU_hai", new double[]{116.306270, 39.930605}),
//            Map.entry("THU_hai", new double[]{116.326478, 39.999322}),
//            Map.entry("BUAA_sha", new double[]{116.272545, 40.150916}),
//            Map.entry("BUPT_sha", new double[]{116.287149, 40.159828}),
//            Map.entry("CUFE_sha", new double[]{116.284783, 40.170648}),
//            Map.entry("BNU_sha", new double[]{116.269658, 40.168323}),
//            Map.entry("zhongyangminzu_feng", new double[]{116.114763, 39.807772}),
//            Map.entry("shouduyike_feng", new double[]{116.353572, 39.862605}),
//            Map.entry("BIT_fang", new double[]{116.171135, 39.734264}),
//            Map.entry("CNU_fang", new double[]{116.179321, 39.710842}),
//            Map.entry("beijianzhu_daxing", new double[]{116.287730, 39.745015}),
//            Map.entry("beigongye_tong", new double[]{116.668596, 39.931106}),
//            Map.entry("duiwaijingmao_chao", new double[]{116.424635, 39.980395}),
//            Map.entry("beijinglianhe_chao", new double[]{116.428469, 39.991356}),
//            Map.entry("CUC_chao", new double[]{116.556506,39.909699}),
//            Map.entry("CUP_chang", new double[]{116.247789,40.217065}),
//            Map.entry("chinazhengfa_chang", new double[]{116.248095,40.224471}),
//            Map.entry("BUCT_chao", new double[]{116.421730, 39.969863}),
//            Map.entry("yangmei_chao", new double[]{116.464622, 39.985171})
//
//    );

//    public static List<Station> generateRandomStations(int numStations) {
//        List<Long> stationIds = new ArrayList<>(areaLocDict.keySet());
//        numStations = Math.min(numStations, stationIds.size());
//        Collections.shuffle(stationIds);
//        List<Long> selectedIds = stationIds.subList(0, numStations);
//
//        List<Station> stationList = new ArrayList<>();
//        Random random = new Random();
//
//        for (Long stationId : selectedIds) {
//            double[] coord = areaLocDict.get(stationId);
//            double longitude = coord[0];
//            double latitude = coord[1];
//            int pnum = generatePassengerCount(random);
//
//            Station station = new Station(stationId, pnum, longitude, latitude);
//            stationList.add(station);
//        }
//
//        return stationList;
//    }
//
//    private static int generatePassengerCount(Random random) {
//        int percentage = random.nextInt(100);
//        if (percentage < 60) {
//            return random.nextInt(16) + 5; // 5-20 人
//        } else if (percentage < 70) {
//            return random.nextInt(21) + 40; // 40-60 人
//        } else {
//            return random.nextInt(21) + 20; // 20-40 人
//        }
//    }
//
//    public static void main(String[] args) {
//        List<Station> stations = generateRandomStations(6);
//        for (Station station : stations) {
//            System.out.println(station);
//        }
//    }


}


