package ch.kaiki.nn.neuralnet;

import org.junit.Test;

public class BackpropagationTest3 {
    double[][] in = {{0, 0}, {1, 0}, {0, 1}, {1, 1}};
    double[][] out = {{0}, {1}, {1}, {0}};


    @Test
    public void backpropagationTest() {
        NeuralNetwork neuralNetwork = new NeuralNetwork.Builder(2,3,1).build();
        neuralNetwork.fit(in, out, 1, 16);

    }
}
