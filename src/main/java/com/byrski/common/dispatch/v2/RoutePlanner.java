package com.byrski.common.dispatch.v2;

import java.text.MessageFormat;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Formatter;

public class RoutePlanner {


    static Logger logger = Logger.getLogger("BusPlanner");

    static {
        try {
            FileHandler fh = new FileHandler("bus2_log.txt");
            fh.setFormatter(new Formatter() {
                public String format(LogRecord record) {
                    return String.format("[%s] %s: %s%n",
                            record.getLevel(),
                            record.getLoggerName(),
                            record.getMessage());
                }
            });
            logger.addHandler(fh);
            logger.setLevel(Level.INFO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final int MAX_REPLANNING_TIMES = 4;
    private static final int MIN_PASSENGERS_PER_STATION = 3;
    private static final int MAX_STATIONS_PER_BUS = 3;
    private static final int MAX_ADDITIONAL_BUSES_PER_STATION = 1;

    private static int[] vehicleCapacity;
    private static int[] vehicleCost;


    private static void setVehicleCapacity(int[] vehicleCapacity) {
        RoutePlanner.vehicleCapacity = vehicleCapacity;
    }

    private static void setVehicleCost(int[] vehicleCost) {
        RoutePlanner.vehicleCost = vehicleCost;
    }


//    public static void main(String[] args) {
//
//        int[] vehicleCapacity = {37, 47};   // 小车在前，大车在后，目前只支持两种车型；考虑到领队占位，可以在此处将vehicleCapacity - 1
//        int[] vehicleCosts = {3200, 3800};  // 对应单辆车价格

//        List<Station> stations = new ArrayList<>();
//        stations.add(new Station("BJTU_hai", 17, 116.336481, 39.951748));
//        stations.add(new Station("CUFE_sha", 34, 116.284783, 40.170648));
//        stations.add(new Station("BIT_fang", 6, 116.171135, 39.734264));
//        stations.add(new Station("CUC_chao", 20, 116.556506, 39.909699));
//        stations.add(new Station("CNU_hai", 35, 116.30627, 39.930605));
//        stations.add(new Station("beigongye_tong", 15, 116.668596, 39.931106));
//        stations.add(new Station("THU_hai", 45, 116.326478, 39.999322));
//        stations.add(new Station("PKU_hai", 7, 116.310656, 39.999944));
//        stations.add(new Station("chinazhengfa_chang", 7, 116.248095, 40.224471));
//        stations.add(new Station("CNU_fang", 12, 116.179321, 39.710842));

//        for(int i=0;i<100;i++){
//            List<Station> stations = testDataGenerator.generateRandomStations(10);
//
//            List<ClusArea> clusAreas = RoutePlanner.planRoute(stations, vehicleCapacity, vehicleCosts);
//
//        }
//
//    }


    public static List<ClusArea> planRoute(List<Station> stationList,int[] vehicleCapacity,int [] vehicleCosts){

        List<ClusArea> clusAreaList = new ArrayList<>();

        try {
            if (vehicleCapacity.length != 2 || vehicleCosts.length != 2) {
                throw new IllegalArgumentException("ERROR: vehicle_capacity or vehicle_costs length is not 2");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
            return clusAreaList;
        }

        logger.info(" \n \n------------start_plan----------------------");
        for(Station st: stationList){

            logger.info("ID: " + st.getId()+
                    " passenger: " + st.getPnum() +
                    "(Lng, Lat): " + st.getLongitude()  + ',' + st.getLatitude()
            );

        }

        RoutePlanner.setVehicleCapacity(vehicleCapacity);
        RoutePlanner.setVehicleCost(vehicleCosts);

        List<ClusArea> optAreas = RoutePlanner.optimizeArea(stationList);
        for (ClusArea optArea : optAreas) {
            Map<Long, Integer> curStationMapList = optArea.generateStationDict();
            List<Bus> busList = executeAssign(curStationMapList);
            optArea.setBusList(busList);

        }

        return optAreas;

    }



    // 车辆分配
    private static List<Bus> executeAssign(Map<Long, Integer> curStationMapList){
        List<Bus> busList;

        int totalNum = curStationMapList.values().stream()
                .mapToInt(Integer::valueOf)
                .sum();

        int rePlanCount = 0;
        boolean rePlan = false;

        while (true) {
            if (rePlan) {
                logger.info("------------re_plan----------------------");
                rePlanCount++;
            }
            Combo combo = optimalBusCombos(totalNum, rePlanCount);
            int cost = combo.getLargeBusCount() * vehicleCost[1] +combo.getSmallBusCount() * vehicleCost[0];

            logger.info("Total Bus: " + (combo.getLargeBusCount() + combo.getSmallBusCount()) +
                    " Large Bus: " + combo.getLargeBusCount() +
                    " Small Bus: " + combo.getSmallBusCount() +
                    " Total Cost: " + cost);

            busList = assignBuses(curStationMapList, combo);
            rePlan = checkAssign(curStationMapList, busList);

            if (!rePlan || rePlanCount > MAX_REPLANNING_TIMES) {
                return busList;
            }
        }
    }

    private static List<Bus> assignBuses(Map<Long, Integer> curStationMapList,Combo combo){

        List<Bus> busList = roughAssignBus(combo, curStationMapList);
        List<Bus> newBusList = fineAssignBus(busList);
        return newBusList;

    }

    private static List<Bus> roughAssignBus(Combo combo,Map<Long, Integer> stationMapList){

        int smallBus = combo.getSmallBusCount();
        int largeBus = combo.getLargeBusCount();
        List<Bus> busList = new ArrayList<>();
        Map<Long, Integer> copiedStations = new HashMap<>();
        for (Map.Entry<Long, Integer> entry : stationMapList.entrySet()) {
            copiedStations.put(entry.getKey(), entry.getValue());
        }
        List<Map.Entry<Long, Integer>> sortedStations = new ArrayList<>(copiedStations.entrySet());
        sortedStations.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // 为人员多的站点分配车辆
        while (true) {
            boolean isOpt = false;
            for (Map.Entry<Long, Integer> station : sortedStations) {
                if (largeBus > 0 && station.getValue() > 1.6 * vehicleCapacity[1]) {
                    Bus bus = new Bus("Large", vehicleCapacity[1], vehicleCost[1],0);
                    station.setValue(bus.loadPassenger(station.getKey(), station.getValue()));
                    busList.add(bus);
                    largeBus--;
                    isOpt = true;
                } else if (smallBus > 0 && station.getValue() > 1.6 * vehicleCapacity[0]) {
                    Bus bus = new Bus("Small", vehicleCapacity[0], vehicleCost[0], 0);
                    station.setValue(bus.loadPassenger(station.getKey(), station.getValue()));
                    busList.add(bus);
                    smallBus--;
                    isOpt = true;
                }
            }
            if (!isOpt) {
                break;
            }
        }

        // 分配剩余的车辆
        for (int l = 0; l < largeBus; l++) {
            busList.add(new Bus("Large", vehicleCapacity[1], vehicleCost[1], 0));
        }
        for (int m = 0; m < smallBus; m++) {
            busList.add(new Bus("Small", vehicleCapacity[0], vehicleCost[0], 0));
        }

        // 根据利润调整
        sortedStations.sort(Map.Entry.comparingByValue()); // 乘客数升序
        do {
            for (Map.Entry<Long, Integer> station : sortedStations) {
                if (station.getValue() != 0) {
                    List<Double> profits = new ArrayList<>();
                    for (Bus bus : busList) {
                        profits.add(bus.calcCarryingProfit(station.getValue()));
                    }
                    int bestBusIndex = profits.indexOf(Collections.max(profits));
                    int remainPassenger = busList.get(bestBusIndex).loadPassenger(station.getKey(), station.getValue());
                    station.setValue(remainPassenger);
                }
            }

            sortedStations.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        } while (sortedStations.get(0).getValue() != 0);
        return busList;

    }

    private static List<Bus> fineAssignBus(List<Bus> busList){

        for (Bus optBus : busList) {
            Map.Entry<Long, Integer> minStationEntry = optBus.getMinStation();
            Long minStation = minStationEntry.getKey();
            int minPnum = minStationEntry.getValue();

            while (minPnum < MIN_PASSENGERS_PER_STATION) {
                adjustBatchPerStation(busList, optBus,minStation, minPnum);

                minStationEntry = optBus.getMinStation();
                minStation = minStationEntry.getKey();
                minPnum = minStationEntry.getValue();
            }
        }

        adjustStationPerBus(busList);
        mergeIdenticalBus(busList);

        for (int idx = 0; idx < busList.size(); idx++) {
            Bus bus = busList.get(idx);
            if (bus.getRoute().size() > MAX_STATIONS_PER_BUS) {
                logger.log(Level.WARNING, MessageFormat.format("*******WARNING bus route > {0} ********", MAX_STATIONS_PER_BUS));
            }
            logger.info("Bus : " + bus.getSize() + "_" + idx + ", \nRoute: " + bus.getRoute().keySet() +
                    ", Passengers: " + bus.getRoute().values());
        }

        logger.info("********************************************************************");

        return busList;
    }

    private static boolean checkAssign(Map<Long, Integer> stations, List<Bus> busList) {
        boolean needRePlan = false;
        int largerBusCapacity = vehicleCapacity[1];

        Map<Long, Integer> stationRequiredBuses = new HashMap<>();
        for (Long station : stations.keySet()) {
            int passengerCount = stations.get(station);
            int requiredBuses = (int) Math.ceil((double) passengerCount / largerBusCapacity);
            stationRequiredBuses.put(station, requiredBuses);
        }

        Map<Long, Integer> stationBusCount = new HashMap<>();
        for (Bus bus : busList) {
            for (Long station : bus.getRoute().keySet()) {
                stationBusCount.put(station, stationBusCount.getOrDefault(station, 0) + 1);
            }
        }

        for (Long station : stationRequiredBuses.keySet()) {
            int requiredBuses = stationRequiredBuses.get(station);
            int actualBuses = stationBusCount.getOrDefault(station, 0);

            if (actualBuses >= Math.max(requiredBuses + MAX_ADDITIONAL_BUSES_PER_STATION,3)) {
                needRePlan = true;
                break;
            }
        }

        for (Bus bus : busList) {
            if (bus.getRoute().size() > MAX_STATIONS_PER_BUS) {
                needRePlan = true;
                break;
            }
        }
        return needRePlan;
    }


    // 车辆优化

    private static void adjustBatchPerStation(List<Bus> busList, Bus optBus, Long minStation, int pNum){
        List<Bus> exList = new ArrayList<>();
        for(Bus bus : busList){
            if(bus.getPassengerNumByStation(minStation) > Math.max(MIN_PASSENGERS_PER_STATION*2,pNum)){
                exList.add(bus);
            }
        }
        if(!exList.isEmpty()){
            exList.sort(Comparator.comparingInt(bus -> bus.route.size()));

            Bus exBus = exList.get(0);
            int exPnum = exBus.getPassengerNumByStation(minStation);

            // optBus 中乘客最多的站点和乘客数量
            Map.Entry<Long, Integer> optMaxStation = optBus.getMaxStation();

            Long optStation = optMaxStation.getKey();
            int optMaxPnum = optMaxStation.getValue();

            if(optStation.equals(minStation) || optBus.getEmptySeats() > 2){
                int pnum = Math.min(exPnum / 2, optBus.getEmptySeats());
                optBus.loadPassenger(minStation, pnum);
                exBus.removePassenger(minStation, pnum);
            }
            else{
                int pnum = Math.min(optMaxPnum / 2, exPnum / 2);
                optBus.removePassenger(optStation, pnum);
                exBus.removePassenger(minStation, pnum);
                optBus.loadPassenger(minStation, pnum);
                exBus.loadPassenger(optStation, pnum);

            }

        }

    }

    private static void adjustStationPerBus(List<Bus> busList) {
        for (Bus optBus : busList) {
            int count = 0;
            while (optBus.getRoute().size() > 3 && count < 3) {
                count++;
                Long minStation = optBus.getSortedRoute(false).get(count).getKey();
                int minPNum = optBus.getRoute().get(minStation);
                for (Bus targetBus : busList) {
                    if (targetBus != optBus) {
                        Set<Long> commonStations = new HashSet<>(optBus.getRoute().keySet());
                        commonStations.retainAll(targetBus.getRoute().keySet());
                        if(targetBus.getRoute().size() < 3 || (commonStations.size() >= 2 && commonStations.contains(minStation))){
                            boolean isExchanged =  exchangePassenger(optBus, targetBus, minStation, commonStations, minPNum);
                            if (isExchanged) {
                                count = 0;
                                break;
                            }

                        }
                    }
                }
            }
        }

    }

    public static void mergeIdenticalBus(List<Bus> busList) {
        for (Bus optBus : busList) {
            int count = 0;

            List<Map.Entry<Long, Integer>> sortedRoute = optBus.getSortedRoute(false);

            while (count < optBus.getRoute().size()) {
                Long minStation = sortedRoute.get(count).getKey();
                int minPnum = sortedRoute.get(count).getValue();
                count++;

                for (Bus targetBus : busList) {
                    if (targetBus == optBus) {continue;}

                    Set<Long> commonStations = new HashSet<>(optBus.getRoute().keySet());
                    commonStations.retainAll(targetBus.getRoute().keySet());

                    if (!commonStations.isEmpty() && commonStations.contains(minStation)) {
                        int optAllAvailableCount = minPnum + optBus.getEmptySeats();

                        for (Map.Entry<Long, Integer> entry : targetBus.getRoute().entrySet()) {
                            Long targetStation = entry.getKey();
                            int targetPnum = entry.getValue();

                            if (targetBus.getEmptySeats() >= minPnum) {
                                targetBus.loadPassenger(minStation, minPnum);
                                optBus.removePassenger(minStation,null);
                                break;
                            }
                            else if (!targetStation.equals(minStation)) {
                                int targetAllAvailableCount = targetPnum + targetBus.getEmptySeats();

                                if (targetAllAvailableCount >= minPnum && optAllAvailableCount >= targetPnum) {
                                    targetBus.removePassenger(targetStation,null);
                                    targetBus.loadPassenger(minStation, minPnum);
                                    optBus.removePassenger(minStation,null);
                                    optBus.loadPassenger(targetStation, targetPnum);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean exchangePassenger(Bus optBus, Bus targetBus, Long minStation, Set<Long> commonStations, int minPNum) {
        commonStations.remove(minStation);
        int commonPNum = commonStations.stream()
                .mapToInt(station -> targetBus.getRoute().get(station))
                .sum();
        if (commonPNum >= minPNum && commonPNum - minPNum >= MIN_PASSENGERS_PER_STATION) {
            optBus.removePassenger(minStation, minPNum);
            for (Long station : commonStations) {
                int transferPNum = Math.min(targetBus.getRoute().get(station), minPNum);
                targetBus.removePassenger(station, transferPNum);
                targetBus.loadPassenger(minStation, transferPNum);
                optBus.loadPassenger(station, transferPNum);
                minPNum -= transferPNum;
                if (minPNum == 0) {
                    return true;
                }
            }
        }

        return false;
    }


    // 区域优化

    private static List<ClusArea> optimizeArea(List<Station> stationList){

        List<ClusArea> clusAreas = ClusUtil.clusterStations(stationList);
        for(ClusArea area : clusAreas){
            area.generateStationDict();
            int totalNum = area.getTotalPnum();
            area.setSeatRate(RoutePlanner.calcSeatRate(totalNum));

        }
        RoutePlanner.mergeArea(clusAreas);

        return clusAreas;
    }

    private static List<ClusArea> mergeArea(List<ClusArea> clusAreaList) {

        if (clusAreaList.size() < 2) { return clusAreaList;}
        clusAreaList.sort(Comparator.comparingDouble(c -> c.getSeatRate()));
        int cnt = 0;
        while (clusAreaList.get(0).getSeatRate() < 0.9 && cnt < clusAreaList.size()) {
            int mm = 10;
            Iterator<ClusArea> iterator = clusAreaList.iterator();
            while(iterator.hasNext()){
                ClusArea clusterArea = iterator.next();
                if (clusterArea.getSeatRate() < 0.94) {
                    List<ClusArea> distanceAreaList = new ArrayList<>(clusAreaList);
                    distanceAreaList.sort(Comparator.comparingDouble(c -> c.calcDistance(clusterArea)));
                    for (ClusArea distArea : distanceAreaList) {
                        double distance = distArea.getDistance(); // tmp_distance equivalent
                        if (distance > 28000) {
                            break;
                        }
                        if (distArea != clusterArea && distArea.getMergeCnt() < 3) {
                            clusterArea.mergeArea(distArea);
                            iterator.remove();
                            break;
                        }
                    }
                }


            }
//            for (ClusArea clusterArea : clusAreaList) {
//                if (clusterArea.getSeatRate() < 0.94) {
//                    List<ClusArea> distanceAreaList = new ArrayList<>(clusAreaList);
//                    distanceAreaList.sort(Comparator.comparingDouble(c -> c.calcDistance(clusterArea)));
//                    for (ClusArea distArea : distanceAreaList) {
//                        double distance = distArea.getDistance(); // tmp_distance equivalent
//
//                        if (distance > 25000) {
//                            break;
//                        }
//                        if (distArea != clusterArea && distArea.getMergeCnt() < 3) {
//                            clusterArea.mergeArea(distArea);
//                            clusAreaList.remove(distArea);
//                            break;
//                        }
//                    }
//                }
//            }

            for(ClusArea area : clusAreaList){
                area.generateStationDict();
                int totalNum = area.getTotalPnum();
                area.setSeatRate(RoutePlanner.calcSeatRate(totalNum));
            }

            clusAreaList.sort(Comparator.comparingDouble(c -> c.getSeatRate()));
            cnt++;
        }

        return clusAreaList;
    }



    // 获取车辆组合

    private static Combo calcBusCombos(int N) {
        Map<String, Combo> memo = new HashMap<>();
        return findOptimalBusCombo(N, vehicleCapacity[1], vehicleCost[1], vehicleCapacity[0], vehicleCost[0], 0, 0, memo);
    }

    private static Combo findOptimalBusCombo(int N, int C_A, int P_A, int C_B, int P_B,
                                             int aCount, int bCount, Map<String, Combo> memo) {

        int totalCapacity = aCount * C_A + bCount * C_B;
        if (totalCapacity >= N) {
            return new Combo(aCount, bCount);  // 基础情况，返回当前组合
        }

        String state = aCount + "," + bCount;
        if (memo.containsKey(state)) {
            return memo.get(state);
        }

        Combo bestCombo = null;
        int bestCost = Integer.MAX_VALUE;

        Combo comboA = findOptimalBusCombo(N, C_A, P_A, C_B, P_B, aCount + 1, bCount, memo);
        int costA = comboA.getLargeBusCount() * P_A + comboA.getSmallBusCount() * P_B;
        if (costA < bestCost) {
            bestCombo = comboA;
            bestCost = costA;
        }

        Combo comboB = findOptimalBusCombo(N, C_A, P_A, C_B, P_B, aCount, bCount + 1, memo);
        int costB = comboB.getLargeBusCount() * P_A + comboB.getSmallBusCount() * P_B;
        if (costB < bestCost) {
            bestCombo = comboB;
            bestCost = costB;
        }

        memo.put(state, bestCombo);
        return bestCombo;
    }
    private static Combo optimalBusCombos(int totalNum,int rePlanCnt){
        Combo combo = calcBusCombos(totalNum);

        while(rePlanCnt>0){

            if(combo.getSmallBusCount() > 0){
                combo.setSmallBusCount(combo.getSmallBusCount() - 1);
                combo.setLargeBusCount(combo.getLargeBusCount() + 1);
                rePlanCnt--;
            }
            else{
                combo.setLargeBusCount(combo.getLargeBusCount() + 1);
                rePlanCnt--;
            }
        }

        return combo;
    }

    private static double calcSeatRate(int totalNum){
        Combo combo = RoutePlanner.calcBusCombos(totalNum);
        double rate = (double) totalNum /(combo.getLargeBusCount()*vehicleCapacity[1] + combo.getSmallBusCount()*vehicleCapacity[0]);
        return rate;
    }


}
