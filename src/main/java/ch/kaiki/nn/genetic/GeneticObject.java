package ch.kaiki.nn.genetic;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This abstract class can be used as superclass for an easy implementation of a genetic algorithm object.
 * Such instance will handle the collection of data for computer vision, feed it to the gene it holds,
 * read the according output and implement the action to be performed.
 * Alternatively, the interface IGeneticObject can be implemented, which will provide the same functionality
 * and allows extending other classes.
 */
public abstract class GeneticObject implements IGeneticObject {

    private IGene gene;

    /**
     * The constructor requires a gene, which will be used to predict an action to be taken.
     * If you work with the GeneticBatch, it is required that this constructor is used.
     *
     * @param gene the gene as 'brain' for this instance.
     */
    public GeneticObject(IGene gene) {
        this.gene = gene;
    }

    /**
     * This method allows a prediction for given input. The input values will be processed in the
     * gene held by this instance.
     *
     * @param inputValues the input array to be processed.
     * @return the output values as Double List.
     */
    @Override
    public List<Double> predict(@NotNull double[] inputValues) {
        return gene.predict(inputValues);
    }

    /**
     * This method contains the logic of one 'step' or 'move' of this instance. It will be triggered
     * sequentially, until the task to be performed is over.
     *
     * @return true if the task is still active.
     */
    @Override
    public abstract boolean perform();

    /**
     * This method will allow to extract the gene for the genetic algorithm.
     *
     * @return the gene held by this instance.
     */
    @Override
    public IGene getGene() {
        return gene;
    }

    /**
     * This method returns the fitness of this instance. The fitness is a value which will be maximized by the
     * kaiki.ch.kaiki.nn.genetic algorithm throughout multiple generations. It will be used to compare this instance with other
     * instances within the same population.
     *
     * @return the fitness value for this instance.
     */
    @Override
    public abstract double getFitness();

    /**
     * This method indicates if this instance is in an immature state yet. In mature statues only, the kaiki.ch.kaiki.nn.genetic
     * algorithm will perform a roulette selection for the next generation.
     *
     * @return true if the state of the instance is yet immature.
     */
    @Override
    public abstract boolean isImmature();

    /**
     * This method allows writing log messages. The best performing instance of a generation will be selected
     * after a generation is run for log output, so the progress of a generation can be supervised.
     *
     * @return the log message from the best instance of the current population.
     */
    @Override
    public String getLogMessage() {
        return "fitness: \t " + getFitness();
    }

    /**
     * This method indicates if this instance did reach the best possible state. It is used for specific log
     * messages, so the progress of the kaiki.ch.kaiki.nn.genetic algorithm can be supervised more easily.
     *
     * @return true if the best possible state is reached.
     */
    @Override
    public abstract boolean hasReachedGoal();

    /**
     * This methods will compare this instance with other instances from the same generation. With the according output
     * it is possible to decide which instance performed best within the current population.
     *
     * @param o the other instance to be compared with this instance.
     * @return 1 if this instance performed better, -1 if worse or 0 if equal.
     */
    @Override
    public int compareTo(@NotNull IGeneticObject o) {
        double fitness = this.getFitness();
        double otherFitness = o.getFitness();
        if (fitness > otherFitness) {
            return 1;
        } else if (fitness < otherFitness) {
            return -1;
        }
        return 0;
    }

}
