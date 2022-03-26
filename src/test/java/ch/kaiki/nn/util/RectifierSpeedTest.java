package ch.kaiki.nn.util;

import ch.kaiki.nn.neuralnet.NeuralNetwork;
import org.junit.Test;

public class RectifierSpeedTest {

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

        NeuralNetwork net = new NeuralNetwork.Builder(2, 64, 64, 64, 1).setDefaultRectifier(rectifier).build();

        long startTime = System.currentTimeMillis();
        net.fit(in, out, 2, 16);
        long duration = System.currentTimeMillis() - startTime;

        System.out.println("rectifier: " + rectifier.getDescription());
        System.out.println(" - speed: " + duration);
    }


}
