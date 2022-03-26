package ch.kaiki.nn.neuralnet;

import ch.kaiki.nn.util.Optimizer;
import org.junit.Test;

public class BackpropagationTest2 {

    @Test
    public void backpropagationTest() {
        double[][] in = {{0, 0}, {1, 0}, {0, 1}, {1, 1}};
        double[][] out = {{0}, {1}, {1}, {0}};

        NeuralNetwork net = new NeuralNetwork.Builder(2,3,1).setLearningRate(0.5).setCostFunction(CostFunction.MSE).setLearningRateOptimizer(Optimizer.NONE).build();
/*
        net.layers.get(0).weight = new Matrix(new double[][]{{-0.78339129, -0.88811407},{-0.29665889, -0.92474611},{ 0.16606784,  0.34511362}});
        net.layers.get(0).bias = new Matrix(new double[][]{{-0.07903282},{0.48223371},{0.00204993}});
        net.layers.get(1).weight = new Matrix(new double[][]{{0.40470524, -0.53871819,  0.48922482}});
        net.layers.get(1).bias = new Matrix(new double[][]{{0.0032744}});
        */
        net.fit(in, out, 10, 16);

        System.out.println("prediction: " + net.predict(in[0]));
        System.out.println("prediction: " + net.predict(in[1]));
        System.out.println("prediction: " + net.predict(in[2]));
        System.out.println("prediction: " + net.predict(in[3]));

        // sigmoid(sigmoid([0.05,0.1]*[[0.15,0.2],[0.25,0.3]] + [0.35,0.35]) * [[0.4,0.45],[0.5,0.55]] + [0.6,0.6])
        // sigmoid(sigmoid([x,y]*[[0.15,0.2],[0.25,0.3]] + [0.35,0.35]) * [[0.4,0.45],[0.5,0.55]] + [0.6,0.6])
        //sigmoid([0.05,0.1]*[[0.15,0.2],[0.25,0.3]] + [0.35,0.35])
        //sigmoid([0.594476, 0.596283] * [[0.4,0.45],[0.5,0.55]] + [0.6,0.6])

        System.out.println();
        System.out.println(net);
        //net.fit(in, out, 10000);
        //System.out.println(net);
        //System.out.println(net.predict(in[0]));
        // [0.050082222480079, 0.9230743974669915] [0.035188665258208664, 0.9486266566463437]
        // [0.04486741145720708, 0.9383927700854706] [0.03217588378403173, 0.956571608301561]

    }
}
