package ch.kaiki.nn.data;

import java.io.Serializable;

public class BackPropEntity implements Serializable {
    private static final long serialVersionUID = 2L;
    private double cost;
    private double accuracy;
    private double recall;
    private double precision;
    private double f1;
    private double costSum;
    private double accuracySum;
    private double recallSum;
    private double precisionSum;
    private double f1Sum;

    public BackPropEntity(double cost, double tp, double fp, double tn, double fn, double costSum, double tpSum, double fpSum, double tnSum, double fnSum) {
        this.cost = cost;
        accuracy = (tp + tn) / (tp + tn + fp + fn);
        recall = (tp == 0 ? 1 : tp / (tp + fn));
        precision = (tp == 0 && fp == 0 ? 0 : tp / (tp + fp));
        f1 = 2 * (precision * recall) / (precision + recall);
        this.costSum = costSum;
        accuracySum = (tpSum + tnSum) / (tpSum + tnSum + fpSum + fnSum);
        recallSum = (tpSum == 0 ? 1 : tpSum / (tpSum + fnSum));
        precisionSum = (tpSum == 0 && fpSum == 0 ? 0 : tpSum / (tpSum + fpSum));
        f1Sum = 2 * (precisionSum * recallSum) / (precisionSum + recallSum);
    }

    public double getCost() {
        return cost;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public double getPrecision() {
        return precision;
    }

    public double getRecall() {
        return recall;
    }

    public double getF1() {
        return f1;
    }

    public double getCostSum() {
        return costSum;
    }

    public double getAccuracySum() {
        return accuracySum;
    }

    public double getPrecisionSum() {
        return precisionSum;
    }

    public double getRecallSum() {
        return recallSum;
    }

    public double getF1Sum() {
        return f1Sum;
    }
}
