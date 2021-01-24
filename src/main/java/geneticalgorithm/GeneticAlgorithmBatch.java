package geneticalgorithm;

import neuralnet.NeuralNetwork;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Properties;

public class GeneticAlgorithmBatch {

    private int generationCount = Integer.MAX_VALUE;
    private int currentGenerationId;
    private GeneticAlgorithmGeneration currentGeneration;
    private final int populationSize;
    private NeuralNetwork neuralNetwork;
    static Properties properties = new Properties();

    public GeneticAlgorithmBatch(@NotNull NeuralNetwork neuralNetwork, int populationSize) {
        this.neuralNetwork = neuralNetwork;
        this.populationSize = populationSize;
        URL path = getClass().getClassLoader().getResource("geneticalgorithm.properties");
        File file = null;
        try {
            file = Paths.get(path.toURI()).toFile();
            properties.load(new FileInputStream(file));
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    public GeneticAlgorithmBatch(NeuralNetwork neuralNetwork, int populationSize, int generationCount) {
        this(neuralNetwork, populationSize);
        this.generationCount = generationCount;
    }

    public NeuralNetwork processGeneration() {
        currentGeneration = new GeneticAlgorithmGeneration(currentGenerationId, populationSize);
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

    public void setGeneticAlgorithmObjectTemplate(String templateName) {
        properties.setProperty("geneticAlgorithmObjectTemplate", templateName);
    }
}
