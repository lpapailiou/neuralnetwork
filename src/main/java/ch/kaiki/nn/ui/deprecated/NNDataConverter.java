package ch.kaiki.nn.ui.deprecated;

import ch.kaiki.nn.data.BackPropEntity;
import ch.kaiki.nn.neuralnet.NeuralNetwork;

import java.util.*;
import java.util.function.Function;

public class NNDataConverter {

    private NNDataConverter() { }

    // cost, accuracy, etc.
    public static double[][] getBackPropData(NeuralNetwork neuralNetwork, Function<BackPropEntity, Double> function) {
        SortedMap<Integer, BackPropEntity> rawData = neuralNetwork.getBackPropData().getMap();
        double[][] result = new double[rawData.size()][2];
        int index = 0;
        for (Integer key : rawData.keySet()) {
            double x = (double) key;
            double y = function.apply(rawData.get(key));
            result[index] = new double[]{x, y};
            index++;
        }
        return result;
    }

    // weight
    public static double[][] getWeightMatrix(NeuralNetwork neuralNetwork, int layer) {
        return neuralNetwork.getWeights().get(layer);
    }

    // confusion matrix
    public static double[][] getConfusionMatrix(NeuralNetwork neuralNetwork, double[][] in, double[][] out) {
        Map<String, List<List<Double>>> distinctClassMap = new TreeMap<>();

        for (int i = 0; i < in.length; i++) {
            List<Double> prediction = neuralNetwork.predict(in[i]);
            String key = Arrays.toString(out[i]);
            List<List<Double>> predictions = distinctClassMap.get(key);
            if (predictions == null) {
                predictions = new ArrayList<>();
            }
            predictions.add(prediction);
            distinctClassMap.put(key, predictions);
        }

        int distinctInputs = distinctClassMap.keySet().size();
        double[][] result = new double[distinctInputs][distinctInputs];
        int index = 0;
        for (String key: distinctClassMap.keySet()) {
            List<List<Double>> predictions = distinctClassMap.get(key);
            int length = predictions.get(0).size();
            double[] prediction = new double[length];
            for (List<Double> predList : predictions) {
                for (int i = 0; i < length; i++) {
                    prediction[i] += predList.get(i);
                }
            }
            for (int i = 0; i < length; i++) {
                prediction[i] /= predictions.size();
            }
            result[index] = prediction;
            index++;
        }
        return result;
    }

    // decision boundary -> done
    public static List<double[][][]> getDecisionBoundaryGrids(NeuralNetwork neuralNetwork, double minX, double maxX, double minY, double maxY, int iterX, int iterY, double zFactor) {
        int[] configuration = neuralNetwork.getConfiguration();
        int gridCount = configuration[configuration.length-1];

        List<double[][][]> gridList = new ArrayList<>();
        for (int i = 0; i < gridCount; i++) {
            gridList.add(new double[iterX][iterY][]);
        }
        double stepX = Math.abs(maxX - minX) / iterX;
        double stepY = Math.abs(maxY - minY) / iterX;
        double x = minX;
        double y = minY;
        for (int i = 0; i < iterX; i++) {
            for (int j = 0; j < iterY; j++) {
                double[] input = {x, y};
                y += stepY;
                List<Double> output = neuralNetwork.predict(input);
                for (int k = 0; k < output.size(); k++) {
                    double[][][] grid = gridList.get(k);
                    double out = output.get(k);
                    grid[i][j] = new double[]{input[0], input[1], out * zFactor};
                }
            }
            y = minY;
            x += stepX;
        }
        return gridList;
    }

    private static double[] toArray(List<Double> list) {
        double[] result = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }

}
