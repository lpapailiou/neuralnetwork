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
 * Generations will be processed in parallel in order to improve processing time. After a generation is run, genes can be extracted.
 *
 * @param <T> the type of the GeneticObject to be used.
 * @param <U> the type of the Gene to be used.
 */
public class GeneticBatch<T, U> {
    private static final Logger LOG = Logger.getLogger("GeneticBatch logger");
    private final int populationSize;
    private Constructor<T> geneticAlgorithmObjectConstructor;
    private IGene seed;
    private int generationCount = Integer.MAX_VALUE;
    private int currentGenerationId;
    private Generation<T> currentGeneration;
    private int parentCount = -1;
    private double reproductionPoolSize = -1;

    /**
     * Constructor to create a new batch. With given parameters, the genetic algorithm will be able to run.
     * It additionally loads the neuralnetwork.properties file from the resources, which can be configured
     * to modify the genetic algorithm.
     *
     * @param templateGeneticAlgorithmObject the type which implements the actual logic of the genetic algorithm.
     * @param geneClass                      the type which implements the gene.
     * @param seed                           the Gene to be seeded for the first population.
     * @param populationSize                 the population size for the genetic algorithm.
     */
    public GeneticBatch(@NotNull Class<T> templateGeneticAlgorithmObject, Class<U> geneClass, @NotNull IGene seed, int populationSize) {
        try {
            geneticAlgorithmObjectConstructor = templateGeneticAlgorithmObject.getDeclaredConstructor(geneClass);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Wrong class definition of first passed argument templateGeneticAlgorithmObject. Must have constructor with single argument gene!", e);
        }
        this.seed = seed;
        if (populationSize < 1) {
            throw new IllegalArgumentException("Population size must be larger than 0!");
        }
        this.populationSize = populationSize;

        try {
            parentCount = Integer.parseInt(NeuralNetwork.Builder.getProperty("genetic_parent_count"));
        } catch (Exception e) {
            LOG.log(Level.INFO, "Could not load property 'genetic_parent_count' from neuralnetwork.properties!", e);
        }
        try {
            reproductionPoolSize = Double.parseDouble(NeuralNetwork.Builder.getProperty("genetic_reproduction_pool_size"));
        } catch (Exception e) {
            LOG.log(Level.INFO, "Could not load property 'genetic_reproduction_pool_size' from neuralnetwork.properties!", e);
        }
        if (parentCount < 1 || parentCount > populationSize) {
            throw new IllegalArgumentException("parent count must be set to at least 1 and it must not exceed population size!");
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
     * @param geneClass                      the type which implements the gene.
     * @param seed                          the seed to be seeded for the first population.
     * @param populationSize                 the population size for the genetic algorithm.
     * @param generationCount                the maximum number of generations for this batch.
     */
    public GeneticBatch(Class<T> templateGeneticAlgorithmObject, Class<U> geneClass, IGene seed, int populationSize, int generationCount) {
        this(templateGeneticAlgorithmObject, geneClass, seed, populationSize);
        this.generationCount = generationCount;
    }

    /**
     * This method will create a new generation and process it afterwards.
     *
     * @return the best seed for reproduction (i.e. the seed for a new generation).
     */
    public IGene processGeneration() {
        if (currentGenerationId == generationCount) {
            return null;
        }
        currentGeneration = new Generation<>(geneticAlgorithmObjectConstructor, currentGenerationId, parentCount, populationSize, reproductionPoolSize);
        seed = currentGeneration.runGeneration(seed);
        currentGenerationId++;
        return seed;
    }

    /**
     * This method will return the current gene considered best for creating the next generation.
     *
     * @return the best gene for reproduction (i.e. the seed for a new generation).
     */
    public IGene getBestSeed() {
        if (currentGeneration == null) {
            return seed;
        }
        return currentGeneration.getBestReproductiveGene();
    }

    /**
     * This method will return the current gene which performed best in the most recent generation.
     *
     * @return the best gene or null if no generation was processed yet.
     */
    public IGene getBestGene() {
        if (currentGeneration == null) {
            return null;
        }
        return currentGeneration.getBestGene();
    }

    /**
     * This method will return the best genes which performed best in the most recent generation.
     *
     * @param count the count of genes to be returned.
     * @return a List of the best genes or an empty List if no generation was processed yet.
     */
    public List<IGene> getBestGenes(int count) {
        List<IGene> genes = new ArrayList<>();
        if (currentGeneration == null) {
            return genes;
        }
        int index = Math.min(count, populationSize);
        List<IGeneticObject> populationList = currentGeneration.getPopulationList();
        for (int i = 0; i < index; i++) {
            genes.add(populationList.get(i).getGene());
        }
        return genes;
    }

    /**
     * Returns current parent count for the genetic algorithm.
     *
     * @return the parent count.
     */
    public int getParentCount() {
        return parentCount;
    }

    /**
     * Sets parent count for the genetic algorithm. Must be at least 1 and must not exceed population size.
     *
     * @param parentCount the count of genes to be crossed over for reproduction.
     * @return the current genetic algorithm batch.
     */
    public GeneticBatch<T, U> setParentCount(int parentCount) {
        if (parentCount < 1 || parentCount > populationSize) {
            throw new IllegalArgumentException("parent count must be set to at least 1 and it must not exceed population size!");
        }
        this.parentCount = parentCount;
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
    public GeneticBatch<T, U> setReproductionPoolSize(double reproductionPoolSize) {
        if (reproductionPoolSize <= 0 || reproductionPoolSize > 1) {
            throw new IllegalArgumentException("reproduction pool size must be set to at least 0.0 and it must not 1.1!");
        }
        this.reproductionPoolSize = reproductionPoolSize;
        return this;
    }

}
