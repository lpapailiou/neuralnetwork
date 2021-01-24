package geneticalgorithm;

import neuralnet.NeuralNetwork;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class GeneticAlgorithmObject<T> implements Comparable<GeneticAlgorithmObject<T>> {

    private NeuralNetwork neuralNetwork;

    public GeneticAlgorithmObject (NeuralNetwork neuralNetwork) {
        this.neuralNetwork = neuralNetwork;
    }

    protected List<Double> predict(@NotNull double[] inputValues) {
        return neuralNetwork.predict(inputValues);
    }

    NeuralNetwork getNeuralNetwork() {
        return neuralNetwork;
    }

    abstract long getFitness();

    abstract boolean executeStep();

    abstract boolean isImmature();

    abstract GeneticAlgorithmObject getGeneticAlgorithmObject(NeuralNetwork neuralNetwork);

    @Override
    public int compareTo(@NotNull GeneticAlgorithmObject<T> other) {
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
