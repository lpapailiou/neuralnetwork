package neuralnet;

import org.junit.Test;

import java.util.List;

public class NeuralTest {

    @Test
    public void testXOr() {
        // testing xor function
        double[][] in = {{0,0}, {1,0}, {0,1}, {1,1}};
        double[][] out = {{0}, {1}, {1}, {0}};
        NeuralNetwork net = new NeuralNetwork(2, 15, 15, 1);
        net.train(in, out, 4000);
        System.out.println("test: ");
        System.out.println("combo 1: " + net.predict(in[0]));
        System.out.println("combo 2: " + net.predict(in[1]));
        System.out.println("combo 3: " + net.predict(in[2]));
        System.out.println("combo 4: " + net.predict(in[3]));
    }

    public void testNeurons() {
        NeuralNetwork net = new NeuralNetwork(3, 4, 7, 2);
        List<Double> out = net.predict(new double[] {1, 2, 3});
        System.out.println(out);


    }
}
