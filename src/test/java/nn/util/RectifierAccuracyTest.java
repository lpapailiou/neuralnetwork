package nn.util;

import nn.neuralnet.NeuralNetwork;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class RectifierAccuracyTest {

    @Test
    public void rectifierTest() {
        double[][] in = {{0, 0}, {1, 0}, {0, 1}, {1, 1}};
        double[][] out = {{0}, {1}, {1}, {0}};


        testXOr(Rectifier.GELU, in, out);
        testXOr(Rectifier.IDENTITY, in, out);
        testXOr(Rectifier.RELU, in, out);
        testXOr(Rectifier.SIGMOID, in, out);
        testXOr(Rectifier.SIGMOID_ACCURATE, in, out);
        testXOr(Rectifier.SILU, in, out);
        testXOr(Rectifier.SILU_ACCURATE, in, out);
        testXOr(Rectifier.SOFTPLUS, in, out);
        testXOr(Rectifier.TANH, in, out);
    }

    void testXOr(Rectifier rectifier, double[][] in, double[][] out) {
        // testing xor function

        NeuralNetwork net = new NeuralNetwork(Initializer.RANDOM, 2, 2, 1).setLearningRate(0.5).setLearningRateOptimizer(Optimizer.NONE);
        net.setRectifier(rectifier);
        net.fit(in, out, 50000);

        System.out.println("rectifier: " + rectifier.getDescription());
        List<Double> set0 = net.predict(in[0]);
        List<Double> set1 = net.predict(in[1]);
        List<Double> set2 = net.predict(in[2]);
        List<Double> set3 = net.predict(in[3]);
        System.out.println(evaluate(set0.get(0), set1.get(0), set2.get(0), set3.get(0)));
        //System.out.println(" - error: " + (set0.get(0) + (1.0-set1.get(0)) + (1.0-set2.get(0)) + set3.get(0))/4.0);

        System.out.println("  combo 1: " + set0.get(0));
        System.out.println("  combo 2: " + set1.get(0));
        System.out.println("  combo 3: " + set2.get(0));
        System.out.println("  combo 4: " + set3.get(0));
    }

    String evaluate(double set0, double set1, double set2, double set3) {
        int TP = 0;
        int TN = 0;
        int FP = 0;
        int FN = 0;
        if (set0 < 0.5) {
            TN++;
        } else {
            FP++;
        }
        if (set3 < 0.5) {
            TN++;
        } else {
            FP++;
        }
        if (set1 >= 0.5) {
            TP++;
        } else {
            FN++;
        }
        if (set2 >= 0.5) {
            TP++;
        } else {
            FN++;
        }
        String data = "TP=" + TP + ", TN=" + TN + ", FP=" + FP + ", FN=" + FN;
        String accuracy = "accuracy: " + (TP + TN) / (TP + TN + FP + FN);

        String recall = "recall: " + (TP == 0 ? 1 : TP / (TP + FN));
        String precision = "precision: " + (TP == 0 && FP == 0 ? 0 : TP / (TP + FP));

        return data + " \n" + accuracy + " \n" + recall + " \n" + precision;

    }

    boolean getSuccess(List<Double> result, int eval) {
        double max = Collections.max(result);
        System.out.println(result);
        return result.get(eval) == max;
    }


    public void xOrTest() {
        double[][] in = {{0, 0}, {1, 0}, {0, 1}, {1, 1}};
        double[][] out = {{0}, {1}, {1}, {0}};

        Rectifier rectifier = Rectifier.RELU;

        NeuralNetwork net = new NeuralNetwork(2, 4, 1);
        net.setRectifier(rectifier).setLearningRate(0.8).setLearningRateOptimizer(Optimizer.NONE);
        net.fit(in, out, 1000);

        System.out.println("test with rectifier: " + rectifier.getDescription());
        System.out.println("  combo 1: " + net.predict(in[0]));
        System.out.println("  combo 2: " + net.predict(in[1]));
        System.out.println("  combo 3: " + net.predict(in[2]));
        System.out.println("  combo 4: " + net.predict(in[3]));
    }
}
