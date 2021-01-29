package neuralnet;

import org.junit.Test;

import java.util.List;

import static util.NetUtils.getStandardizedOutputList;
import static org.junit.Assert.assertEquals;

public class NeuralTest {

    @Test
    public void toStringTest() {
        NeuralNetwork net = new NeuralNetwork(2, 15, 15, 1);
        System.out.println(net.toString());
    }

    //@Test
    public void testXOr() {
        // testing xor function
        double[][] in = {{0,0}, {1,0}, {0,1}, {1,1}};
        double[][] out = {{0}, {1}, {1}, {0}};
        NeuralNetwork net = new NeuralNetwork(2, 15, 15, 1);
        net.fit(in, out, 4000);

        //assertEquals(net.predict(in[0]), net.predict(in[0]), 0.2);
        List<Double> expected = getStandardizedOutputList(net.predict(in[0]));
        List<Double> actual = net.predict(in[0]);
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i), 0.2);
        }

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
