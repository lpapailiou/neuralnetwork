package ch.kaiki.nn.genetic;

import ch.kaiki.nn.neuralnet.NeuralNetwork;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class allows an easy handling of the genetic algorithm. It will prepare according populations and allows processing generation by generation.
 * Generations will be processed in parallel in order to improve processing time. After a generation is run, NeuralNetworks can be extracted.
 *
 * @param <T> the type of the GeneticAlgorithmObject to be used.
 */
public class GeneticAlgorithmBatch<T> {
    private static final Logger LOG = Logger.getLogger("GeneticAlgorithmBatch logger");
    private final int populationSize;
    private Constructor<T> geneticAlgorithmObjectConstructor;
    private NeuralNetwork seedNeuralNetwork;
    private int generationCount = Integer.MAX_VALUE;
    private int currentGenerationId;
    private GeneticAlgorithmGeneration<T> currentGeneration;
    private int reproductionPoolSize;

    /**
     * Constructor to create a new batch. With given parameters, the genetic algorithm will be able to run.
     * It additionally loads the neuralnetwork.properties file from the resources, which can be configured
     * to modify the genetic algorithm.
     *
     * @param templateGeneticAlgorithmObject the type which implements the actual logic of the genetic algorithm.
     * @param seedNeuralNetwork              the NeuralNetwork to be seeded for the first population.
     * @param populationSize                 the population size for the genetic algorithm.
     */
    public GeneticAlgorithmBatch(@NotNull Class<T> templateGeneticAlgorithmObject, @NotNull NeuralNetwork seedNeuralNetwork, int populationSize) {
        try {
            geneticAlgorithmObjectConstructor = templateGeneticAlgorithmObject.getDeclaredConstructor(NeuralNetwork.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Wrong class definition of first passed argument templateGeneticAlgorithmObject. Must have constructor with single argument NeuralNetwork!", e);
        }
        this.seedNeuralNetwork = seedNeuralNetwork;
        if (populationSize < 1) {
            throw new IllegalArgumentException("Population size must be larger than 0!");
        }
        this.populationSize = populationSize;

        try {
            reproductionPoolSize = Integer.parseInt(NeuralNetwork.Builder.getProperty("genetic_reproduction_pool_size"));
        } catch (Exception e) {
            LOG.log(Level.INFO, "Could not load property 'genetic_reproduction_pool_size' from neuralnetwork.properties!", e);
        } finally {
            if (reproductionPoolSize == 0) {
                reproductionPoolSize = (int) (populationSize * 0.2);
                reproductionPoolSize = Math.max(reproductionPoolSize, 2);
            }
        }
        if (reproductionPoolSize < 1 || reproductionPoolSize > populationSize) {
            throw new IllegalArgumentException("reproduction pool must be set to at least 1 in neuralnetwork.properties and it must not exceed population size!");
        }
    }

    /**
     * Constructor to create a new batch. With given parameters, the genetic algorithm will be able to run.
     * It additionally loads the neuralnetwork.properties file which can be configured to modify the genetic algorithm.
     *
     * @param templateGeneticAlgorithmObject the type which implements the actual logic of the genetic algorithm.
     * @param seedNeuralNetwork              the NeuralNetwork to be seeded for the first population.
     * @param populationSize                 the population size for the genetic algorithm.
     * @param generationCount                the maximum number of generations for this batch.
     */
    public GeneticAlgorithmBatch(Class<T> templateGeneticAlgorithmObject, NeuralNetwork seedNeuralNetwork, int populationSize, int generationCount) {
        this(templateGeneticAlgorithmObject, seedNeuralNetwork, populationSize);
        this.generationCount = generationCount;
    }

    /**
     * This method will create a new generation and process it afterwards.
     *
     * @return the best NeuralNetwork for reproduction (i.e. the seed for a new generation).
     */
    public NeuralNetwork processGeneration() {
        currentGeneration = new GeneticAlgorithmGeneration<>(geneticAlgorithmObjectConstructor, currentGenerationId, reproductionPoolSize, populationSize);
        seedNeuralNetwork = currentGeneration.runGeneration(seedNeuralNetwork);
        if (currentGenerationId == generationCount) {
            return null;
        }
        currentGenerationId++;
        return seedNeuralNetwork;
    }

    /**
     * This method will return the current NeuralNetwork considered best for creating the next generation.
     *
     * @return the best NeuralNetwork for reproduction (i.e. the seed for a new generation).
     */
    public NeuralNetwork getBestNeuralNetworkForReproduction() {
        if (currentGeneration == null) {
            return seedNeuralNetwork;
        }
        return currentGeneration.getBestNeuralNetworkForReproduction();
    }

    /**
     * This method will return the current NeuralNetwork which performed best in the most recent generation.
     *
     * @return the best NeuralNetwork or null if no generation was processed yet.
     */
    public NeuralNetwork getBestNeuralNetwork() {
        if (currentGeneration == null) {
            return null;
        }
        return currentGeneration.getBestNeuralNetwork();
    }

    /**
     * This method will return the best NeuralNetworks which performed best in the most recent generation.
     *
     * @param count the count of NeuralNetworks to be returned.
     * @return a List of the best NeuralNetworks or an empty List if no generation was processed yet.
     */
    public List<NeuralNetwork> getBestNeuralNetworks(int count) {
        List<NeuralNetwork> networks = new ArrayList<>();
        if (currentGeneration == null) {
            return networks;
        }
        int index = Math.min(count, populationSize);
        List<IGeneticAlgorithmObject> populationList = currentGeneration.getPopulationList();
        for (int i = 0; i < index; i++) {
            networks.add(populationList.get(i).getNeuralNetwork());
        }
        return networks;
    }

    /**
     * Returns current reproduction pool size for the genetic algorithm. Must not match corresponding property.
     *
     * @return the reproduction pool size.
     */
    public int getReproductionPoolSize() {
        return reproductionPoolSize;
    }

    /**
     * Sets reproduction pool size for the genetic algorithm.
     *
     * @param reproductionPoolSize the count of neural networks to be merged for reproduction.
     * @return the current genetic algorithm batch.
     */
    public GeneticAlgorithmBatch<T> setReproductionPoolSize(int reproductionPoolSize) {
        if (reproductionPoolSize < 1 || reproductionPoolSize > populationSize) {
            throw new IllegalArgumentException("reproduction pool must be set to at least 1 and it must not exceed population size!");
        }
        this.reproductionPoolSize = reproductionPoolSize;
        return this;
    }

}
