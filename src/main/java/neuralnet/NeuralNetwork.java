package neuralnet;

import util.Descent;
import util.Rectifier;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * This is the central class of this library, allowing to create a freely configurable neural network.
 * The architecture can be set by parameters. Input and output values are designed to be double arrays.
 * It supports supervised and unsupervised machine learning.
 * The neural network also offers property change support for the predict method.
 */
public class NeuralNetwork implements Serializable {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private static final Properties PROPERTIES = new Properties();
    private static final long serialVersionUID = 2L;

    private int[] configuration;
    private List<Layer> layers = new ArrayList<>();
    private List<List<Double>> cachedNodeValues = new ArrayList<>();

    private Rectifier rectifier;
    private Descent learningRateDescent;
    private double initialLearningRate;
    private double learningRate;
    private double learningRateMomentum;
    private int iterationCount;
    private Descent mutationRateDescent;
    private double initialMutationRate;
    private double mutationRate;
    private double mutationRateMomentum;

    static {
        URL path = NeuralNetwork.class.getClassLoader().getResource("neuralnetwork.properties");
        File file;
        try {
            assert path != null;
            file = Paths.get(path.toURI()).toFile();
            PROPERTIES.load(new FileInputStream(file));
        } catch (URISyntaxException | IOException | FileSystemNotFoundException e) {
            throw new IllegalStateException("Could not access properties file neuralnetwork.properties in local resources folder!", e);
        }
    }

    /**
     * A constructor of the neural network.
     * The varag parameters will define the architecture of the neural network. You need to enter at least two parameters.
     * For hyperparameters and additional default settings, please use the neuralnetwork.properties files or
     * the according setters (builder pattern available).
     * @param layerParams the architecture of the neural network. First argument = node count of input layer; last argument = node count of output layer; arguments between = node count per hidden layer.
     */
    public NeuralNetwork(int... layerParams) {
        if (layerParams.length < 2) {
            throw new IllegalArgumentException("enter at least two arguments to create neural network!");
        } else if (Arrays.stream(layerParams).anyMatch(number -> number < 1)) {
            throw new IllegalArgumentException("every layer must have at least one node!");
        }

        this.configuration = layerParams;
        for (int i = 1; i < layerParams.length; i++) {
            layers.add(new Layer(layerParams[i], layerParams[i-1]));
        }

        try {
            this.rectifier = Rectifier.valueOf(PROPERTIES.getProperty("rectifier").toUpperCase());
            this.learningRateDescent = Descent.valueOf(PROPERTIES.getProperty("learning_rate_descent").toUpperCase());
            this.mutationRateDescent = Descent.valueOf(PROPERTIES.getProperty("mutation_rate_descent").toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Please choose valid enum constant. See properties files for hints.", e);
        }

        try {
            this.initialLearningRate = Double.parseDouble(PROPERTIES.getProperty("learning_rate"));
            this.learningRate = initialLearningRate;
            this.mutationRate = Double.parseDouble(PROPERTIES.getProperty("genetic_mutation_rate"));
            if (learningRate < 0 || learningRate > 1) {
                throw new IllegalArgumentException("Learning rate must be set between 0.0 and 1.0!");
            }
            this.learningRateMomentum = Double.parseDouble(PROPERTIES.getProperty("learning_decay_momentum"));
            if (learningRateMomentum < 0 || learningRateMomentum > 1) {
                throw new IllegalArgumentException("Momentum must be set between 0.0 and 1.0!");
            }
            this.initialMutationRate = Double.parseDouble(PROPERTIES.getProperty("genetic_mutation_rate"));
            this.mutationRate = initialMutationRate;
            if (mutationRate < 0 || mutationRate > 1) {
                throw new IllegalArgumentException("Mutation rate must be set between 0.0 and 1.0!");
            }
            this.mutationRateMomentum = Double.parseDouble(PROPERTIES.getProperty("mutation_decay_momentum"));
            if (mutationRateMomentum < 0 || mutationRateMomentum > 1) {
                throw new IllegalArgumentException("Momentum must be set between 0.0 and 1.0!");
            }
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Please set a double value for this property!");
        }
    }

    private NeuralNetwork(List<Layer> layers) {
        List<Layer> newLayerSet = new ArrayList<>();
        for (Layer layer : layers) {
            newLayerSet.add(layer.copy());
        }
        this.layers = newLayerSet;
    }

    /**
     * This method will take input nodes as parameter and return the predicted output nodes.
     * The neural net will not be modified. This method can be used for testing or the unsupervised machine learning approach.
     * Additionally, this method offers property change support.
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
        Matrix tmp = Matrix.fromArray(rectifier, inputNodes);

        for (Layer layer : layers) {
            cachedNodeValues.add(Matrix.asList(tmp));
            tmp = Matrix.multiply(layer.weight, tmp);
            tmp.addBias(layer.bias);
            tmp.activate();
        }

        cachedNodeValues.add(Matrix.asList(tmp));
        pcs.firePropertyChange("predict", false, true);

        return Matrix.asList(tmp);
    }

    /**
     * This method will take input nodes as well as the expected output nodes as parameter and return the predicted output nodes.
     * The neural net will be modified and back propagate the expected values to adjust the weighed layers. This method can be used for training as supervised machine learning algorithm.
     * @param inputNodes the input nodes as double array
     * @param expectedOutputNodes the expected output nodes as double array
     * @return the actual output nodes as Double List
     */
    public List<Double> fit(double[] inputNodes, double[] expectedOutputNodes) {
        if (inputNodes == null || expectedOutputNodes == null) {
            throw new NullPointerException("inputNodes and expectedOutputNodes are required!");
        } else if (inputNodes.length != configuration[0]) {
            throw new IllegalArgumentException("input node count does not match neural network configuration! received " + inputNodes.length + " instead of " + configuration[0] + " input nodes.");
        }

        Matrix input = Matrix.fromArray(rectifier, inputNodes);

        // forward propagate and prepare output
        List<Matrix> steps = new ArrayList<>();
        Matrix tmp = input;
        for (Layer layer : layers) {
            tmp = Matrix.multiply(layer.weight, tmp);
            tmp.addBias(layer.bias);
            tmp.activate();
            steps.add(tmp);
        }

        Matrix target = Matrix.fromArray(rectifier, expectedOutputNodes);

        // backward propagate to adjust weights in layers
        iterationCount++;
        Matrix error = null;
        for (int i = steps.size()-1; i >= 0; i--) {
            if (error == null) {
                error = Matrix.subtract(target, steps.get(steps.size()-1));
            } else {
                error = Matrix.multiply(Matrix.transpose(layers.get(i+1).weight), error);
            }
            Matrix gradient = steps.get(i).derive();
            gradient.multiplyElementwise(error);
            gradient.multiply(learningRate);
            Matrix delta = Matrix.multiply(gradient, Matrix.transpose((i == 0) ? input : steps.get(i-1)));
            layers.get(i).weight.add(delta);
            layers.get(i).bias.addBias(gradient);
        }
        decreaseRate();
        return Matrix.asList(tmp);
    }

    /**
     * This method can be used to batch train the neural net with the supervised machine learning approach.
     * @param inputSet the input set of possible input node values
     * @param expectedOutputSet the output set of according expected output values
     * @param epochs the count of repetitions of the batch training
     */
    public void fit(double[][] inputSet, double[][] expectedOutputSet, int epochs) {
        if (inputSet == null || expectedOutputSet == null) {
            throw new NullPointerException("inputSet and expectedOutputSet are required!");
        }
        for (int i = 0; i < epochs; i++) {
            int sampleIndex = (int) (Math.random() * inputSet.length);
            fit(inputSet[sampleIndex], expectedOutputSet[sampleIndex]);
        }
    }

    /**
     * This method will merge two neural networks to a new neural network.
     * @param a neural network to be altered and merged
     * @param b neural network to be merged
     * @return the neural network a merged with b
     */
    public static NeuralNetwork merge(NeuralNetwork a, NeuralNetwork b) {
        if (a == null || b == null) {
            throw new NullPointerException("two NeuralNetwork instances required!");
        }
        a = a.copy();
        for (int i = 0; i < a.layers.size(); i++) {
            a.layers.get(i).weight = Matrix.merge(a.layers.get(i).weight, b.layers.get(i).weight);
            a.layers.get(i).bias = Matrix.merge(a.layers.get(i).bias, b.layers.get(i).bias);
        }
        return a;
    }

    /**
     * This method will provide a randomized copy of the current neural network. The output neural network will not be connected to the copied neural network.
     * @return a mutated copy of this instance
     */
    public NeuralNetwork mutate() {
        NeuralNetwork neuralNetwork = this.copy();
        neuralNetwork.randomize(learningRate, mutationRate);
        return neuralNetwork;
    }

    /**
     * This method will provide an identical copy of the current neural network. The output neural network will not be connected to the copied neural network.
     * @return an identical copy of this instance
     */
    public NeuralNetwork copy() {
        NeuralNetwork neuralNetwork = new NeuralNetwork(layers);
        neuralNetwork.configuration = this.configuration;
        neuralNetwork.rectifier = this.rectifier;
        neuralNetwork.learningRateDescent = this.learningRateDescent;
        neuralNetwork.learningRateMomentum = this.learningRateMomentum;
        neuralNetwork.initialLearningRate = this.initialLearningRate;
        neuralNetwork.learningRate = this.learningRate;
        neuralNetwork.iterationCount = this.iterationCount;
        neuralNetwork.mutationRateDescent = this.mutationRateDescent;
        neuralNetwork.mutationRateMomentum = this.mutationRateMomentum;
        neuralNetwork.initialMutationRate = this.initialMutationRate;
        neuralNetwork.mutationRate = this.mutationRate;
        return neuralNetwork;
    }

    private void randomize(double factor, double grade) {
        for (Layer layer : layers) {
            layer.weight.randomize(factor, grade);
            layer.bias.randomize(factor, grade);
        }
    }

    /**
     * Method to set the rectifier for the NeuralNetwork.
     * The rectifier is the activation function for the nodes of the NeuralNetwork.
     * @param rectifier the rectifier to be chosen.
     * @return the NeuralNetwork.
     */
    public NeuralNetwork setRectifier(Rectifier rectifier) {
        this.rectifier = rectifier;
        return this;
    }

    /**
     * Returns current rectifier of this NeuralNetwork. Must not match corresponding property.
     * @return the rectifier.
     */
    public Rectifier getRectifier() {
        return rectifier;
    }

    /**
     * Method to set the learning rate descent.
     * @param learningRateDescent the descent function the learning rate.
     * @return the NeuralNetwork.
     */
    public NeuralNetwork setLearningRateDescent(Descent learningRateDescent) {
        this.learningRateDescent = learningRateDescent;
        return this;
    }

    /**
     * Returns current learning rate descent of this NeuralNetwork. Must not match corresponding property.
     * @return the learning rate descent.
     */
    public Descent getLearningRateDescent() {
        return learningRateDescent;
    }

    /**
     * Method to set the learning rate. The learning rate may be decreased in case of
     * unsupervised learning.
     * @param learningRate the learning rate.
     * @return the NeuralNetwork.
     */
    public NeuralNetwork setLearningRate(double learningRate) {
        if (learningRate < 0 || learningRate > 1) {
            throw new IllegalArgumentException("Learning rate must be set between 0.0 and 1.0!");
        }
        this.initialLearningRate = learningRate;
        this.learningRate = learningRate;
        return this;
    }

    /**
     * Returns current learning rate of this NeuralNetwork. Must not match corresponding property.
     * @return the learning rate.
     */
    public double getLearningRate() {
        return learningRate;
    }

    /**
     * Decreases the current learning and mutation rate according to the chosen Descent function.
     */
    public void decreaseRate() {
        this.learningRate = learningRateDescent.decrease(initialLearningRate, learningRateMomentum, iterationCount);
        this.mutationRate = mutationRateDescent.decrease(initialMutationRate, mutationRateMomentum, iterationCount);
        iterationCount++;
    }

    /**
     * This method will reset the learning rate to its original state.
     */
    public void resetLearningRate() {
        this.learningRate = this.initialLearningRate;
    }

    /**
     * Sets momentum for the decrease of the learning rate.
     * @param learningRateMomentum the momentum to decrease the learning rate.
     * @return the NeuralNetwork.
     */
    public NeuralNetwork setLearningRateMomentum(double learningRateMomentum) {
        this.learningRateMomentum = learningRateMomentum;
        if (learningRateMomentum < 0 || learningRateMomentum > 1) {
            throw new IllegalArgumentException("Momentum must be set between 0.0 and 1.0!");
        }
        return this;
    }

    /**
     * Returns current momentum of this NeuralNetwork. Must not match corresponding property.
     * @return the momentum.
     */
    public double getLearningRateMomentum() {
        return learningRateMomentum;
    }

    /**
     * Method to set the mutation rate descent.
     * Genetic algorithm only.
     * @param mutationRateDescent the descent function the mutation rate.
     * @return the NeuralNetwork.
     */
    public NeuralNetwork setMutationRateDescent(Descent mutationRateDescent) {
        this.mutationRateDescent = mutationRateDescent;
        return this;
    }

    /**
     * Returns current mutation rate descent of this NeuralNetwork. Must not match corresponding property.
     * @return the mutation rate descent.
     */
    public Descent getMutationRateDescent() {
        return mutationRateDescent;
    }

    /**
     * Sets mutation rate in percentage for the count of mutated components of the neural network.
     * @param mutationRate the mutation rate. Must be between 0.0 and 1.0.
     * @return the NeuralNetwork.
     */
    public NeuralNetwork setMutationRate(double mutationRate) {
        this.initialMutationRate = mutationRate;
        this.mutationRate = mutationRate;
        if (mutationRate < 0 || mutationRate > 1) {
            throw new IllegalArgumentException("Mutation rate rate must be set between 0.0 and 1.0!");
        }
        return this;
    }

    /**
     * Returns current mutation rate of this NeuralNetwork. Must not match corresponding property.
     * @return the mutation rate.
     */
    public double getMutationRate() {
        return mutationRate;
    }

    /**
     * This method will reset the mutation rate to its original state.
     */
    public void resetMutationRate() {
        this.mutationRate = this.initialMutationRate;
    }

    /**
     * Sets momentum for the decrease of the mutation rate.
     * @param mutationRateMomentum the momentum to decrease the mutation rate.
     * @return the NeuralNetwork.
     */
    public NeuralNetwork setMutationRateMomentum(double mutationRateMomentum) {
        this.mutationRateMomentum = mutationRateMomentum;
        if (mutationRateMomentum < 0 || mutationRateMomentum > 1) {
            throw new IllegalArgumentException("Momentum must be set between 0.0 and 1.0!");
        }
        return this;
    }

    /**
     * Returns current momentum of this NeuralNetwork. Must not match corresponding property.
     * @return the momentum.
     */
    public double getMutationRateMomentum() {
        return mutationRateMomentum;
    }

    /**
     * Setter to allow altering properties for the NeuralNetwork configuration.
     * The set value will not be validated within this method. Please see neuralnetwork.properties
     * as guideline.
     * @param key the key of the property.
     * @param value the value of the property.
     */
    public static void setProperty(String key, String value) {
        if (!key.equals("learning_rate") && !key.equals("rectifier") && !key.equals("learning_rate_descent") && !key.equals("learning_decay_momentum") && !key.equals("genetic_reproduction_pool_size") && !key.equals("genetic_mutation_rate") && !key.equals("mutation_rate_descent") && !key.equals("mutation_decay_momentum")) {
            throw new IllegalArgumentException("Property with key " + key + "is not valid in this context!");
        } else if (key.equals("learning_rate")) {
            if (Double.parseDouble(value) < 0 || Double.parseDouble(value) > 1) {
                throw new IllegalArgumentException("Learning rate must be set between 0.0 and 1.0!");
            }
        } else if (key.equals("learning_decay_momentum") || key.equals("mutation_decay_momentum")) {
            if (Double.parseDouble(value) < 0 || Double.parseDouble(value) > 1) {
                throw new IllegalArgumentException("Momentum must be set between 0.0 and 1.0!");
            }
        } else if (key.equals("genetic_reproduction_pool_size")) {
            if (Double.parseDouble(value) < 2) {
                throw new IllegalArgumentException("Reproduction pool size must be set above 2!");
            }
        } else if (key.equals("genetic_mutation_rate")) {
            if (Double.parseDouble(value) < 0 || Double.parseDouble(value) > 1) {
                throw new IllegalArgumentException("Mutation rate must be set between 0.0 and 1.0!");
            }
        }
        PROPERTIES.setProperty(key, value);
    }

    /**
     * Getter for the NeuralNetwork properties.
     * @param key the key of the property.
     * @return the value of the according property.
     */
    public static String getProperty(String key) {
        return PROPERTIES.getProperty(key);
    }

    /**
     * Getter of the configuration of the neural network, which was used to create it.
     * @return the node count per layer.
     */
    public int[] getConfiguration() {
        return configuration;
    }

    /**
     * With this method, the actual values of every node during a prediction process can be querieed. The
     * values are available right after a prediction was executed.
     * @return the cached values for every node after a prediction.
     */
    public List<List<Double>> getCachedNodeValues() {
        return cachedNodeValues;
    }

    /**
     * This method allows do extracts the weight matrix of a specific layer.
     * @param layer the index of the layer.
     * @return the weight matrix of the layer.
     */
    public double[][] getWeights(int layer) {
        return layers.get(layer).weight.getData();
    }

    /**
     * This method allows to set a property change listener to the neural network.
     * It will fire when a prediction is made.
     * @param listener the PropertyChangeListener to be added.
     */
    public void addListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener("predict", listener);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("rectifier: ");
        sb.append(rectifier.getDescription());
        sb.append(", ");
        sb.append("learning rate: ");
        sb.append(learningRate);
        sb.append(", ");
        sb.append("learning rate descent: ");
        sb.append(learningRateDescent.getDescription());
        sb.append(", ");
        sb.append("learning rate momentum: ");
        sb.append(learningRateMomentum);
        sb.append(", ");
        sb.append("mutation rate: ");
        sb.append(mutationRate);
        sb.append(", ");
        sb.append("mutation rate descent: ");
        sb.append(mutationRateDescent.getDescription());
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

}
