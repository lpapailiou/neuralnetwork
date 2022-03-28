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
    private int reproductionSpecimenCount = -1;
    private double reproductionPoolSize = -1;

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
            reproductionSpecimenCount = Integer.parseInt(NeuralNetwork.Builder.getProperty("genetic_reproduction_specimen_count"));
        } catch (Exception e) {
            LOG.log(Level.INFO, "Could not load property 'genetic_reproduction_specimen_count' from neuralnetwork.properties!", e);
        }
        try {
            reproductionPoolSize = Double.parseDouble(NeuralNetwork.Builder.getProperty("genetic_reproduction_pool_size"));
        } catch (Exception e) {
            LOG.log(Level.INFO, "Could not load property 'genetic_reproduction_pool_size' from neuralnetwork.properties!", e);
        }
        if (reproductionSpecimenCount < 1 || reproductionSpecimenCount > populationSize) {
            throw new IllegalArgumentException("reproduction specimen count must be set to at least 1 and it must not exceed population size!");
        }
        if (reproductionPoolSize <= 0 || reproductionPoolSize > 1) {
            throw new IllegalArgumentException("reproduction pool size must have a value larger than 0.0 and less or equal to 1.0!");
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
        if (currentGenerationId == generationCount) {
            return null;
        }
        currentGeneration = new GeneticAlgorithmGeneration<>(geneticAlgorithmObjectConstructor, currentGenerationId, reproductionSpecimenCount, populationSize, reproductionPoolSize);
        seedNeuralNetwork = currentGeneration.runGeneration(seedNeuralNetwork);
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
     * Returns current reproduction specimen count for the genetic algorithm.
     *
     * @return the reproduction specimen count.
     */
    public int getReproductionSpecimenCount() {
        return reproductionSpecimenCount;
    }

    /**
     * Sets reproduction specimen count for the genetic algorithm. Must be at least 1 and must not exceed population size.
     *
     * @param reproductionSpecimenCount the count of neural networks to be merged for reproduction.
     * @return the current genetic algorithm batch.
     */
    public GeneticAlgorithmBatch<T> setReproductionSpecimenCount(int reproductionSpecimenCount) {
        if (reproductionSpecimenCount < 1 || reproductionSpecimenCount > populationSize) {
            throw new IllegalArgumentException("reproduction specimen count must be set to at least 1 and it must not exceed population size!");
        }
        this.reproductionSpecimenCount = reproductionSpecimenCount;
        return this;
    }

    /**
     * Returns current reproduction pool size for the genetic algorithm.
     *
     * @return the reproduction pool size.
     */
    public double getReproductionPoolSize() {
        return reproductionPoolSize;
    }

    /**
     * Sets reproduction pool size for the genetic algorithm. Must be greater than 0.0 and less or equal to 1.0.
     * @param reproductionPoolSize the size of the reproduction pool as percentage.
     * @return the current genetic algorithm batch.
     */
    public GeneticAlgorithmBatch<T> setReproductionPoolSize(double reproductionPoolSize) {
        if (reproductionPoolSize <= 0 || reproductionPoolSize > 1) {
            throw new IllegalArgumentException("reproduction specimen count must be set to at least 1 and it must not exceed population size!");
        }
        this.reproductionPoolSize = reproductionPoolSize;
        return this;
    }

}
