package ch.kaiki.nn.neuralnet;


import ch.kaiki.nn.data.BackPropData;
import ch.kaiki.nn.util.Initializer;
import ch.kaiki.nn.util.Optimizer;
import ch.kaiki.nn.util.Rectifier;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the central class of this library, allowing to create a freely configurable neural network.
 * The architecture can be set by parameters. Input and output values are designed to be double arrays.
 * It supports supervised and unsupervised machine learning.
 * The neural network also offers property change support for the predict method.
 */
public class NeuralNetwork implements Serializable {

    private static final long serialVersionUID = 2L;
    private static final Logger LOG = Logger.getLogger("NeuralNetwork logger");

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private CostFunction costFunction;
    private Regularizer regularizer = Regularizer.NONE;
    private double regularizationLambda = 0;
    private int[] configuration;

    private List<Layer> layers = new ArrayList<>();
    private final List<List<Double>> cachedNodeValues = new ArrayList<>();
    private Initializer initializer;
    private double dropout;
    private BatchMode batchMode;
    private Optimizer learningRateOptimizer;
    private double initialLearningRate;
    private double learningRate;
    private double learningRateMomentum;
    private int iterationCount;
    private Optimizer mutationRateOptimizer;
    private double initialMutationRate;
    private double mutationRate;
    private double mutationRateMomentum;
    private final BackPropData backPropData = new BackPropData();

    private final boolean followChainRule = true;

    private Batch batch = new Batch();

    /**
     * The constructor of the neural network.
     * The varag parameters will define the architecture of the neural network. You need to enter at least two parameters.
     * For hyperparameters and additional default settings, please use the neuralnetwork.properties files or
     * the according setters.
     *
     * @param configuration the architecture of the neural network. First argument = node count of input layer; last argument = node count of output layer; arguments between = node count per hidden layer.
     */
    private NeuralNetwork(int... configuration) {
        this.configuration = configuration;
    }

    private NeuralNetwork(List<Layer> layers) {
        List<Layer> newLayerSet = new ArrayList<>();
        for (Layer layer : layers) {
            newLayerSet.add(layer.copy());
        }
        this.layers = newLayerSet;
    }

    private void initializeLayers(Rectifier rectifier) {
        for (int i = 1; i < configuration.length; i++) {
            int fanIn = configuration[i - 1];
            int fanOut = (i == configuration.length - 1) ? 0 : configuration[i + 1];
            Layer layer = new Layer(rectifier, configuration[i], configuration[i - 1]);
            layer.initialize(initializer, fanIn, fanOut);
            layers.add(layer);
        }
    }

    /**
     * This method will merge multiple neural networks to a new neural network.
     *
     * @param neuralNetworks the neural networks to be merged. Metadata will be copied from the first neural network given.
     * @return the neural network a merged with b
     */
    public static NeuralNetwork merge(List<NeuralNetwork> neuralNetworks) {
        if (neuralNetworks.size() < 2) {
            throw new NullPointerException("At least two NeuralNetwork instances required!");
        }
        NeuralNetwork neuralNetwork = neuralNetworks.get(0).copy();
        for (int i = 0; i < neuralNetwork.layers.size(); i++) {
            Matrix[] weights = new Matrix[neuralNetworks.size()];
            Matrix[] biases = new Matrix[neuralNetworks.size()];
            for (int j = 0; j < neuralNetworks.size(); j++) {
                weights[j] = neuralNetworks.get(j).layers.get(i).weight;
                biases[j] = neuralNetworks.get(j).layers.get(i).bias;
            }
            neuralNetwork.layers.get(i).weight = Matrix.merge(weights);
            neuralNetwork.layers.get(i).bias = Matrix.merge(biases);
        }
        return neuralNetwork;
    }

    /**
     * This method will take input nodes as parameter and return the predicted output nodes.
     * The neural net will not be modified. This method can be used for testing or the unsupervised machine learning approach.
     * Additionally, this method offers property change support.
     *
     * @param inputNodes the input nodes as double array
     * @return the predicted output nodes as Double List
     */
    public List<Double> predict(double[] inputNodes) {
        if (inputNodes == null) {
            throw new NullPointerException("inputNodes must not be null!");
        } else if (inputNodes.length != configuration[0]) {
            throw new IllegalArgumentException("input node count does not match neural network configuration! received " + inputNodes.length + " instead of " + configuration[0] + " input nodes.");
        }

        cachedNodeValues.clear();
        Matrix tmp = Matrix.fromArray(inputNodes);
        for (Layer layer : layers) {
            cachedNodeValues.add(Matrix.asList(tmp));
            tmp = Matrix.multiply(layer.weight, tmp);
            tmp.addBias(layer.bias);
            tmp.activate(layer.rectifier);
        }
        if (dropout > 0) {
            tmp.divide(dropout);
        }
        cachedNodeValues.add(Matrix.asList(tmp));
        pcs.firePropertyChange("predict", false, true);
        return Matrix.asList(tmp);
    }

    /**
     * This method will take input nodes as well as the expected output nodes as parameter and return the predicted output nodes.
     * The neural net will be modified and back propagate the expected values to adjust the weighed layers. This method can be used for training as supervised machine learning algorithm.
     *
     * @param inputNodes          the input nodes as double array
     * @param expectedOutputNodes the expected output nodes as double array
     * @return the actual output nodes as Double List
     */
    public List<Double> fit(double[] inputNodes, double[] expectedOutputNodes) {
        return fit(inputNodes, expectedOutputNodes, true);
    }

    private List<Double> fit(double[] inputNodes, double[] expectedOutputNodes, boolean consumeBatch) {
        if (inputNodes == null || expectedOutputNodes == null) {
            throw new NullPointerException("inputNodes and expectedOutputNodes are required!");
        } else if (inputNodes.length != configuration[0]) {
            throw new IllegalArgumentException("input node count does not match neural network configuration! received " + inputNodes.length + " instead of " + configuration[0] + " input nodes.");
        } else if (expectedOutputNodes.length != configuration[configuration.length-1]) {
            throw new IllegalArgumentException("output node count does not match neural network configuration! received " + expectedOutputNodes.length + " instead of " + configuration[configuration.length-1] + " output nodes.");
        }

        Matrix input = Matrix.fromArray(inputNodes);

        // forward propagate and prepare output
        List<Matrix> cachedNodeValueVector = new ArrayList<>();
        Matrix tmp = input;
        for (int i = 0; i < layers.size(); i ++) {
            Layer layer = layers.get(i);
            boolean dropoutActive = dropout > 0 && i == layers.size() - 1;
            if (dropoutActive) {
                tmp = Matrix.multiply(layer.weight.dropout(dropout), tmp);      // W x a
            } else {
                tmp = Matrix.multiply(layer.weight, tmp);                       // W x a
            }
            tmp.addBias(layer.bias);                                // (W x a) + b = z
            tmp.activate(layer.rectifier);                          // activate(z)
            if (dropoutActive) {
                tmp.multiply(dropout);
            }
            cachedNodeValueVector.add(tmp);
        }

        Matrix target = Matrix.fromArray(expectedOutputNodes);

        // start backpropagating
        int iter = cachedNodeValueVector.size() - 1;
        Matrix lastCachedVector = cachedNodeValueVector.get(cachedNodeValueVector.size() - 1);

        // computation of cost C
        double cost = costFunction.cost(lastCachedVector, target) + regularizer.costSummand(lastCachedVector, regularizationLambda);
        backPropData.add(iterationCount, cost, Matrix.asArray(lastCachedVector), expectedOutputNodes);

        // computation of loss L = dC/da(L) (derivation of cost function)
        Matrix loss = costFunction.gradient(lastCachedVector, target);
        // backward propagate to adjust weights in layers
        Matrix pass = null;

        // apply regularization
        loss = Matrix.apply(loss, regularizer.gradient(loss, regularizationLambda), Double::sum);       // TODO: only for first loss or for all?
        for (int i = iter; i >= 0; i--) {
            if (i != iter) {
                if (!followChainRule) {
                    // computation of loss L = dC/da(L-i)
                    // this implementation does not follow the chain rule, but seems to get better results
                    loss = Matrix.multiply(Matrix.transpose(layers.get(i + 1).weight), loss);
                } else {
                    // correct implementation according to chain rule
                    loss = pass;
                }
            }

            // gradient: da(L)/dz(L) (derivation of activation)
            Matrix gradient = cachedNodeValueVector.get(i).derive(layers.get(i).rectifier);

            // loss * gradient: dC/da(L) * da(L)/dz(L) (loss * derivation of activation)
            gradient.multiply(loss);

            if (followChainRule) {
                // this would be the correct implementation of the chain rule - would be used as loss for the next iteration
                pass = gradient.copy();
                pass = Matrix.multiply(Matrix.transpose(layers.get(i).weight), pass);
            }

            gradient.multiply(learningRate);

            // no change as add gate distributes when derived
            Matrix biasDelta = gradient.copy();
            batch.addBias(biasDelta, iter-i);
            // delta rule: dz(L)/dw(L) (* dC/da(L) * da(L)/dz(L)) (multiply with preceding activated neurons to get weight derivation)
            Matrix weightDelta = Matrix.multiply(gradient, Matrix.transpose((i == 0) ? input : cachedNodeValueVector.get(i - 1)));
            batch.addWeight(weightDelta, iter-i);

            // update weights and bias
            //layers.get(i).weight.subtract(weightDelta);
            //layers.get(i).bias.subtractBias(biasDelta);

            if (consumeBatch) {
                layers.get(i).weight.subtract(batch.getWeight(iter-i));
                layers.get(i).bias.subtractBias(batch.getBias(iter-i));
            }

        }
        if (consumeBatch) {
            batch = new Batch();
        }
        decreaseRate();
        return Matrix.asList(tmp);
    }

    private class Batch {
        int iter = 0;
        List<Matrix> weightDeltas = new ArrayList<>();
        List<Matrix> biasDeltas = new ArrayList<>();

        public void addWeight(Matrix matrix, int index) {
            if (weightDeltas.size() == index) {
                weightDeltas.add(matrix);
            } else {
                Matrix m = weightDeltas.get(index);
                m.add(matrix);
                weightDeltas.set(index, m);
            }
            iter++;
        }

        public void addBias(Matrix matrix, int index) {
            if (biasDeltas.size() == index) {
                biasDeltas.add(matrix);
            } else {
                Matrix m = biasDeltas.get(index);
                m.add(matrix);
                biasDeltas.set(index, m);
            }
            iter++;
        }

        public Matrix getWeight(int index) {
            Matrix result = this.weightDeltas.get(index).copy();
            if (NeuralNetwork.this.batchMode == BatchMode.MEAN) {
                result.divide(iter);
            }
            return result;
        }

        public Matrix getBias(int index) {
            Matrix result = this.biasDeltas.get(index).copy();
            if (NeuralNetwork.this.batchMode == BatchMode.MEAN) {
                result.divide(iter);
            }
            return result;
        }
    }

    /**
     * This method can be used to batch train the neural net with the supervised machine learning approach.
     *
     * @param inputSet          the input set of possible input node values
     * @param expectedOutputSet the output set of according expected output values
     * @param epochs            the count of repetitions of the batch training
     * @param batchSize         the batch size for fitting cycles
     */
    public void fit(double[][] inputSet, double[][] expectedOutputSet, int epochs, int batchSize) {
        if (inputSet == null || expectedOutputSet == null) {
            throw new NullPointerException("inputSet and expectedOutputSet are required!");
        }
        long starTime = System.nanoTime();
        int rest = epochs % batchSize;
        for (int i = 0; i < epochs; i++) {
            int sampleIndex = (int) (Math.random() * inputSet.length);
            if (i == epochs-rest) {
                batchSize = rest;
            }
            fit(inputSet[sampleIndex], expectedOutputSet[sampleIndex], (i+1) % batchSize == 0);
        }
        long endTime = System.nanoTime();
        double fitTime = (endTime - starTime);
        String unit = "nanos";
        if (fitTime >= 1000000) {
            fitTime /= 1000000.;
            unit = "ms";
            if (fitTime >= 1000) {
                fitTime /= 1000.;
                unit = "s";
                if (fitTime >= 60) {
                    fitTime /= 60.;
                    unit = "min";
                }
            }
        }
        LOG.log(Level.INFO, "Fitting time for " + epochs + " epochs: " + fitTime + " " + unit + ".");
    }

    /**
     * This method will provide a randomized copy of the current neural network. The output neural network will not be connected to the copied neural network.
     *
     * @return a mutated copy of this instance
     */
    public NeuralNetwork mutate() {
        NeuralNetwork neuralNetwork = this.copy();
        neuralNetwork.randomize();
        return neuralNetwork;
    }

    /**
     * This method will provide a re-initialized copy of the current neural network. The output neural network will not be connected to the copied neural network.
     *
     * @return a re-initialized copy of this instance
     */
    public NeuralNetwork initialize() {
        NeuralNetwork neuralNetwork = this.copy();
        neuralNetwork.initialilzeLayers();
        return neuralNetwork;
    }

    private void initialilzeLayers() {
        for (int i = 1; i < configuration.length; i++) {
            int fanIn = configuration[i - 1];
            int fanOut = (i == configuration.length - 1) ? 0 : configuration[i + 1];
            layers.get(i-1).initialize(initializer, fanIn, fanOut);
        }
    }

    /**
     * This method will provide an identical copy of the current neural network. The output neural network will not be connected to the copied neural network.
     *
     * @return an identical copy of this instance
     */
    public NeuralNetwork copy() {       // TODO: add copy function which allows to change stuff
        NeuralNetwork neuralNetwork = new NeuralNetwork(layers);
        neuralNetwork.configuration = this.configuration;
        neuralNetwork.initializer = this.initializer;

        neuralNetwork.costFunction = this.costFunction;
        neuralNetwork.regularizer = this.regularizer;
        neuralNetwork.regularizationLambda = this.regularizationLambda;
        neuralNetwork.dropout = this.dropout;
        neuralNetwork.batchMode = this.batchMode;

        neuralNetwork.initialLearningRate = this.initialLearningRate;
        neuralNetwork.learningRate = this.learningRate;
        neuralNetwork.learningRateOptimizer = this.learningRateOptimizer;
        neuralNetwork.learningRateMomentum = this.learningRateMomentum;

        neuralNetwork.initialMutationRate = this.initialMutationRate;
        neuralNetwork.mutationRate = this.mutationRate;
        neuralNetwork.mutationRateOptimizer = this.mutationRateOptimizer;
        neuralNetwork.mutationRateMomentum = this.mutationRateMomentum;

        neuralNetwork.iterationCount = this.iterationCount;
        return neuralNetwork;
    }

    private void randomize() {
        for (Layer layer : layers) {
            layer.weight.randomize(learningRate, mutationRate);
            layer.bias.randomize(learningRate, mutationRate);
        }
    }

    /**
     * Getter of the configuration of the neural network, which was used to create it.
     *
     * @return the node count per layer.
     */
    public int[] getConfiguration() {
        return configuration;
    }

    /**
     * With this method, the actual values of every node during a prediction process can be querieed. The
     * values are available right after a prediction was executed.
     *
     * @return the cached values for every node after a prediction.
     */
    public List<List<Double>> getCachedNodeValues() {
        return cachedNodeValues;
    }

    /**
     * This method allows do extracts the weight matrices from all layers.
     * They are ordered from input to output.
     *
     * @return the weight matrices of all layers.
     */
    public List<double[][]> getWeights() {
        List<double[][]> weights = new ArrayList<>();
        for (Layer layer : layers) {
            weights.add(layer.weight.getData());
        }
        return weights;
    }

    /**
     * This method allows do extracts the bias matrices from all layers.
     * They are ordered from input to output.
     *
     * @return the bias matrices of all layers.
     */
    public List<double[][]> getBiases() {
        List<double[][]> biases = new ArrayList<>();
        for (Layer layer : layers) {
            biases.add(layer.bias.getData());
        }
        return biases;
    }

    public BackPropData getBackPropData() {
        return backPropData;
    }

    /**
     * Getter for the initializer enum.
     *
     * @return the initializer.
     */
    public Initializer getInitializer() {
        return initializer;
    }

    /**
     * Returns current rectifier of this NeuralNetwork. Must not match corresponding property.
     *
     * @return the rectifier.
     */
    public Map<Integer,Rectifier> getRectifiers() {
        Map<Integer, Rectifier> map = new TreeMap<>();
        for (int i = 0; i < layers.size(); i++) {
            map.put(i, layers.get(i).rectifier);
        }
        return map;
    }

    /**
     * Getter for the cost function.
     *
     * @return the cost function.
     */
    public CostFunction getCostFunction() {
        return costFunction;
    }

    /**
     * Getter for the regularizer.
     *
     * @return the regularizer.
     */
    public Regularizer getRegularizer() {
        return regularizer;
    }

    /**
     * Getter for the regularization lambda.
     *
     * @return the regularization parameter.
     */
    public double getRegularizationLambda() {
        return regularizationLambda;
    }

    /**
     * Getter for the dropout factor.
     * @return the dropout factor.
     */
    public double getDropoutFactor() {
        return dropout;
    }

    /**
     * Getter for the batch mode.
     * @return the batch mode.
     */
    public BatchMode getBatchMode() {
        return batchMode;
    }

    /**
     * Returns current learning rate of this NeuralNetwork. Must not match corresponding property.
     *
     * @return the learning rate.
     */
    public double getLearningRate() {
        return learningRate;
    }

    /**
     * Returns current learning rate optimizer of this NeuralNetwork. Must not match corresponding property.
     *
     * @return the learning rate optimizer.
     */
    public Optimizer getLearningRateOptimizer() {
        return learningRateOptimizer;
    }

    /**
     * Returns current momentum of this NeuralNetwork. Must not match corresponding property.
     *
     * @return the momentum.
     */
    public double getLearningRateMomentum() {
        return learningRateMomentum;
    }

    /**
     * Decreases the current learning and mutation rate according to the chosen Optimizer function.
     */
    public void decreaseRate() {
        this.learningRate = learningRateOptimizer.decrease(initialLearningRate, learningRateMomentum, iterationCount);
        this.mutationRate = mutationRateOptimizer.decrease(initialMutationRate, mutationRateMomentum, iterationCount);
        iterationCount++;
    }

    /**
     * This method will reset the learning rate to its original state.
     */
    public void resetLearningRate() {
        this.learningRate = this.initialLearningRate;
    }

    /**
     * Returns current mutation rate of this NeuralNetwork. Must not match corresponding property.
     *
     * @return the mutation rate.
     */
    public double getMutationRate() {
        return mutationRate;
    }

    /**
     * Returns current mutation rate optimizer of this NeuralNetwork. Must not match corresponding property.
     *
     * @return the mutation rate optimizer.
     */
    public Optimizer getMutationRateOptimizer() {
        return mutationRateOptimizer;
    }

    /**
     * Returns current momentum of this NeuralNetwork. Must not match corresponding property.
     *
     * @return the momentum.
     */
    public double getMutationRateMomentum() {
        return mutationRateMomentum;
    }

    /**
     * This method will reset the mutation rate to its original state.
     */
    public void resetMutationRate() {
        this.mutationRate = this.initialMutationRate;
    }

    /**
     * This method allows to set a property change listener to the neural network.
     * It will fire when a prediction is made.
     *
     * @param listener the PropertyChangeListener to be added.
     */
    public void addListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener("predict", listener);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("initializer: ");
        sb.append(initializer);
        sb.append(", ");
        sb.append("rectifier: ");
        Map<Integer, Rectifier> rectifierMap = getRectifiers();
        for (Integer key : rectifierMap.keySet()) {
            sb.append(rectifierMap.get(key).getDescription());
            sb.append(", ");
        }
        sb.append("cost function: ");
        sb.append(costFunction.getDescription());
        sb.append(", ");
        sb.append("regularizer: ");
        sb.append(regularizer.getDescription());
        sb.append(", ");
        sb.append("regularization lambda: ");
        sb.append(regularizationLambda);
        sb.append(", ");
        sb.append("dropout factor: ");
        sb.append(dropout);
        sb.append(", ");
        sb.append("batch mode: ");
        sb.append(batchMode);
        sb.append(", ");
        sb.append("learning rate: ");
        sb.append(learningRate);
        sb.append(", ");
        sb.append("learning rate optimizer: ");
        sb.append(learningRateOptimizer.getDescription());
        sb.append(", ");
        sb.append("learning rate momentum: ");
        sb.append(learningRateMomentum);
        sb.append(", ");
        sb.append("mutation rate: ");
        sb.append(mutationRate);
        sb.append(", ");
        sb.append("mutation rate optimizer: ");
        sb.append(mutationRateOptimizer.getDescription());
        sb.append(", ");
        sb.append("mutation rate momentum: ");
        sb.append(mutationRateMomentum);
        sb.append("\n");
        for (int i = 0; i < layers.size(); i++) {
            sb.append(" ----- layer ");
            sb.append(i);
            sb.append(" -----\n");
            sb.append(layers.get(i).toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Integer.parseInt(learningRate + "" + iterationCount + layers.size());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NeuralNetwork)) {
            return false;
        }
        return this.toString().equals(o.toString());
    }

    public static class Builder {

        private static final Logger LOG = Logger.getLogger("NeuralNetwork.Builder logger");
        private static final Properties PROPERTIES = new Properties();
        static {
            try (InputStream in = NeuralNetwork.class.getClassLoader().getResourceAsStream("neuralnetwork.properties")) {
                File file = File.createTempFile("neural_network_properties", ".txt");
                Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                PROPERTIES.load(new FileInputStream(file));
                file.delete();
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Could not access properties file neuralnetwork.properties in local resources folder!", e);
            }
        }

        int[] layerParams;

        private Initializer initializer = Initializer.RANDOM;
        private Rectifier rectifier = Rectifier.SIGMOID;
        private final Map<Integer, Rectifier> rectifierMap = new TreeMap<>();

        private CostFunction costFunction = CostFunction.MSE;
        private Regularizer regularizer = Regularizer.NONE;
        private double regularizationLambda = 0;
        private double dropout = 0;
        private BatchMode batchMode = BatchMode.MEAN;

        private double learningRate = 0.8;
        private Optimizer learningRateOptimizer = Optimizer.NONE;
        private double learningRateMomentum = 0.01;

        private double mutationRate = 0.5;
        private Optimizer mutationRateOptimizer = Optimizer.NONE;
        private double mutationRateMomentum = 0.01;

        /**
         * This is the constructor for the neural network builder.
         * It will allow to set different hyper parameters to a new neural network instance.
         * This builder contains default values for any parameter. It is possible to overwrite these
         * parameters by using the neuralnetwork.properties.file in the local resources.
         * @param layerParams the nodes per layer of the neural network.
         */
        public Builder(int... layerParams) {
            if (layerParams.length < 2) {
                throw new IllegalArgumentException("enter at least two arguments to create neural network!");
            } else if (Arrays.stream(layerParams).anyMatch(number -> number < 1)) {
                throw new IllegalArgumentException("every layer must have at least one node!");
            }
            this.layerParams = layerParams;
            loadProperties();
        }

        /**
         * The build method will assemble a new NeuralNetwork according to the given parameters.
         * @return the new NeuralNetwork instance.
         */
        public NeuralNetwork build() {
            NeuralNetwork neuralNetwork = new NeuralNetwork(layerParams);
            if (this.initializer == null) {
                throw new NullPointerException("Initializer must not be null!");
            }
            neuralNetwork.initializer = this.initializer;
            if (this.costFunction == null) {
                throw new NullPointerException("Cost function must not be null!");
            }
            neuralNetwork.costFunction = this.costFunction;
            if (this.regularizer == null) {
                throw new NullPointerException("Regularizer must not be null!");
            }
            neuralNetwork.regularizer = this.regularizer;
            if (this.regularizationLambda < 0.0 || this.regularizationLambda > 1.0) {
                throw new IllegalArgumentException("Regularization lambda must not be within a range of 0.0 and 1.0!");
            }
            neuralNetwork.regularizationLambda = this.regularizationLambda;
            if (this.dropout < 0.0 || this.dropout > 1.0) {
                throw new IllegalArgumentException("Dropout factor must not be within a range of 0.0 and 1.0!");
            }
            neuralNetwork.dropout = this.dropout;
            if (this.batchMode == null) {
                throw new NullPointerException("Batch mode must not be null!");
            }
            neuralNetwork.batchMode = this.batchMode;
            if (this.learningRate < 0.0 || this.learningRate > 1.0) {
                throw new IllegalArgumentException("Learning rate must not be within a range of 0.0 and 1.0!");
            }
            neuralNetwork.initialLearningRate = this.learningRate;
            neuralNetwork.learningRate = this.learningRate;
            if (this.learningRateOptimizer == null) {
                throw new NullPointerException("Learning rate optimizer must not be null!");
            }
            neuralNetwork.learningRateOptimizer = this.learningRateOptimizer;
            if (this.learningRateMomentum < 0.0 || this.learningRateMomentum > 1.0) {
                throw new IllegalArgumentException("Learning rate momentum must not be within a range of 0.0 and 1.0!");
            }
            neuralNetwork.learningRateMomentum = this.learningRateMomentum;
            if (this.mutationRate < 0.0 || this.mutationRate > 1.0) {
                throw new IllegalArgumentException("Mutation rate must not be within a range of 0.0 and 1.0!");
            }
            neuralNetwork.initialMutationRate = this.mutationRate;
            neuralNetwork.mutationRate = this.mutationRate;
            if (this.mutationRateOptimizer == null) {
                throw new NullPointerException("Mutation rate optimizer must not be null!");
            }
            neuralNetwork.mutationRateOptimizer = this.mutationRateOptimizer;
            if (this.mutationRateMomentum < 0.0 || this.mutationRateMomentum > 1.0) {
                throw new IllegalArgumentException("Mutation rate momentum must not be within a range of 0.0 and 1.0!");
            }
            neuralNetwork.mutationRateMomentum = this.mutationRateMomentum;

            neuralNetwork.initializeLayers(rectifier);

            for (Integer index : rectifierMap.keySet()) {
                if (index > 0 && index < layerParams.length-1) {
                    Rectifier rectifier = rectifierMap.get(index);
                    if (rectifier != null) {
                        neuralNetwork.layers.get(index).rectifier = rectifierMap.get(index);
                    } else {
                        throw new NullPointerException("Rectifier must not be null!");
                    }
                } else {
                    throw new IllegalArgumentException("Index " + index + " to set rectifier " + rectifierMap.get(index) + " is not in range!");
                }
            }

            return neuralNetwork;
        }

        /**
         * Setter for initializer.
         *
         * @param initializer the initializer to set.
         */
        public Builder setInitializer(Initializer initializer) {
            this.initializer = initializer;
            return this;
        }

        /**
         * Method to set the rectifier for the NeuralNetwork.
         * The rectifier is the activation function for the nodes of the NeuralNetwork.
         *
         * @param rectifier the rectifier to be chosen.
         * @return the Builder.
         */
        public Builder setDefaultRectifier(Rectifier rectifier) {
            this.rectifier = rectifier;
            return this;
        }

        /**
         * Method allowing to set a specific rectifier for a specific layer.
         * @param rectifier the rectifier to set.
         * @param layerIndex the index of the chosen layer.
         * @return the Builder.
         */
        public Builder setRectifierToLayer(Rectifier rectifier, int layerIndex) {
            rectifierMap.put(layerIndex, rectifier);
            return this;
        }

        /**
         * Method allowing to set a specific rectifier for the last layer.
         * @param rectifier the rectifier to set.
         * @return the Builder.
         */
        public Builder setLastLayerRectifier(Rectifier rectifier) {
            rectifierMap.put(layerParams.length-2, rectifier);
            return this;
        }

        /**
         * Setter for the cost function.
         *
         * @param costFunction the cost function to set.
         * @return the Builder.
         */
        public Builder setCostFunction(CostFunction costFunction) {
            this.costFunction = costFunction;
            return this;
        }

        /**
         * Setter for the regularizer.
         *
         * @param regularizer the regularizer to set.
         * @return the Builder.
         */
        public Builder setRegularizer(Regularizer regularizer) {
            this.regularizer = regularizer;
            return this;
        }

        /**
         * Setter for the regularization lambda.
         *
         * @param regularizationLambda the regularization parameter to set.
         * @return the Builder.
         */
        public Builder setRegularizationLambda(double regularizationLambda) {
            this.regularizationLambda = regularizationLambda;
            return this;
        }

        /**
         * Setter for the dropout factor.
         * @param dropout the dropout factor.
         * @return the Builder.
         */
        public Builder setDropoutFactor(double dropout) {
            this.dropout = dropout;
            return this;
        }

        /**
         * Setter for the batch mode.
         * @param batchMode the batch mode.
         * @return the Builder.
         */
        public Builder setBatchMode(BatchMode batchMode) {
            this.batchMode = batchMode;
            return this;
        }

        /**
         * Method to set the learning rate. The learning rate may be decreased in case of
         * unsupervised learning.
         *
         * @param learningRate the learning rate. Must be between 0.0 and 1.0.
         * @return the Builder.
         */
        public Builder setLearningRate(double learningRate) {
            this.learningRate = learningRate;
            return this;
        }

        /**
         * Method to set the learning rate optimizer.
         *
         * @param learningRateOptimizer the optimizer function the learning rate.
         * @return the Builder.
         */
        public Builder setLearningRateOptimizer(Optimizer learningRateOptimizer) {
            this.learningRateOptimizer = learningRateOptimizer;
            return this;
        }

        /**
         * Sets momentum for the decrease of the learning rate.
         *
         * @param learningRateMomentum the momentum to decrease the learning rate. Must be between 0.0 and 1.0.
         * @return the Builder.
         */
        public Builder setLearningRateMomentum(double learningRateMomentum) {
            this.learningRateMomentum = learningRateMomentum;
            return this;
        }

        /**
         * Sets mutation rate in percentage for the count of mutated components of the neural network.
         *
         * @param mutationRate the mutation rate. Must be between 0.0 and 1.0.
         * @return the Builder.
         */
        public Builder setMutationRate(double mutationRate) {
            this.mutationRate = mutationRate;
            return this;
        }

        /**
         * Method to set the mutation rate optimizer.
         * Genetic algorithm only.
         *
         * @param mutationRateOptimizer the optimizer function the mutation rate. Must be between 0.0 and 1.0.
         * @return the Builder.
         */
        public Builder setMutationRateOptimizer(Optimizer mutationRateOptimizer) {
            this.mutationRateOptimizer = mutationRateOptimizer;
            return this;
        }

        /**
         * Sets momentum for the decrease of the mutation rate.
         *
         * @param mutationRateMomentum the momentum to decrease the mutation rate. Must be between 0.0 and 1.0.
         * @return the Builder.
         */
        public Builder setMutationRateMomentum(double mutationRateMomentum) {
            this.mutationRateMomentum = mutationRateMomentum;
            return this;
        }

        private void loadProperties() {

            try {
                this.initializer = Initializer.valueOf(PROPERTIES.getProperty("initializer").toUpperCase());
            } catch (Exception e) {
                logMissingProperty("initializer", e);
            }

            try {
                this.rectifier = Rectifier.valueOf(PROPERTIES.getProperty("rectifier").toUpperCase());
            } catch (Exception e) {
                logMissingProperty("rectifier", e);
            }

            try {
                this.costFunction = CostFunction.valueOf(PROPERTIES.getProperty("cost_function").toUpperCase());
            } catch (Exception e) {
                logMissingProperty("cost_function", e);
            }

            try {
                this.regularizer = Regularizer.valueOf(PROPERTIES.getProperty("regularizer").toUpperCase());
            } catch (Exception e) {
                logMissingProperty("regularizer", e);
            }

            try {
                this.regularizationLambda = Double.parseDouble(PROPERTIES.getProperty("regularizer_param"));
            } catch (Exception e) {
                logMissingProperty("regularizer_param", e);
            }

            try {
                this.dropout = Double.parseDouble(PROPERTIES.getProperty("dropout_factor"));
            } catch (Exception e) {
                logMissingProperty("dropout_factor", e);
            }

            try {
                this.batchMode = BatchMode.valueOf(PROPERTIES.getProperty("batch_mode").toUpperCase());
            } catch (Exception e) {
                logMissingProperty("batch_mode", e);
            }

            try {
                this.learningRate = Double.parseDouble(PROPERTIES.getProperty("learning_rate"));
            } catch (Exception e) {
                logMissingProperty("learning_rate", e);
            }

            try {
                this.learningRateOptimizer = Optimizer.valueOf(PROPERTIES.getProperty("learning_rate_optimizer").toUpperCase());
            } catch (Exception e) {
                logMissingProperty("learning_rate_optimizer", e);
            }

            try {
                this.learningRateMomentum = Double.parseDouble(PROPERTIES.getProperty("learning_rate_momentum"));
            } catch (Exception e) {
                logMissingProperty("learning_rate_momentum", e);
            }

            try {
                this.mutationRate = Double.parseDouble(PROPERTIES.getProperty("mutation_rate"));
            } catch (Exception e) {
                logMissingProperty("mutation_rate", e);
            }

            try {
                this.mutationRateOptimizer = Optimizer.valueOf(PROPERTIES.getProperty("mutation_rate_optimizer").toUpperCase());
            } catch (Exception e) {
                logMissingProperty("mutation_rate_optimizer", e);
            }

            try {
                this.mutationRateMomentum = Double.parseDouble(PROPERTIES.getProperty("mutation_rate_momentum"));
            } catch (Exception e) {
                logMissingProperty("mutation_rate_momentum", e);
            }

        }

        private void logMissingProperty(String propertyName, Throwable e) {
            LOG.log(Level.INFO, "Could not load property '" + propertyName + "' from neuralnetwork.properties!", e);

        }

        /**
         * Setter to allow altering properties for the NeuralNetwork configuration.
         * Key and value will not be validated within this method. Please see neuralnetwork.properties
         * as guideline.
         *
         * @param key   the key of the property.
         * @param value the value of the property.
         */
        public static void setProperty(String key, String value) {
            PROPERTIES.setProperty(key, value);
        }

        /**
         * Returns Properties object.
         * @return the properties.
         */
        public static Properties getProperties() {
            return PROPERTIES;
        }

        /**
         * Getter for the NeuralNetwork properties.
         *
         * @param key the key of the property.
         * @return the value of the according property.
         */
        public static String getProperty(String key) {
            return PROPERTIES.getProperty(key);
        }
    }


}
