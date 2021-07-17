package data;

import java.util.SortedMap;
import java.util.TreeMap;

public class BackPropData {
    private SortedMap<Integer, BackPropEntity> iterationMap = new TreeMap<>();
    private double costSum;
    private int tpSum;
    private int fpSum;
    private int tnSum;
    private int fnSum;

    public void add(int iteration, double cost, double[] actual, double[] expected) {
        int tp = 0;
        int fp = 0;
        int tn = 0;
        int fn = 0;
        costSum += cost;
        for (int i = 0; i < actual.length; i++) {
            if (actual[i] > 0.5) {
                if (expected[i] > 0.5) {
                    tpSum++;
                    tp++;
                } else {
                    fpSum++;
                    fp++;
                }
            } else {
                if (expected[i] > 0.5) {
                    fn++;
                    fnSum++;
                } else {
                    tnSum++;
                    tn++;
                }
            }
        }

        BackPropEntity backPropEntity = new BackPropEntity(cost, tp, fp, tn, fn, costSum, tpSum, fpSum, tnSum, fnSum);
        iterationMap.put(iteration, backPropEntity);
    }

    public SortedMap<Integer, BackPropEntity> getMap() {
        return iterationMap;
    }
}


