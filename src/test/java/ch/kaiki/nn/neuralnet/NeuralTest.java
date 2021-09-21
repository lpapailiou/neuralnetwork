package ch.kaiki.nn.neuralnet;

import org.junit.Test;
import ch.kaiki.nn.util.Initializer;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class NeuralTest {

    //@Test
    public void toStringTest() {
        NeuralNetwork net = new NeuralNetwork.Builder(2, 15, 15, 1).setInitializer(Initializer.XAVIER).build();
        System.out.println(net.toString());
    }

    @Test
    public void testXOr() {
        // testing xor function
        double[][] in = {{0, 0}, {1, 0}, {0, 1}, {1, 1}};
        double[][] out = {{0}, {1}, {1}, {0}};
        NeuralNetwork net = new NeuralNetwork.Builder(2, 15, 15, 1).build();
        net.fit(in, out, 1000, 16);

        for (int i = 0; i < in.length; i++) {
            double expected = out[i][0];
            double actual = net.predict(in[i]).get(0);
            System.out.println("expected: " + expected + ", actual: " + actual);
            //assertEquals(expected, actual, 0.3);
        }

        System.out.println(net);
    }

    public void testNeurons() {
        NeuralNetwork net = new NeuralNetwork.Builder(3, 4, 7, 2).build();
        List<Double> out = net.predict(new double[]{1, 2, 3});
        System.out.println(out);


    }
}
