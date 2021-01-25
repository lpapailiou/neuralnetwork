package geneticalgorithm;

import neuralnet.NeuralNetwork;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

class GeneticAlgorithmGeneration {

    private  static final Logger LOG = Logger.getLogger("generation logger");
    private static final int THREAD_POOL = 16;
    private int id;
    private int populationSize;
    private NeuralNetwork bestNeuralNetwork;
    private NeuralNetwork bestNeuralNetworkForReproduction;
    private List<GeneticAlgorithmObject> populationList = new ArrayList<>();
    private int selectionReproductionSize = 2;
    private Constructor<?> templateBuilder;

    GeneticAlgorithmGeneration(int id, int populationSize) {
        this.id = id;
        this.populationSize = populationSize;
        selectionReproductionSize = Integer.parseInt(GeneticAlgorithmBatch.properties.getProperty("selectionReproductionSize"));
        try {
            templateBuilder = Class.forName(GeneticAlgorithmBatch.properties.getProperty("geneticAlgorithmObjectTemplate")).getConstructor(NeuralNetwork.class);
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            throw new UnsupportedOperationException("geneticAlgorithmObjectTemplate property is not set correctly!", e);
        }
    }

    NeuralNetwork runGeneration(NeuralNetwork seedNeuralNetwork) {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL);
        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            tasks.add(new BackgroundProcess(i == 0 ? seedNeuralNetwork : seedNeuralNetwork.clone(), populationList));
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
        NeuralNetwork bestForReproduction = bestNeuralNetwork;

        LOG.log(Level.INFO, () -> String.format("generation #%d: \t %s", id, populationList.get(0).getLogMessage()));

        if (populationList.get(0).isPerfectScore()) {
            long calc = populationList.stream().filter(o -> o.isPerfectScore()).count();
            double scorePercent = 100.0 / populationSize * (double) calc ;
            LOG.log(Level.INFO, () -> String.format("****************** PERFECT SCORE ACHIEVED! ****************** \nat generation #%d, %.2f%s units (%d of %d) reached a perfect score.", id, scorePercent, "%", calc, populationSize));
        }

        if (populationList.size() < 2) {
            return bestNeuralNetwork;
        } else if (populationList.size() < 20 || populationList.get(0).isImmature()) {
            return NeuralNetwork.merge(bestNeuralNetwork, populationList.get(1).getNeuralNetwork());
        }

        int selectionPoolSize = 8;
        if (populationList.size() >= 100 && populationList.size() < 200) {
            selectionPoolSize = (int) (populationSize * 0.2);
        } else if (populationList.size() >= 200 && populationList.size() < 1000) {
            selectionPoolSize = (int) (populationSize * 0.1);
        } else if (populationList.size() >= 1000) {
            selectionPoolSize = (int) (populationSize * 0.01);
        }

        Map<Integer, Long> map = new HashMap<>();
        double sumFitness = 0;
        for (int i = 0; i < selectionPoolSize; i++) {
            GeneticAlgorithmObject object = populationList.get(i);
            long fitness = object.getFitness();
            sumFitness += fitness;
            map.put(i, fitness);
        }

        List<NeuralNetwork> mergeList = new ArrayList<>();

        for (int i = 0; i < selectionReproductionSize; i++) {
            mergeList.add(0, spinRouletteWheel(map, selectionPoolSize, sumFitness));
        }

        bestForReproduction = mergeList.get(0);
        for (int i = 1; i < mergeList.size(); i++) {
            bestForReproduction = NeuralNetwork.merge(bestForReproduction, mergeList.get(i));
        }

        return NeuralNetwork.merge(bestNeuralNetwork, bestForReproduction);
    }

    private NeuralNetwork spinRouletteWheel(Map<Integer, Long> map, int selectionPoolSize, double sumFitness) {
        long checksum = 0;
        NeuralNetwork chosen = null;
        for (int i = 0; i < selectionPoolSize; i++) {
            checksum += map.get(i);
            if (checksum > new Random().nextInt((int) sumFitness)) {
                chosen = populationList.get(i).getNeuralNetwork();
                break;
            }
        }
        return chosen;
    }

    List<GeneticAlgorithmObject> getPopulationList() {
        return populationList;
    }

    class BackgroundProcess implements Runnable {
        NeuralNetwork neuralNetwork;
        GeneticAlgorithmObject object;

        BackgroundProcess(NeuralNetwork neuralNetwork, List<GeneticAlgorithmObject> populationList) {
            this.neuralNetwork = neuralNetwork;
            try {
                object = (GeneticAlgorithmObject) templateBuilder.newInstance(neuralNetwork);
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                throw new UnsupportedOperationException("geneticAlgorithmObjectTemplate property is not a suitable class!", e);
            }
            populationList.add(object);
        }

        @Override
        public void run() {
            boolean running = true;
            while(running) {
                running = object.executeStep();
            }
        }
    }

}
