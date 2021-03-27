package geneticalgorithm;

import neuralnet.NeuralNetwork;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This interface provides a free implementation of a genetic algorithm object.
 * Such instance will handle the collection of data for computer vision, feed it to the NeuralNetwork it holds,
 * read the according output and implement the acton to be performed.
 * To get it working, the implementing class must have a constructor which takes a NeuralNetwork
 * as parameter.
 * As this interface extends Comparable, the compareTo method must be implemented. Please make sure it uses the
 * fitness function and the higher value indicates better performance.
 * Alternatively, the abstract class GeneticAlgorithmObject can be extended, which will provide more implementations,
 * trading off less flexibility as no other classes can be extended.
 */

public interface IGeneticAlgorithmObject extends Comparable<IGeneticAlgorithmObject> {

    /**
     * This method allows a prediction for given input. The input values will be processed in the
     * NeuralNetwork held by this instance.
     * @param inputValues the input array to be processed.
     * @return the output values as Double List.
     */
    List<Double> predict(@NotNull double[] inputValues);

    /**
     * This method contains the logic of one 'step' or 'move' of this instance. It will be triggered
     * sequentially, until the task to be performed is over.
     * @return true if the task is still active.
     */
    boolean perform();

    /**
     * This method will allow to extract the NeuralNetwork for the genetic algorithm.
     * @return the NeuralNetwork held by this instance.
     */
    NeuralNetwork getNeuralNetwork();

    /**
     * This method returns the fitness of this instance. The fitness is a value which will be maximized by the
     * genetic algorithm throughout multiple generations. It will be used to compare this instance with other
     * instances within the same population.
     * @return the fitness value for this instance.
     */
    long getFitness();

    /**
     * This method indicates if this instance is in an immature state yet. In mature statues only, the genetic
     * algorithm will perform a roulette selection for the next generation.
     * @return true if the state of the instance is yet immature.
     */
    boolean isImmature();

    /**
     * This method allows writing log messages. The best performing instance of a generation will be selected
     * after a generation is run for log output, so the progress of a generation can be supervised.
     * @return the log message from the best instance of the current population.
     */
    String getLogMessage();

    /**
     * This method indicates if this instance did reach the best possible state. It is used for specific log
     * messages, so the progress of the genetic algorithm can be supervised more easily.
     * @return true if the best possible state is reached.
     */
    boolean hasReachedGoal();

}
