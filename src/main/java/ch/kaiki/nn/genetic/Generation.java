package ch.kaiki.nn.genetic;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

class Generation<T> {

    private static final Logger LOG = Logger.getLogger("Generation logger");
    private static final int THREAD_POOL = 16;
    private final Constructor<T> geneticObjectConstructor;
    private final int id;
    private final int populationSize;
    private IGene bestGene;
    private IGene bestReproductiveGene;
    private final List<IGeneticObject> populationList = new ArrayList<>();
    private final int parentCount;
    private int selectionPoolSize;

    Generation(Constructor<T> geneticObjectConstructor, int id, int parentCount, int populationSize, double reproductionPoolSize) {
        this.geneticObjectConstructor = geneticObjectConstructor;
        this.id = id;
        this.parentCount = parentCount;
        this.populationSize = populationSize;
        this.selectionPoolSize = (int) (reproductionPoolSize * (double) populationSize);
        this.selectionPoolSize = Math.max(this.selectionPoolSize, 1);
    }

    IGene runGeneration(IGene seed) {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL);
        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            tasks.add(new BackgroundProcess(geneticObjectConstructor, i == 0 ? seed.initialize() : seed.mutate(), populationList));
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
        bestReproductiveGene = evolve();
        bestReproductiveGene.decreaseRate();
        return bestReproductiveGene;
    }

    IGene getBestGene() {
        return bestGene;
    }

    IGene getBestReproductiveGene() {
        return bestReproductiveGene;
    }

    private synchronized IGene evolve() {
        populationList.sort(Comparator.nullsLast(Collections.reverseOrder()));
        //Collections.sort(populationList, Collections.reverseOrder());

        bestGene = populationList.get(0).getGene();
        IGene bestForReproduction;

        LOG.log(Level.INFO, () -> String.format("generation #%d: \t %s", id, populationList.get(0).getLogMessage()));

        if (populationList.get(0).hasReachedGoal()) {
            long calc = populationList.stream().filter(IGeneticObject::hasReachedGoal).count();
            double scorePercent = 100.0 / populationSize * (double) calc;
            LOG.log(Level.INFO, () -> String.format("****************** PERFECT SCORE ACHIEVED! ****************** \nat generation #%d, %.2f%s units (%d of %d) reached a perfect score.", id, scorePercent, "%", calc, populationSize));
        }

        if (populationList.size() < 2) {
            return bestGene;
        }

        Map<Integer, Long> map = new HashMap<>();
        double sumFitness = 0;
        for (int i = 0; i < selectionPoolSize; i++) {
            IGeneticObject object = populationList.get(i);
            long fitness = object.getFitness();
            sumFitness += fitness;
            map.put(i, fitness);
        }

        List<IGene> mergeList = new ArrayList<>();
        mergeList.add(bestGene);
        for (int i = 0; i < parentCount - 1; i++) {
            mergeList.add(spinRouletteWheel(map, selectionPoolSize, sumFitness));
        }

        bestForReproduction = bestGene.crossover(mergeList);

        return bestForReproduction;
    }

    private IGene spinRouletteWheel(Map<Integer, Long> map, int selectionPoolSize, double sumFitness) {
        double checksum = 0;
        double random = Math.random() * sumFitness;
        for (int i = 0; i < selectionPoolSize; i++) {
            checksum += map.get(i);
            if (checksum >= random) {
                return populationList.get(i).getGene();
            }
        }
        return null;
    }

    List<IGeneticObject> getPopulationList() {
        return populationList;
    }

    class BackgroundProcess implements Runnable {
        IGeneticObject object;

        BackgroundProcess(Constructor<T> geneticAlgorithmObjectConstructor, IGene gene, List<IGeneticObject> populationList) {

            try {
                object = (IGeneticObject) geneticAlgorithmObjectConstructor.newInstance(gene);
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                throw new UnsupportedOperationException("geneticAlgorithmObjectConstructor does not allow creation of an instance implementing IGeneticObject!", e);
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
