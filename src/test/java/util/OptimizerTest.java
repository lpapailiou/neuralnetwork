package util;

import neuralnet.NeuralNetwork;
import org.junit.Test;

public class OptimizerTest {

    @Test
    public void descentTest() {
        NeuralNetwork net = new NeuralNetwork(2, 2, 2);
        net.setLearningRate(1);
        net.setMutationRate(1);
        net.setLearningRateOptimizer(Optimizer.SGD);
        net.setLearningRateMomentum(0.005);
        net.setMutationRateOptimizer(Optimizer.SGD);
        net.setMutationRateMomentum(0.005);

        for (int i = 0; i < 100; i++) {
            net.decreaseRate();
        }

        System.out.println(net.getLearningRate() + " " + net.getMutationRate());

    }


}
