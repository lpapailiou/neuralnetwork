package geneticalgorithm;

import neuralnet.NeuralNetwork;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class GeneticAlgorithmObject implements IGeneticAlgorithmObject {

    private NeuralNetwork neuralNetwork;

    public GeneticAlgorithmObject (NeuralNetwork neuralNetwork) {
        this.neuralNetwork = neuralNetwork;
    }

    @Override
    public List<Double> predict(@NotNull double[] inputValues) {
        return neuralNetwork.predict(inputValues);
    }

    @Override
    public NeuralNetwork getNeuralNetwork() {
        return neuralNetwork;
    }

    @Override
    public abstract long getFitness();

    @Override
    public abstract boolean isPerfectScore();

    @Override
    public abstract boolean apply();

    @Override
    public abstract boolean isImmature();

    @Override
    public String getLogMessage() {
        return "fitness: \t " + getFitness();
    }

    @Override
    public int compareTo(@NotNull IGeneticAlgorithmObject other) {
        long fitness = this.getFitness();
        long otherFitness = other.getFitness();
        if (fitness > otherFitness) {
            return 1;
        } else if (fitness < otherFitness) {
            return -1;
        }
        return 0;
    }

}
