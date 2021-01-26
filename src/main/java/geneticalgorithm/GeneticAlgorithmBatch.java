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

public class GeneticAlgorithmBatch {

    private int generationCount = Integer.MAX_VALUE;
    private int currentGenerationId;
    private GeneticAlgorithmGeneration currentGeneration;
    private Constructor constructor;
    private final int populationSize;
    private NeuralNetwork neuralNetwork;
    private Properties properties = new Properties();

    public GeneticAlgorithmBatch(@NotNull Class<?> type, @NotNull NeuralNetwork neuralNetwork, int populationSize) {
        try {
             constructor = type.getDeclaredConstructor(NeuralNetwork.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Wrong generic class given. Must have constructor with argument NeuralNetwork!", e);
        }
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

    public GeneticAlgorithmBatch(Class<?> type, NeuralNetwork neuralNetwork, int populationSize, int generationCount) {
        this(type, neuralNetwork, populationSize);
        this.generationCount = generationCount;
    }

    public NeuralNetwork processGeneration() {
        currentGeneration = new GeneticAlgorithmGeneration(properties, constructor, currentGenerationId, populationSize);
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

    public NeuralNetwork getBestNeuralNetworkForReproduction() {
        return currentGeneration.getBestNeuralNetworkForReproduction();
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
