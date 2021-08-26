package ch.kaiki.nn.neuralnet;

import org.junit.Test;
import ch.kaiki.nn.util.Optimizer;
import ch.kaiki.nn.util.Rectifier;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class PropertiesTest {

    private Rectifier rectifier;
    private Optimizer learningRateOptimizer;
    private double initialLearningRate;
    private double learningRate;
    private double learningRateMomentum;
    private int iterationCount;
    private Optimizer mutationRateOptimizer;
    private double initialMutationRate;
    private double mutationRate;
    private double mutationRateMomentum;

    private double[][] in = {{0, 0}, {1, 0}, {0, 1}, {1, 1}};
    private double[][] out = {{1, 0}, {0, 1}, {0, 1}, {1, 0}};


    @Test
    public void initialRandomizationTest() {

    }

    @Test
    public void cachedNodeTest() {
        NeuralNetwork neuralNetwork = new NeuralNetwork.Builder(2, 3, 2).build();
        neuralNetwork.predict(new double[]{1, 2});
        assertTrue(neuralNetwork.getCachedNodeValues().size() > 0);
    }

    @Test
    public void configurationTest() {
        int[] configuration = new int[]{2, 3, 2};
        NeuralNetwork neuralNetwork = new NeuralNetwork.Builder(configuration).build();
        assertTrue(Arrays.equals(configuration, neuralNetwork.getConfiguration()));
    }


}
