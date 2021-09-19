package ch.kaiki.nn.data;

import ch.kaiki.nn.neuralnet.Matrix;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;

public class BackPropData implements Serializable {
    private static final long serialVersionUID = 2L;
    private SortedMap<Integer, BackPropEntity> iterationMap = new TreeMap<>();
    private double[][] confusionMatrix;
    private double costSum;
    private int tpSum;
    private int fpSum;
    private int tnSum;
    private int fnSum;
    private boolean initialized;
    private int[] iterCount;
    private boolean isBinary;

    public void add(int iteration, double cost, double[] actual, double[] expected) {
        if (!initialized) {
            isBinary = expected.length == 1;
            if (isBinary) {
                confusionMatrix = new double[2][2];
                iterCount = new int[2];
            } else {
                confusionMatrix = new double[expected.length][expected.length];
                iterCount = new int[expected.length];
            }
            initialized = true;
        }
        int tp = 0;
        int fp = 0;
        int tn = 0;
        int fn = 0;
        costSum += cost;
        for (int i = 0; i < expected.length; i++) {
            int maxIndex = getMaxIndex(actual);
            double maxValue = actual[maxIndex];
            // collect scores
            if (expected[i] > 0.5) {
                if ((!isBinary && actual[i] == maxValue) || (isBinary && actual[i] > 0.5)) {
                    tpSum++;
                    tp++;
                } else {
                    fnSum++;
                    fn++;
                }
            } else {
                if ((!isBinary && actual[i] == maxValue) || (isBinary && actual[i] > 0.5)) {
                    fpSum++;
                    fp++;
                } else {
                    tnSum++;
                    tn++;
                }
            }

            // build confusion matrix
            if (isBinary) {
                if (expected[0] > 0.5) {
                    iterCount[0] += 1;
                } else {
                    iterCount[1] += 1;
                }
                confusionMatrix[0][0] += tp;
                confusionMatrix[0][1] += fn;
                confusionMatrix[1][0] += fp;
                confusionMatrix[1][1] += tn;

            } else {
                if (expected[i] > 0.5) {
                    iterCount[i] += 1;
                    for (int j = 0; j < actual.length; j++) {
                        double value = j == maxIndex ? 1 : 0;
                        confusionMatrix[i][j] += value;
                    }

                }
            }
        }
        BackPropEntity backPropEntity = new BackPropEntity(cost, tp, fp, tn, fn, costSum, tpSum, fpSum, tnSum, fnSum);
        iterationMap.put(iteration, backPropEntity);
    }

    public SortedMap<Integer, BackPropEntity> getMap() {
        return iterationMap;
    }

    public double[][] getConfusionMatrix() {
        if (!initialized) {
            return null;
        }
        return transpose(confusionMatrix);
    }

    public double[][] getNormalizedConfusionMatrix() {
        if (!initialized) {
            return null;
        }
        double[][] normalizedMatrix = new double[confusionMatrix.length][confusionMatrix.length];
        for (int i = 0; i < confusionMatrix.length; i++) {
            for (int j = 0; j < confusionMatrix.length; j++) {
                double iter = iterCount[i];
                normalizedMatrix[i][j] = iter == 0 ? 0 : confusionMatrix[i][j] / iterCount[i];
            }
        }
        return transpose(normalizedMatrix);
    }

    private int getMaxIndex(double[] actual) {
        double val = Double.MIN_VALUE;
        int index = 0;
        for (int i = 0; i < actual.length; i++) {
            if (actual[i] > val) {
                val = actual[i];
                index = i;
            }
        }
        return index;
    }

    private double[][] transpose(double[][] m) {
        double[][] tmp = new double[m.length][m.length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m.length; j++) {
                tmp[j][i] = m[i][j];
            }
        }
        return tmp;
    }
}


