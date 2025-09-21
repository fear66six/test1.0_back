package com.byrski.common.dispatch;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class BusPlanner {

    private static final int MAX_REPLANNING_TIMES = 4;
    private static final int MIN_PASSENGERS_PER_STATION = 3;
    private static final int MAX_STATIONS_PER_BUS = 3;
    private static final int MAX_ADDITIONAL_BUSES_PER_STATION = 1;
    static class Combo {
        private final int largeBusCount;
        private final int smallBusCount;

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

//    public static void main(String[] args) {
//        int[] vehicleCapacity = {37, 47};   // 小车在前，大车在后，目前只支持两种车型；考虑到领队占位，可以在此处将vehicleCapacity - 1
//        int[] vehicleCosts = {3200, 3800};  // 对应单辆车价格
//
//        //生成车站数据测试
//        int test_epoches = 1000;
//        for(int epoch=1;epoch<=test_epoches;epoch++){
//            Map<String, Integer> stations = StationGenerator.generateStationData(10);
//            int totalSum = stations.values().stream().mapToInt(Integer::intValue).sum();
//            log.info("--------------------TEST DATA {} --------------------", epoch);
//            stations.forEach((station, people) -> {
//                log.info("{}: {}", station, people);
//            });
//            log.info("total passengers: {}", totalSum);
//
//            // 得到每辆车的途经车站及对应载客人数信息
//            List<Bus> busList = planRouteTop(totalSum, stations, vehicleCapacity, vehicleCosts);
//
//        }
//
//    }

    /**
     * 车辆调度算法入口
     * @param totalSum 总乘客数
     * @param stations 站点数据
     * @param vehicleCapacity 车辆容量
     * @param vehicleCosts 车辆价格
     * @return 最优车辆调度结果
     */
    public static List<BusInDispatch> planRouteTop(Integer totalSum, Map<Long, Integer> stations,
                                            List<Integer> vehicleCapacity, List<Double> vehicleCosts) {
        List<BusInDispatch> busInDispatchList = new ArrayList<>();

        try {
            if (vehicleCapacity.size() != 2 || vehicleCosts.size() != 2) {
                throw new IllegalArgumentException("ERROR: vehicle_capacity or vehicle_costs length is not 2");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return busInDispatchList;
        }

        Combo bestCombo = optimalBusCombos(totalSum, vehicleCapacity.get(1), vehicleCosts.get(1), vehicleCapacity.get(0), vehicleCosts.get(0));

        int rePlanCount = 0;
        Combo addCombo = bestCombo;
        boolean rePlan = false;

        while (true) {
            if (rePlan) {
                log.info("------------re_plan----------------------");
                addCombo = addBus(bestCombo, rePlanCount);
                rePlanCount++;
            }
            double cost = addCombo.getLargeBusCount() * vehicleCosts.get(1) + addCombo.getSmallBusCount() * vehicleCosts.get(0);

            log.info("Total Bus: {} Large Bus: {} Small Bus: {} Total Cost: {}", addCombo.getLargeBusCount() + addCombo.getSmallBusCount(), addCombo.getLargeBusCount(), addCombo.getSmallBusCount(), cost);

            List<BusInDispatch> currentBusInDispatchList = planRoute(stations, vehicleCapacity, vehicleCosts,0, addCombo);
            rePlan = checkRoute(stations, currentBusInDispatchList, vehicleCapacity.get(1));

            if (!rePlan || rePlanCount > MAX_REPLANNING_TIMES) {
                busInDispatchList = currentBusInDispatchList;
                break;
            }
        }
        return busInDispatchList;
    }


    private static boolean checkRoute(Map<Long, Integer> stations, List<BusInDispatch> busInDispatchList, int largerBusCapacity) {
        boolean needRePlan = false;

        Map<Long, Integer> stationRequiredBuses = new HashMap<>();
        for (Long station : stations.keySet()) {
            int passengerCount = stations.get(station);
            int requiredBuses = (int) Math.ceil((double) passengerCount / largerBusCapacity);
            stationRequiredBuses.put(station, requiredBuses);
        }

        Map<Long, Integer> stationBusCount = new HashMap<>();
        for (BusInDispatch busInDisPatch : busInDispatchList) {
            for (Long station : busInDisPatch.getRoute().keySet()) {
                stationBusCount.put(station, stationBusCount.getOrDefault(station, 0) + 1);
            }
        }

        for (Long station : stationRequiredBuses.keySet()) {
            int requiredBuses = stationRequiredBuses.get(station);
            int actualBuses = stationBusCount.getOrDefault(station, 0);

            if (actualBuses > Math.max(requiredBuses + MAX_ADDITIONAL_BUSES_PER_STATION,3)) {
                needRePlan = true;
                break;
            }
        }

        for (BusInDispatch busInDisPatch : busInDispatchList) {
            if (busInDisPatch.getRoute().size() > MAX_STATIONS_PER_BUS) {
                needRePlan = true;
                break;
            }
        }
        return needRePlan;
    }

    private static List<BusInDispatch> planRoute(Map<Long, Integer> stations, List<Integer> vehicleCapacity, List<Double> vehicleCosts,
                                                 int reservedSeats, Combo bestCombo) {

        List<BusInDispatch> busInDispatchList = planRouteRough(bestCombo.getLargeBusCount(), bestCombo.getSmallBusCount(),
                vehicleCapacity, vehicleCosts, stations, reservedSeats);

        for (int idx = 0; idx < busInDispatchList.size(); idx++) {
            BusInDispatch busInDisPatch = busInDispatchList.get(idx);
            log.info("Bus : {}_{}, Route: {} Passengers: {}", busInDisPatch.getSize(), idx, busInDisPatch.getRoute().keySet(), busInDisPatch.getRoute().values());
        }

        List<BusInDispatch> newBusInDispatchList = planRouteByStation(busInDispatchList, reservedSeats);
        return newBusInDispatchList;
    }

    private static List<BusInDispatch> planRouteByStation(List<BusInDispatch> busInDispatchList, int reservedSeats) {
       log.info("^^^^^^^^^^^^^^^^^((((((plan_route_by_station))))))^^^^^^^^^^^^^^^^^");

        for (BusInDispatch optBusInDispatch : busInDispatchList) {
            Map.Entry<Long, Integer> minStationEntry = optBusInDispatch.getMinStation();
            Long minStation = minStationEntry.getKey();
            int minPnum = minStationEntry.getValue();

            while (minPnum < MIN_PASSENGERS_PER_STATION) {
                BusInDispatch exBusInDispatch = findExchangeBus(busInDispatchList, minStation, minPnum);
                exchangeBus(optBusInDispatch, exBusInDispatch, minStation);

                minStationEntry = optBusInDispatch.getMinStation();
                minStation = minStationEntry.getKey();
                minPnum = minStationEntry.getValue();
            }
        }

        List<BusInDispatch> newBusInDispatchList = optimizeBuses(busInDispatchList);

        for (int idx = 0; idx < newBusInDispatchList.size(); idx++) {
            BusInDispatch busInDisPatch = newBusInDispatchList.get(idx);
            if (busInDisPatch.getRoute().size() > MAX_STATIONS_PER_BUS) {
                log.warn("*******WARNING bus route > {} ********", MAX_STATIONS_PER_BUS);
            }
            log.info("Bus : {}_{}, Route: {}, Passengers: {}", busInDisPatch.getSize(), idx, busInDisPatch.getRoute().keySet(), busInDisPatch.getRoute().values());
        }

        return newBusInDispatchList;
    }

    private static boolean exchangePassenger(BusInDispatch optBusInDispatch, BusInDispatch targetBusInDispatch, Long minStation, Set<Long> commonStations, int minPNum) {
        commonStations.remove(minStation);
        int commonPNum = commonStations.stream()
                .mapToInt(station -> targetBusInDispatch.getRoute().get(station))
                .sum();
        if (commonPNum >= minPNum && commonPNum - minPNum >= MIN_PASSENGERS_PER_STATION) {
            optBusInDispatch.removePassenger(minStation, minPNum);
            for (Long station : commonStations) {
                int transferPNum = Math.min(targetBusInDispatch.getRoute().get(station), minPNum);
                targetBusInDispatch.removePassenger(station, transferPNum);
                targetBusInDispatch.loadPassenger(minStation, transferPNum);
                optBusInDispatch.loadPassenger(station, transferPNum);
                minPNum -= transferPNum;
                if (minPNum == 0) {
                    return true;
                }
            }
        }

        return false;
    }

    private static List<BusInDispatch> optimizeBuses(List<BusInDispatch> busInDispatchList) {
        for (BusInDispatch optBusInDispatch : busInDispatchList) {
            int count = 0;
            while (optBusInDispatch.getRoute().size() > 3 && count < 3) {
                count++;
                Long minStation = optBusInDispatch.getSortedRoute(false).get(count).getKey();
                int minPNum = optBusInDispatch.getRoute().get(minStation);
                for (BusInDispatch targetBusInDispatch : busInDispatchList) {
                    if (targetBusInDispatch != optBusInDispatch) {
                        Set<Long> commonStations = new HashSet<>(optBusInDispatch.getRoute().keySet());
                        commonStations.retainAll(targetBusInDispatch.getRoute().keySet());
                        if(targetBusInDispatch.getRoute().size() < 3 || (commonStations.size() >= 2 && commonStations.contains(minStation))){
                            boolean isExchanged =  exchangePassenger(optBusInDispatch, targetBusInDispatch, minStation, commonStations, minPNum);
                            if (isExchanged) {
                                count = 0;
                                break;
                            }

                        }
                    }
                }
            }
        }

        return busInDispatchList;
    }

    private static void exchangeBus(BusInDispatch optBusInDispatch, BusInDispatch exBusInDispatch, Long minStation){
        if (exBusInDispatch == null) {
            log.info("NONENEEEE!!!!");
            return;
        }

        // optBus 中乘客最多的站点和乘客数量
        Map.Entry<Long, Integer> optMaxStation = optBusInDispatch.getMaxStation();
        if (optMaxStation == null) return;

        Long optStation = optMaxStation.getKey();
        int optMaxPnum = optMaxStation.getValue();

        // exBus在minStation的乘客数量
        int exPnum = exBusInDispatch.getPassengerNumByStation(minStation);

        // 计算交换的乘客数量
        int pnum = Math.min(optMaxPnum / 2, exPnum / 2);

        optBusInDispatch.removePassenger(optStation, pnum);
        exBusInDispatch.removePassenger(minStation, pnum);
        optBusInDispatch.loadPassenger(minStation, pnum);
        exBusInDispatch.loadPassenger(optStation, pnum);
    }

    private static BusInDispatch findExchangeBus(List<BusInDispatch> busInDispatchList, Long staName, int pNum){
        List<BusInDispatch> exList = new ArrayList<>();
        for(BusInDispatch busInDisPatch : busInDispatchList){
            if(busInDisPatch.getPassengerNumByStation(staName) > Math.max(MIN_PASSENGERS_PER_STATION*2,pNum)){
                exList.add(busInDisPatch);
            }
        }
        if(!exList.isEmpty()){
            exList.sort(Comparator.comparingInt(busInDispatch -> busInDispatch.route.size()));
            return exList.get(0);
        }
        return null;
    }


    private static List<BusInDispatch> planRouteRough(int largerBus, int smallBus, List<Integer> vehicleCapacity,
                                                      List<Double> vehicleCosts, Map<Long, Integer> stations,
                                                      int initReservedSeats) {
        List<BusInDispatch> busInDispatchList = new ArrayList<>();
        HashMap<Long, Integer> copiedStations = new HashMap<>(stations);
        List<Map.Entry<Long, Integer>> sortedStations = new ArrayList<>(copiedStations.entrySet());
        sortedStations.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // 为人员多的站点分配车辆
        while (true) {
            boolean isOpt = false;
            for (Map.Entry<Long, Integer> station : sortedStations) {
                if (largerBus > 0 && station.getValue() > 1.6 * vehicleCapacity.get(1)) {
                    BusInDispatch busInDisPatch = new BusInDispatch("Large", vehicleCapacity.get(1), vehicleCosts.get(1), initReservedSeats);
                    station.setValue(busInDisPatch.loadPassenger(station.getKey(), station.getValue()));
                    busInDispatchList.add(busInDisPatch);
                    largerBus--;
                    isOpt = true;
                } else if (smallBus > 0 && station.getValue() > 1.6 * vehicleCapacity.get(0)) {
                    BusInDispatch busInDisPatch = new BusInDispatch("Small", vehicleCapacity.get(0), vehicleCosts.get(0), initReservedSeats);
                    station.setValue(busInDisPatch.loadPassenger(station.getKey(), station.getValue()));
                    busInDispatchList.add(busInDisPatch);
                    smallBus--;
                    isOpt = true;
                }
            }
            if (!isOpt) {
                break;
            }
        }

        // 分配剩余的车辆
        for (int l = 0; l < largerBus; l++) {
            busInDispatchList.add(new BusInDispatch("Large", vehicleCapacity.get(1), vehicleCosts.get(1), initReservedSeats));
        }
        for (int m = 0; m < smallBus; m++) {
            busInDispatchList.add(new BusInDispatch("Small", vehicleCapacity.get(0), vehicleCosts.get(0), initReservedSeats));
        }

        // 根据利润调整
        sortedStations.sort(Map.Entry.comparingByValue()); // 乘客数升序
        do {
            for (Map.Entry<Long, Integer> station : sortedStations) {
                if (station.getValue() != 0) {
                    List<Double> profits = new ArrayList<>();
                    for (BusInDispatch busInDisPatch : busInDispatchList) {
                        profits.add(busInDisPatch.calcCarryingProfit(station.getValue()));
                    }
                    int bestBusIndex = profits.indexOf(Collections.max(profits));
                    int remainPassenger = busInDispatchList.get(bestBusIndex).loadPassenger(station.getKey(), station.getValue());
                    station.setValue(remainPassenger);
                }
            }

            sortedStations.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        } while (sortedStations.get(0).getValue() != 0);
        return busInDispatchList;
    }

    private static Combo addBus(Combo bestCombo, int cnt) {
        if(bestCombo.getSmallBusCount() > cnt){
            return new Combo(bestCombo.largeBusCount+cnt+1, bestCombo.smallBusCount-cnt-1);
        }
        else{
            return new Combo(bestCombo.largeBusCount, bestCombo.smallBusCount+1);
        }

    }

    private static Combo optimalBusCombos(Integer N, Integer C_A, Double P_A, Integer C_B, Double P_B) {
        Map<String, Combo> memo = new HashMap<>();
        return findOptimalBusCombo(N, C_A, P_A, C_B, P_B, 0, 0, memo);
    }

    private static Combo findOptimalBusCombo(Integer N, Integer C_A, Double P_A, Integer C_B, Double P_B,
                                             int aCount, int bCount, Map<String, Combo> memo) {

        int totalCapacity = aCount * C_A + bCount * C_B;
        if (totalCapacity >= N) {
            return new Combo(aCount, bCount);  // 基础情况，返回当前组合
        }

        String state = aCount + "," + bCount;
        if (memo.containsKey(state)) {
            return memo.get(state); // 从备忘录中获取组合
        }

        Combo bestCombo = null;
        double bestCost = Double.MAX_VALUE;

        Combo comboA = findOptimalBusCombo(N, C_A, P_A, C_B, P_B, aCount + 1, bCount, memo);
        double costA = comboA.getLargeBusCount() * P_A + comboA.getSmallBusCount() * P_B;
        if (costA < bestCost) {
            bestCombo = comboA;
            bestCost = costA;
        }

        Combo comboB = findOptimalBusCombo(N, C_A, P_A, C_B, P_B, aCount, bCount + 1, memo);
        double costB = comboB.getLargeBusCount() * P_A + comboB.getSmallBusCount() * P_B;
        if (costB < bestCost) {
            bestCombo = comboB;
            bestCost = costB;
        }

        memo.put(state, bestCombo);
        return bestCombo;
    }
}


