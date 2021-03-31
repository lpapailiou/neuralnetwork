package neuralnet;

import org.junit.Assert;
import org.junit.Test;
import util.Descent;
import util.Initializer;
import util.Rectifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PropertiesTest {

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
    public void initialRandomizationTest() {

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
