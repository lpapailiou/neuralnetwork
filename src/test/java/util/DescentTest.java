package util;

import neuralnet.NeuralNetwork;
import org.junit.Test;

public class DescentTest {

    @Test
    public void descentTest() {
        NeuralNetwork net = new NeuralNetwork(2,2,2);
        net.setLearningRate(1);
        net.setMutationRate(1);
        net.setLearningRateDescent(Descent.SGD);
        net.setLearningRateMomentum(0.005);
        net.setMutationRateDescent(Descent.SGD);
        net.setMutationRateMomentum(0.005);

        for (int i = 0; i < 100; i++) {
            net.decreaseRate();
        }

        System.out.println(net.getLearningRate() + " " + net.getMutationRate());

    }



}
