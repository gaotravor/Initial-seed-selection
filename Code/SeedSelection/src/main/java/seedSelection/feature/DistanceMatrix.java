package seedSelection.feature;

import java.util.Arrays;

public class DistanceMatrix {

    private final Double[][] distanceMatrix;

    public DistanceMatrix(int caseCount) {
        distanceMatrix = new Double[caseCount][caseCount];
        for (int i = 0; i < caseCount; i++) {
            Arrays.fill(distanceMatrix[i], -1.0);
        }
    }

    public double getDistance(int rowIndex, int colIndex, Double[] feature1, Double[] feature2) {
        if (distanceMatrix[rowIndex][colIndex] == -1) {
            distanceMatrix[rowIndex][colIndex] = calDistance(feature1, feature2);
            distanceMatrix[colIndex][rowIndex] = distanceMatrix[rowIndex][colIndex];
        }
        return distanceMatrix[rowIndex][colIndex];
    }

    public static double calDistance(Double[] feature1, Double[] feature2) {
        double distance = 0.0;
        for (int i = 0; i < feature1.length; i++) {
            distance += Math.pow(feature1[i] - feature2[i], 2);
        }
        return Math.sqrt(distance);
    }
}
