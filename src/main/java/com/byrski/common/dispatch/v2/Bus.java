package com.byrski.common.dispatch.v2;


import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Data
class Bus {
    static double emptyPenalty = 0.8;
    static double stopPenalty = 0.16;

    String size;
    int capacity;
    int emptySeats;
    int price;
    int reservedSeats;
    int initReservedSeats;
    int passStation;
    Map<Long, Integer> route;

    public Bus(String size, int capacity, int price, int initReservedSeats) {
        this.size = size;
        this.capacity = capacity;
        this.emptySeats = capacity;
        this.price = price;
        this.reservedSeats = initReservedSeats;
        this.initReservedSeats = initReservedSeats;
        this.passStation = 0;
        this.route = new HashMap<>();
    }

    public double calcCarryingProfit(int passengerNum) {
        passengerNum = Math.min(passengerNum, emptySeats);
        passengerNum += reservedSeats;
        int profitStation = Math.max(passStation, 0);
        double profit = (double) passengerNum / (capacity + initReservedSeats) * price
                - stopPenalty * price * profitStation;
        return emptySeats == 0 ? Double.NEGATIVE_INFINITY : profit;
    }

    public int loadPassenger(Long stationId, int passengerNum) {
        int remainingPassengers = Math.max(passengerNum - emptySeats, 0);
        emptySeats = Math.max(emptySeats - passengerNum, 0);

        route.put(stationId, route.getOrDefault(stationId, 0) + passengerNum - remainingPassengers);
        passStation = route.size();

        return remainingPassengers;
    }

    public void removePassenger(Long stationId, Integer passengerNum) {
        if (!route.containsKey(stationId)) {
            Logger.getGlobal().info("ERROR: removePassenger error");
            return;
        }

        int curPNum = route.get(stationId);
        if (passengerNum == null || passengerNum >= curPNum) {
            route.remove(stationId);
            passStation = route.size();
            emptySeats += curPNum;
        }
        else {
            route.put(stationId, curPNum - passengerNum);
            emptySeats += passengerNum;
        }
    }

    public int getPassengerNumByStation(Long stationId) {
        return route.getOrDefault(stationId, 0);
    }

    public List<Map.Entry<Long, Integer>> getSortedRoute(boolean reversed) {

        List<Map.Entry<Long, Integer>> entryList = new ArrayList<>(route.entrySet());
        if(reversed){
            entryList.sort(Map.Entry.<Long, Integer>comparingByValue().reversed());
        }
        else{
            entryList.sort(Map.Entry.<Long, Integer>comparingByValue());
        }

        return entryList;
    }

    public double calcSeatingRate() {
        return (double) (initReservedSeats + capacity - emptySeats - reservedSeats)
                / (initReservedSeats + capacity);
    }

    public int getTotalPassengerNum() {
        return capacity - emptySeats + initReservedSeats - reservedSeats;
    }

    public Map.Entry<Long, Integer> getMinStation() {
        List<Map.Entry<Long, Integer>> entryList = getSortedRoute(false);
        return entryList.get(0);
    }

    public Map.Entry<Long, Integer> getMaxStation() {
        List<Map.Entry<Long, Integer>> entryList = getSortedRoute(false);
        return entryList.get(entryList.size() - 1);
    }


}
