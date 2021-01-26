package geneticalgorithm;

import neuralnet.NeuralNetwork;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IGeneticAlgorithmObject extends Comparable<IGeneticAlgorithmObject> {

    List<Double> predict (@NotNull double[] inputValues);

    NeuralNetwork getNeuralNetwork();

    long getFitness();

    boolean isPerfectScore();

    boolean apply();

    boolean isImmature();

    String getLogMessage();

}
