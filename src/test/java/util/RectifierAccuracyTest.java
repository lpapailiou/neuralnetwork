package util;

import neuralnet.NeuralNetwork;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RectifierAccuracyTest {

    @Test
    public void rectifierTest() {
        double[][] in = {{0,0}, {1,0}, {0,1}, {1,1}};
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

        NeuralNetwork net = new NeuralNetwork(2, 4, 1).setLearningRate(0.9).setLearningRateDescent(Descent.NONE);
        net.setRectifier(rectifier);
        net.fit(in, out, 5000);

        System.out.println("rectifier: " + rectifier.getDescription());
        System.out.println(" - error: " + (net.predict(in[0]).get(0) + (1.0-net.predict(in[1]).get(0)) + (1.0-net.predict(in[2]).get(0)) + net.predict(in[3]).get(0))/4.0);
        System.out.println("  combo 1: " + net.predict(in[0]));
        System.out.println("  combo 2: " + net.predict(in[1]));
        System.out.println("  combo 3: " + net.predict(in[2]));
        System.out.println("  combo 4: " + net.predict(in[3]));
    }

    @Test
    public void xOrTest() {
        double[][] in = {{0,0}, {1,0}, {0,1}, {1,1}};
        double[][] out = {{0}, {1}, {1}, {0}};

        Rectifier rectifier = Rectifier.RELU;

        NeuralNetwork net = new NeuralNetwork(2, 4, 1);
        net.setRectifier(rectifier).setLearningRate(0.8).setLearningRateDescent(Descent.NONE);
        net.fit(in, out, 1000);

        System.out.println("test with rectifier: " + rectifier.getDescription());
        System.out.println("  combo 1: " + net.predict(in[0]));
        System.out.println("  combo 2: " + net.predict(in[1]));
        System.out.println("  combo 3: " + net.predict(in[2]));
        System.out.println("  combo 4: " + net.predict(in[3]));
    }
}
