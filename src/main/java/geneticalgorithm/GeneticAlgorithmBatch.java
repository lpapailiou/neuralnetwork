package geneticalgorithm;

import neuralnet.NeuralNetwork;
import org.jetbrains.annotations.NotNull;

public class GeneticAlgorithmBatch {

    private int generationCount = Integer.MAX_VALUE;
    private int currentGenerationId;
    private GeneticAlgorithmGeneration currentGeneration;
    private final int populationSize;
    private NeuralNetwork neuralNetwork;
    private GeneticAlgorithmObject templateObject;

    public GeneticAlgorithmBatch(@NotNull NeuralNetwork neuralNetwork, GeneticAlgorithmObject templateObject, int populationSize) {
        this.neuralNetwork = neuralNetwork;
        this.populationSize = populationSize;
        this.templateObject = templateObject;
    }

    public GeneticAlgorithmBatch(NeuralNetwork neuralNetwork, GeneticAlgorithmObject templateObject, int populationSize, int generationCount) {
        this(neuralNetwork, templateObject, populationSize);
        this.generationCount = generationCount;
    }

    public NeuralNetwork processGeneration() {
        currentGeneration = new GeneticAlgorithmGeneration(currentGenerationId, templateObject, populationSize);
        neuralNetwork = currentGeneration.runGeneration(neuralNetwork);
        if (currentGenerationId == generationCount) {
            return null;
        }
        currentGenerationId++;
        return neuralNetwork;
    }

    public NeuralNetwork getBestNeuralNetwork() {
        return currentGeneration.getBestNeuralNetwork();
    }
}
