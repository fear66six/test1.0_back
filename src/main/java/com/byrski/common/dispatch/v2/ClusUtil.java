package com.byrski.common.dispatch.v2;

import java.util.*;

public class ClusUtil {

    private static final double MAX_SSE = 0.05;


//    public static void main(String[] args) {
//
//        List<Station> stations = new ArrayList<>();
//        stations.add(new Station("duiwaijingmao_chao", 28, 116.358104, 39.961554));
//        stations.add(new Station( "CNU_fang", 21, 116.179321, 39.71117));
//        stations.add(new Station("BNU_sha", 43, 116.269658, 40.168323));
//        stations.add(new Station("BIT_fang", 33, 116.171135, 39.734264));
//        stations.add(new Station("BUAA_hai", 38, 116.340649, 39.98321));
//        stations.add(new Station( "beijinglianhe_chao", 50, 116.428469, 39.991356));
//        stations.add(new Station( "CUFE_hai", 9, 116.342425, 39.959852));
//        stations.add(new Station("CUFE_sha", 46, 116.284783, 40.170648));
//        stations.add(new Station( "BUAA_sha", 39, 116.272545, 40.150916));
//        stations.add(new Station("shouduyike_feng", 9, 116.353572, 39.862605));
//        stations.add(new Station("BJTU_hai", 46, 116.336481, 39.951748));
//
//        List<ClusArea> clusAreas = ClusUtil.clusterStations(stations);
//
//
//    }

    public static class Result {
        public int[] labels;
        public double[][] centers;
        public double sse;

        public Result(int[] labels, double[][] centers, double sse) {
            this.labels = labels;
            this.centers = centers;
            this.sse = sse;
        }
    }

    public static Result weightedKMeans(double[][] coords, double[] weights, int nClusters, int maxIter, double tol, int randomState) {
        Random rng = new Random(randomState);
        int N = coords.length;

        // Randomly initialize cluster centers
        double[][] centers = new double[nClusters][2];
        List<Integer> chosenIndexes = new ArrayList<>();
        while (chosenIndexes.size() < nClusters) {
            int index = rng.nextInt(N);
            if (!chosenIndexes.contains(index)) {
                chosenIndexes.add(index);
                centers[chosenIndexes.size() - 1] = coords[index];
            }
        }

        int[] labels = new int[N];
        for (int iter = 0; iter < maxIter; iter++) {
            double[][] dist = new double[N][nClusters];
            for (int i = 0; i < N; i++) {
                for (int k = 0; k < nClusters; k++) {
                    dist[i][k] = Math.sqrt(Math.pow(coords[i][0] - centers[k][0], 2) + Math.pow(coords[i][1] - centers[k][1], 2));
                }
                labels[i] = getMinIndex(dist[i]);
            }

            // Update centers
            double[][] newCenters = new double[nClusters][2];
            for (int k = 0; k < nClusters; k++) {
                double sumWeights = 0.0;
                double lngSum = 0.0, latSum = 0.0;
                for (int i = 0; i < N; i++) {
                    if (labels[i] == k) {
                        lngSum += coords[i][0] * weights[i];
                        latSum += coords[i][1] * weights[i];
                        sumWeights += weights[i];
                    }
                }
                if (sumWeights > 0) {
                    newCenters[k][0] = lngSum / sumWeights;
                    newCenters[k][1] = latSum / sumWeights;
                } else {
                    newCenters[k] = coords[rng.nextInt(N)];
                }
            }

            // Check for convergence
            double shift = 0.0;
            for (int k = 0; k < nClusters; k++) {
                shift += Math.sqrt(Math.pow(centers[k][0] - newCenters[k][0], 2) + Math.pow(centers[k][1] - newCenters[k][1], 2));
            }
            centers = newCenters;
            if (shift < tol) break;
        }

        // Compute SSE
        double sse = 0.0;
        for (int i = 0; i < N; i++) {
            int cluster = labels[i];
            sse += Math.pow(coords[i][0] - centers[cluster][0], 2) + Math.pow(coords[i][1] - centers[cluster][1], 2);
        }

        return new Result(labels, centers, sse);
    }

    public static List<ClusArea> clusterStations(List<Station> stations) {
        int N = stations.size();
        if (N == 0) return Collections.emptyList();

        double[][] coords = new double[N][2];
        double[] weights = new double[N];
        for (int i = 0; i < N; i++) {
            coords[i][0] = stations.get(i).getLongitude();
            coords[i][1] = stations.get(i).getLatitude();
            weights[i] = 1; // stations.get(i).getPnum();
        }

        int KMax = Math.max(1, (int) Math.floor(0.7 * N));

        double bestSSE = Double.MAX_VALUE;
        Result bestResult = null;


        for (int k = 1; k <= KMax; k++) {
            Result result = weightedKMeans(coords, weights, k, 400, 1e-4, 22);
            if (result.sse < bestSSE) {
                bestSSE = result.sse;
                bestResult = result;
            }
            if (result.sse < MAX_SSE) break;
        }

        List<ClusArea> clusterAreaList = new ArrayList<>();

        for (int i = 0; i < bestResult.centers.length; i++) {
            List<Integer> idList = new ArrayList<>();
            idList.add(i);
            ClusArea area = new ClusArea(idList);
            area.setCenterCoor(bestResult.centers[i]);
            clusterAreaList.add(area);
        }

        for (int i = 0; i < stations.size(); i++) {
            int label = bestResult.labels[i];
            Station station = stations.get(i);
            station.setCluster(label);
            for (ClusArea area : clusterAreaList) {
                if (area.getIds().contains(label)) {
                    area.append(station);
                    break;
                }
            }
        }

        return clusterAreaList;
    }

    private static int getMinIndex(double[] array) {
        int index = 0;
        double min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
                index = i;
            }
        }
        return index;
    }
}