package ch.kaiki.nn.genetic;

import ch.kaiki.nn.neuralnet.NeuralNetwork;

import java.util.List;

public interface IGene {

    /**
     * Getter for the IGene properties.
     *
     * @param key the key of the property.
     * @return the value of the according property.
     */
    String getProperty(String key);

    IGene crossover(List<IGene> genes);

    IGene initialize();

    IGene mutate();

    void decreaseRate();

    List<Double> predict(double[] inputNodes);
}
