package geneticalgorithm;

import neuralnet.NeuralNetwork;
import org.jetbrains.annotations.NotNull;

public class GeneticAlgorithmBatch {

    private int generationCount = Integer.MAX_VALUE;
    private int currentGenerationId;
    private final int populationSize;
    private NeuralNetwork neuralNetwork;

    public GeneticAlgorithmBatch(@NotNull NeuralNetwork neuralNetwork, int populationSize) {
        this.neuralNetwork = neuralNetwork;
        this.populationSize = populationSize;
    }

    public GeneticAlgorithmBatch(NeuralNetwork neuralNetwork, int populationSize, int generationCount) {
        this(neuralNetwork, populationSize);
        this.generationCount = generationCount;
    }

    public NeuralNetwork processGeneration() {
        GeneticAlgorithmGeneration generation = new GeneticAlgorithmGeneration(currentGenerationId, populationSize);
        neuralNetwork = generation.runGeneration(neuralNetwork);
        if (currentGenerationId == generationCount) {
            return null;
        }
        currentGenerationId++;
        return neuralNetwork;
    }
}
