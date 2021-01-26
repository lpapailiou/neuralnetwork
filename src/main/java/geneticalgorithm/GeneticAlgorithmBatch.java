package geneticalgorithm;

import neuralnet.NeuralNetwork;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This class allows an easy handling of the genetic algorithm. It will prepare according populations and allows processing generation by generation.
 * Generations will be processed in parallel in order to improve processing time. After a generation is run, NeuralNetworks can be extracted.
 * @param <T> the type of the GeneticAlgorithmObject to be used.
 */
public class GeneticAlgorithmBatch<T> {

    private Properties properties = new Properties();

    private Constructor<T> geneticAlgorithmObjectConstructor;
    private NeuralNetwork neuralNetwork;
    private int generationCount = Integer.MAX_VALUE;
    private int currentGenerationId;
    private GeneticAlgorithmGeneration<T> currentGeneration;
    private final int populationSize;

    /**
     * Constructor to create a new batch. With given parameters, the genetic algorithm will be able to run.
     * It additionally loads the geneticalgorithm.properties file which can be configured to modify the genetic algorithm.
     * @param templateGeneticAlgorithmObject the type which implements the actual logic of the genetic algorithm.
     * @param seedNeuralNetwork the NeuralNetwork to be seeded for the first population.
     * @param populationSize the population size for the genetic algorithm.
     */
    public GeneticAlgorithmBatch(@NotNull Class<T> templateGeneticAlgorithmObject, @NotNull NeuralNetwork seedNeuralNetwork, int populationSize) {
        try {
            geneticAlgorithmObjectConstructor = templateGeneticAlgorithmObject.getDeclaredConstructor(NeuralNetwork.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Wrong generic class given. Must have constructor with argument NeuralNetwork!", e);
        }
        this.neuralNetwork = seedNeuralNetwork;
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

    public GeneticAlgorithmBatch(Class<T> templateGeneticAlgorithmObject, NeuralNetwork neuralNetwork, int populationSize, int generationCount) {
        this(templateGeneticAlgorithmObject, neuralNetwork, populationSize);
        this.generationCount = generationCount;
    }

    public NeuralNetwork processGeneration() {
        currentGeneration = new GeneticAlgorithmGeneration<T>(properties, geneticAlgorithmObjectConstructor, currentGenerationId, populationSize);
        neuralNetwork = currentGeneration.runGeneration(neuralNetwork);
        if (currentGenerationId == generationCount) {
            return null;
        }
        currentGenerationId++;
        return neuralNetwork;
    }

    public NeuralNetwork getBestNeuralNetworkForReproduction() {
        return currentGeneration.getBestNeuralNetworkForReproduction();
    }

    public NeuralNetwork getBestNeuralNetwork() {
        return currentGeneration.getBestNeuralNetwork();
    }

    public List<NeuralNetwork> getBestNeuralNetworks(int count) {
        List<NeuralNetwork> networks = new ArrayList<>();
        int index = Math.min(count, populationSize);
        List<IGeneticAlgorithmObject> populationList = currentGeneration.getPopulationList();
        for (int i = 0; i < index; i++) {
            networks.add(populationList.get(i).getNeuralNetwork());
        }
        return networks;
    }

}
