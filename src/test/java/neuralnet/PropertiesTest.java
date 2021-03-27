package neuralnet;

import org.junit.Assert;
import org.junit.Test;
import util.Descent;
import util.Rectifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PropertiesTest {


    private boolean normalize;
    private Rectifier rectifier;
    private Descent learningRateDescent;
    private double initialLearningRate;
    private double learningRate;
    private double learningRateMomentum;
    private int iterationCount;
    private Descent mutationRateDescent;
    private double initialMutationRate;
    private double mutationRate;
    private double mutationRateMomentum;

    private double[][] in = {{0,0}, {1,0}, {0,1}, {1,1}};
    private double[][] out = {{1,0}, {0,1}, {0,1}, {1,0}};


    @Test
    public void normlizationTest() {
        NeuralNetwork neuralNetwork = new NeuralNetwork(2,3,2);
        neuralNetwork.setNormalized(true);
        neuralNetwork.fit(in, out, 5000);
        double[][] data = neuralNetwork.getWeights(0);
        for (double[] a : data) {
            for (double b : a) {
                assertTrue(b >= -1 && b <= 1);
            }
        }
    }

    @Test
    public void initialRandomizationTest() {
        double initialRand = Double.parseDouble(NeuralNetwork.getProperty("initial_randomization"));
        NeuralNetwork neuralNetwork = new NeuralNetwork(0.5, 2,3,2);
        assertTrue(neuralNetwork.getInitialRandomization() == 0.5);
        assertTrue(new NeuralNetwork(2,3,2).getInitialRandomization() == 1);
        assertEquals(0.5, neuralNetwork.copy().getInitialRandomization(), 0.0);
        NeuralNetwork.setProperty("initial_randomization", "0.3");
        assertTrue(neuralNetwork.copy().getInitialRandomization() == 0.5);
        assertTrue(new NeuralNetwork(2,3,2).getInitialRandomization() == 0.3);
        NeuralNetwork.setProperty("initial_randomization", String.valueOf(initialRand));
    }

    @Test
    public void cachedNodeTest() {
        NeuralNetwork neuralNetwork = new NeuralNetwork(2,3,2);
        neuralNetwork.predict(new double[] {1,2});
        assertTrue(neuralNetwork.getCachedNodeValues().size() > 0);
    }

    @Test
    public void configurationTest() {
        int[] configuration = new int[] {2,3,2};
        NeuralNetwork neuralNetwork = new NeuralNetwork(configuration);
        assertTrue(Arrays.equals(configuration, neuralNetwork.getConfiguration()));
    }


}
