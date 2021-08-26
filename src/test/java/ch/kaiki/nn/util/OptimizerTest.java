package ch.kaiki.nn.util;

import ch.kaiki.nn.neuralnet.NeuralNetwork;
import org.junit.Test;

public class OptimizerTest {

    @Test
    public void descentTest() {
        NeuralNetwork net = new NeuralNetwork.Builder(2, 2, 2)
        .setLearningRate(1)
        .setMutationRate(1)
        .setLearningRateOptimizer(Optimizer.SGD)
        .setLearningRateMomentum(0.005)
        .setMutationRateOptimizer(Optimizer.SGD)
        .setMutationRateMomentum(0.005).build();

        for (int i = 0; i < 100; i++) {
            net.decreaseRate();
        }

        System.out.println(net.getLearningRate() + " " + net.getMutationRate());

    }


}
