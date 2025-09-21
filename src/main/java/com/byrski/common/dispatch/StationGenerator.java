package com.byrski.common.dispatch;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component
public class StationGenerator {

    // 生成站点数据的函数
    public static Map<Long, Integer> generateStationData(int siteCount) {
        Map<Long, Integer> stationData = new HashMap<>();

        if (siteCount > 26) {
            throw new IllegalArgumentException("最多26站点");
        }

        Random rand = new Random();


        // 权重：Low (50%), Medium Low (30%), Medium High (15%), High (5%)
        int[] weights = {30, 30, 25, 15};
        int[][] ranges = {
                {5, 12},  // Low
                {13, 35}, // Medium Low
                {35, 60}, // Medium High
                {60, 120} // High
        };

        for (int i = 0; i < siteCount; i++) {
            int people = getPeople(rand, weights, ranges);
            stationData.put((long) i, people);
        }

        return stationData;
    }



    public static int getPeople(Random rand, int[] weights, int[][] ranges) {
        int totalWeight = 100;
        int randomWeight = rand.nextInt(totalWeight);

        int[] range = selectRangeByWeight(randomWeight, weights, ranges);
        return rand.nextInt(range[1] - range[0] + 1) + range[0];
    }

    public static int[] selectRangeByWeight(int randomWeight, int[] weights, int[][] ranges) {
        int ct = 0;
        for (int i = 0; i < weights.length; i++) {
            ct += weights[i];
            if (randomWeight < ct) {
                return ranges[i];
            }
        }

        return ranges[0];
    }



//    public static void saveMapToFile(Map<String, Integer> map, String filePath) {
//        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
//            out.writeObject(map);
//        } catch (IOException e) {
//            System.err.println("Error saving map " + e.getMessage());
//        }
//    }
//
//    public static Map<String, Integer> loadMapFromFile(String filePath) {
//        Map<String, Integer> map = null;
//        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
//            map = (Map<String, Integer>) in.readObject();
//            System.out.println("Map successfully loaded from " + filePath);
//        } catch (IOException | ClassNotFoundException e) {
//            System.err.println("Error loading map from file: " + e.getMessage());
//        }
//        return map;
//    }
}
