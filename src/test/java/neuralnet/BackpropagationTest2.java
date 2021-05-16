package neuralnet;

import org.junit.Test;
import util.Rectifier;

import java.util.ArrayList;
import java.util.List;

public class BackpropagationTest2 {

    @Test
    public void backpropagationTest() {
        double[][] in = {{0.05,0.1}};
        double[][] out = {{0.01,0.99}};
/*
        NeuralNetwork net = new NeuralNetwork(2,2,2);
        net.setCostFunction(CostFunction.MSE_NAIVE);
        net.setLearningRate(0.5);
        net.layers.get(0).weight = new Matrix(new double[][]{{0.15,0.2},{0.25,0.3}});
        net.layers.get(0).bias = new Matrix(new double[][]{{0.35},{0.35}});
        net.layers.get(1).weight = new Matrix(new double[][]{{0.4,0.45},{0.5,0.55}});
        net.layers.get(1).bias = new Matrix(new double[][]{{0.6},{0.6}});
        System.out.println(net);
        System.out.println("prediction: " + net.predict(in[0]) + "\n");

        // sigmoid(sigmoid([0.05,0.1]*[[0.15,0.2],[0.25,0.3]] + [0.35,0.35]) * [[0.4,0.45],[0.5,0.55]] + [0.6,0.6])
        // sigmoid(sigmoid([x,y]*[[0.15,0.2],[0.25,0.3]] + [0.35,0.35]) * [[0.4,0.45],[0.5,0.55]] + [0.6,0.6])
        //sigmoid([0.05,0.1]*[[0.15,0.2],[0.25,0.3]] + [0.35,0.35])
        //sigmoid([0.594476, 0.596283] * [[0.4,0.45],[0.5,0.55]] + [0.6,0.6])


        net.fit(in, out, 10000);
        System.out.println(net);
        System.out.println(net.predict(in[0]));
        // [0.050082222480079, 0.9230743974669915] [0.035188665258208664, 0.9486266566463437]
        // [0.04486741145720708, 0.9383927700854706] [0.03217588378403173, 0.956571608301561]
*/
    }
}
