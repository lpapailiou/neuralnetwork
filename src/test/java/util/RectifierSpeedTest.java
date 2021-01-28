package util;

import neuralnet.NeuralNetwork;
import org.junit.Test;

public class RectifierSpeedTest {

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

        NeuralNetwork net = new NeuralNetwork(2, 4, 1);
        net.setRectifier(rectifier);

        long startTime = System.currentTimeMillis();
        net.train(in, out, 100000);
        long duration = System.currentTimeMillis() - startTime;

        System.out.println("rectifier: " + rectifier.getDescription());
        System.out.println(" - speed: " + duration);
    }


}
