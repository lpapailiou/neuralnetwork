package ch.kaiki.nn.genetic;

import ch.kaiki.nn.neuralnet.NeuralNetwork;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

class GeneticAlgorithmGeneration<T> {

    private static final Logger LOG = Logger.getLogger("GeneticAlgorithmGeneration logger");
    private static final int THREAD_POOL = 16;
    private final Constructor<T> geneticAlgorithmObjectConstructor;
    private final int id;
    private final int populationSize;
    private NeuralNetwork bestNeuralNetwork;
    private NeuralNetwork bestNeuralNetworkForReproduction;
    private final List<IGeneticAlgorithmObject> populationList = new ArrayList<>();
    private final int reproductionSpecimenCount;
    private int selectionPoolSize;

    GeneticAlgorithmGeneration(Constructor<T> geneticAlgorithmObjectConstructor, int id, int reproductionSpecimenCount, int populationSize, double reproductionPoolSize) {
        this.geneticAlgorithmObjectConstructor = geneticAlgorithmObjectConstructor;
        this.id = id;
        this.reproductionSpecimenCount = reproductionSpecimenCount;
        this.populationSize = populationSize;
        this.selectionPoolSize = (int) reproductionPoolSize * populationSize;
        this.selectionPoolSize = Math.max(this.selectionPoolSize, 1);
    }

    NeuralNetwork runGeneration(NeuralNetwork seedNeuralNetwork) {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL);
        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            tasks.add(new BackgroundProcess(geneticAlgorithmObjectConstructor, i == 0 ? seedNeuralNetwork.initialize() : seedNeuralNetwork.mutate(), populationList));
        }
        CompletableFuture<?>[] futures = tasks.stream().map(task -> CompletableFuture.runAsync(task, executorService)).toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(futures).join();
        executorService.shutdown();

        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.log(Level.WARNING, "executor service interrupted unexpectedly!", e);
            Thread.currentThread().interrupt();
        }

        bestNeuralNetworkForReproduction = evolve();
        bestNeuralNetworkForReproduction.decreaseRate();
        return bestNeuralNetworkForReproduction;
    }

    NeuralNetwork getBestNeuralNetwork() {
        return bestNeuralNetwork;
    }

    NeuralNetwork getBestNeuralNetworkForReproduction() {
        return bestNeuralNetworkForReproduction;
    }

    private NeuralNetwork evolve() {
        populationList.sort(Comparator.nullsLast(Collections.reverseOrder()));
        bestNeuralNetwork = populationList.get(0).getNeuralNetwork();
        NeuralNetwork bestForReproduction;

        LOG.log(Level.INFO, () -> String.format("generation #%d: \t %s", id, populationList.get(0).getLogMessage()));

        if (populationList.get(0).hasReachedGoal()) {
            long calc = populationList.stream().filter(IGeneticAlgorithmObject::hasReachedGoal).count();
            double scorePercent = 100.0 / populationSize * (double) calc;
            LOG.log(Level.INFO, () -> String.format("****************** PERFECT SCORE ACHIEVED! ****************** \nat generation #%d, %.2f%s units (%d of %d) reached a perfect score.", id, scorePercent, "%", calc, populationSize));
        }

        if (populationList.size() < 2) {
            return bestNeuralNetwork;
        }

        Map<Integer, Double> map = new HashMap<>();
        double sumFitness = 0;
        for (int i = 0; i < selectionPoolSize; i++) {
            IGeneticAlgorithmObject object = populationList.get(i);
            double fitness = object.getFitness();
            sumFitness += fitness;
            map.put(i, fitness);
        }

        List<NeuralNetwork> mergeList = new ArrayList<>();
        mergeList.add(bestNeuralNetwork);
        for (int i = 0; i < reproductionSpecimenCount - 1; i++) {
            mergeList.add(spinRouletteWheel(map, selectionPoolSize, sumFitness));
        }

        bestForReproduction = NeuralNetwork.merge(mergeList);

        return bestForReproduction;
    }

    private NeuralNetwork spinRouletteWheel(Map<Integer, Double> map, int selectionPoolSize, double sumFitness) {
        double checksum = 0;
        double random = new Random().nextDouble() * sumFitness;
        for (int i = 0; i < selectionPoolSize; i++) {
            checksum += map.get(i);
            if (checksum >= random) {
                return populationList.get(i).getNeuralNetwork();
            }
        }
        return null;
    }

    List<IGeneticAlgorithmObject> getPopulationList() {
        return populationList;
    }

    class BackgroundProcess implements Runnable {
        NeuralNetwork neuralNetwork;
        IGeneticAlgorithmObject object;

        BackgroundProcess(Constructor<T> geneticAlgorithmObjectConstructor, NeuralNetwork neuralNetwork, List<IGeneticAlgorithmObject> populationList) {
            this.neuralNetwork = neuralNetwork;
            try {
                object = (IGeneticAlgorithmObject) geneticAlgorithmObjectConstructor.newInstance(neuralNetwork);
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                throw new UnsupportedOperationException("geneticAlgorithmObjectConstructor does not allow creation of an instance implementing IGeneticAlgorithmObject!", e);
            }
            populationList.add(object);
        }

        @Override
        public void run() {
            while (object.perform()) {
                // run while genetic algorithm object has not reached goal
            }
        }
    }

}
