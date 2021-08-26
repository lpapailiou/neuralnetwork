package ch.kaiki.nn.ui.deprecated;


import ch.kaiki.nn.neuralnet.NeuralNetwork;

import java.util.List;

public class MeshGridFactory {

    private double xMin;
    private double xMax;
    private double yMin;
    private double yMax;

    double[][][] getMeshGrid(NeuralNetwork neuralNetwork, double minX, double maxX, double minY, double maxY, int iterX, int iterY) {
        return getMeshGrid(neuralNetwork, minX, maxX, minY, maxY, iterX, iterY,0);
    }

    double[][][] getMeshGrid(NeuralNetwork neuralNetwork, double minX, double maxX, double minY, double maxY, int iterX, int iterY, int zIndex) {
        if (neuralNetwork == null) {
            throw new IllegalArgumentException("Neural network must not be null!");
        }
        int[] configuration = neuralNetwork.getConfiguration();
        if (configuration[0] != 2) {
            throw new IllegalArgumentException("Decision boundaries can only be plotted for 2-dimensional inputs!");
        }
        if (zIndex < 0 || zIndex > configuration[configuration.length-1]) {
            throw new IllegalArgumentException("zIndex " + zIndex + " is out of scope!");
        }

        double[][][] grid = new double[iterX][iterY][];
        double stepX = Math.abs(maxX - minX) / iterX;
        double stepY = Math.abs(maxY - minY) / iterX;
        double x = minX;
        double y = minY;

        for (int i = 0; i <= iterX; i++) {
            for (int j = 0; j <= iterY; j++) {
                double[] input = {x, y};
                y += stepY;
                List<Double> output = neuralNetwork.predict(input);
                grid[i][j] = new double[] {input[0], input[1], output.get(zIndex)};
            }
            y = minY;
            x += stepX;
        }

        return grid;
    }

    private void prepareRanges(double[][] in) {
        xMin = Double.MAX_VALUE;
        xMax = Double.MIN_VALUE;
        yMin = Double.MAX_VALUE;
        yMax = Double.MIN_VALUE;

        for (int i = 0; i < in.length; i++) {
            double xValue = in[i][0];
            if (xValue < xMin) {
                xMin = xValue;
            }
            if (xValue > xMax) {
                xMax = xValue;
            }
            double yValue = in[i][1];
            if (yValue < yMin) {
                yMin = yValue;
            }
            if (yValue > yMax) {
                yMax = yValue;
            }
        }
    }

}
