package neuralnet;

import util.LearningRateDescent;
import util.Rectifier;

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
 */
public class NeuralNetwork implements Serializable {

    private static final Properties PROPERTIES = new Properties();
    private static final long serialVersionUID = 2L;
    private List<Layer> layers = new ArrayList<>();
    private int inputLayerNodes;

    private Rectifier rectifier;
    private LearningRateDescent learningRateDescent;
    private double initialLearningRate;
    private double learningRate;
    private double momentum;
    private int iteration_count;

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

        this.inputLayerNodes = layerParams[0];
        for (int i = 1; i < layerParams.length; i++) {
            layers.add(new Layer(layerParams[i], layerParams[i-1]));
        }

        try {
            this.rectifier = Rectifier.valueOf(PROPERTIES.getProperty("rectifier").toUpperCase());
            this.learningRateDescent = LearningRateDescent.valueOf(PROPERTIES.getProperty("learning_rate_descent").toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Please choose valid enum constant. See properties files for hints.", e);
        }

        try {
            this.initialLearningRate = Double.parseDouble(PROPERTIES.getProperty("learning_rate"));
            this.learningRate = initialLearningRate;
            if (learningRate < 0 || learningRate > 1) {
                throw new IllegalArgumentException("Learning rate must be set between 0.0 and 1.0!");
            }
            this.momentum = Double.parseDouble(PROPERTIES.getProperty("learning_decay_momentum"));
            if (momentum < 0 || momentum > 1) {
                throw new IllegalArgumentException("Momentum must be set between 0.0 and 1.0!");
            }
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Please set a double value for this property!");
        }
    }

    private NeuralNetwork(int inputLayerNodes, List<Layer> layers) {
        List<Layer> newLayerSet = new ArrayList<>();
        for (Layer layer : layers) {
            newLayerSet.add(layer.copy());
        }
        this.layers = newLayerSet;
        this.inputLayerNodes = inputLayerNodes;
    }

    /**
     * This method will take input nodes as parameter and return the predicted output nodes.
     * The neural net will not be modified. This method can be used for testing or the unsupervised machine learning approach.
     * @param inputNodes the input nodes as double array
     * @return the predicted output nodes as Double List
     */
    public List<Double> predict(double[] inputNodes) {
        if (inputNodes == null) {
            throw new NullPointerException("inputNodes must not be null!");
        } else if (inputNodes.length != inputLayerNodes) {
            throw new IllegalArgumentException("input node count does not match neural network configuration! received " + inputNodes.length + " instead of " + inputLayerNodes + " input nodes.");
        }

        Matrix tmp = Matrix.fromArray(rectifier, inputNodes);

        for (Layer layer : layers) {
            tmp = Matrix.multiply(layer.weight, tmp);
            tmp.addBias(layer.bias);
            tmp.activate();
        }

        return Matrix.asList(tmp);
    }

    /**
     * This method will take input nodes as well as the expected output nodes as parameter and return the predicted output nodes.
     * The neural net will be modified and back propagate the expected values to adjust the weighed layers. This method can be used for training as supervised machine learning algorithm.
     * @param inputNodes the input nodes as double array
     * @param expectedOutputNodes the expected output nodes as double array
     * @return the actual output nodes as Double List
     */
    public List<Double> learn(double[] inputNodes, double[] expectedOutputNodes) {
        if (inputNodes == null || expectedOutputNodes == null) {
            throw new NullPointerException("inputNodes and expectedOutputNodes are required!");
        } else if (inputNodes.length != inputLayerNodes) {
            throw new IllegalArgumentException("input node count does not match neural network configuration! received " + inputNodes.length + " instead of " + inputLayerNodes + " input nodes.");
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
        iteration_count++;
        Matrix error = null;
        for (int i = steps.size()-1; i >= 0; i--) {
            if (error == null) {
                error = Matrix.subtract(target, steps.get(steps.size()-1));
            } else {
                error = Matrix.multiply(Matrix.transpose(layers.get(i+1).weight), error);
            }
            Matrix gradient = steps.get(i).derive();
            gradient.multiplyElementwise(error);
            decreaseLearningRate();
            gradient.multiply(learningRate);
            Matrix delta = Matrix.multiply(gradient, Matrix.transpose((i == 0) ? input : steps.get(i-1)));
            layers.get(i).weight.add(delta);
            layers.get(i).bias.addBias(gradient);
        }

        return Matrix.asList(tmp);
    }

    /**
     * This method can be used to batch train the neural net with the supervised machine learning approach.
     * @param inputSet the input set of possible input node values
     * @param expectedOutputSet the output set of according expected output values
     * @param rounds the count of repetitions of the batch training
     */
    public void train(double[][] inputSet, double[][] expectedOutputSet, int rounds) {
        if (inputSet == null || expectedOutputSet == null) {
            throw new NullPointerException("inputSet and expectedOutputSet are required!");
        }
        for (int i = 0; i < rounds; i++) {
            int sampleIndex = (int) (Math.random() * inputSet.length);
            learn(inputSet[sampleIndex], expectedOutputSet[sampleIndex]);
        }
    }

    /**
     * This method will merge two neural networks to one. Please note that the first neural network given as parameter will be altered in the process.
     * @param a neural network to be altered and merged
     * @param b neural network to be merged
     * @return the neural network a merged with b
     */
    public static NeuralNetwork merge(NeuralNetwork a, NeuralNetwork b) {
        if (a == null || b == null) {
            throw new NullPointerException("two NeuralNetwork instances required!");
        }
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
        neuralNetwork.randomize(learningRate);
        return neuralNetwork;
    }

    /**
     * This method will provide an identical copy of the current neural network. The output neural network will not be connected to the copied neural network.
     * @return an identical copy of this instance
     */
    public NeuralNetwork copy() {
        NeuralNetwork neuralNetwork = new NeuralNetwork(inputLayerNodes, layers);
        neuralNetwork.setRectifier(this.rectifier).setLearningRateDescent(this.learningRateDescent).setLearningRate(this.learningRate).setMomentum(this.momentum);
        return neuralNetwork;
    }

    private void randomize(double factor) {
        for (Layer layer : layers) {
            layer.weight.randomize(factor);
            layer.bias.randomize(factor);
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
     * Supervised learning only.
     * @param learningRateDescent the descent function the learning rate.
     * @return the NeuralNetwork.
     */
    public NeuralNetwork setLearningRateDescent(LearningRateDescent learningRateDescent) {
        this.learningRateDescent = learningRateDescent;
        return this;
    }

    /**
     * Returns current learning rate descent of this NeuralNetwork. Must not match corresponding property.
     * @return the learning rate descent.
     */
    public LearningRateDescent getLearningRateDescent() {
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
     * Decreases the current learning rate according to the chosen LearningRateDescent function.
     */
    public void decreaseLearningRate() {
        this.learningRate = learningRateDescent.decrease(initialLearningRate, momentum, iteration_count);
        iteration_count++;
    }

    /**
     * This method will reset the learning rate to its original state.
     */
    public void resetLearningRate() {
        this.learningRate = this.initialLearningRate;
    }

    /**
     * Sets momentum for the decrease of the learning rate.
     * @param momentum the momentum to decrease the learning rate.
     * @return the NeuralNetwork.
     */
    public NeuralNetwork setMomentum(double momentum) {
        this.momentum = momentum;
        if (momentum < 0 || momentum > 1) {
            throw new IllegalArgumentException("Learning rate must be set between 0.0 and 1.0!");
        }
        return this;
    }

    /**
     * Returns current momentum of this NeuralNetwork. Must not match corresponding property.
     * @return the momentum.
     */
    public double getMomentum() {
        return momentum;
    }

    /**
     * Setter to allow altering properties for the NeuralNetwork configuration.
     * The set value will not be validated within this method. Please see neuralnetwork.properties
     * as guideline.
     * @param key the key of the property.
     * @param value the value of the property.
     */
    public static void setProperty(String key, String value) {
        if (!key.equals("learning_rate") && !key.equals("rectifier") && !key.equals("learning_rate_descent") && !key.equals("learning_decay_momentum") && !key.equals("genetic_reproduction_pool_size")) {
            throw new IllegalArgumentException("Property with key " + key + "is not valid in this context!");
        } else if (key.equals("learning_rate")) {
            if (Double.parseDouble(value) < 0 || Double.parseDouble(value) > 1) {
                throw new IllegalArgumentException("Learning rate must be set between 0.0 and 1.0!");
            }
        } else if (key.equals("learning_decay_momentum")) {
            if (Double.parseDouble(value) < 0 || Double.parseDouble(value) > 1) {
                throw new IllegalArgumentException("Momentum must be set between 0.0 and 1.0!");
            }
        } else if (key.equals("genetic_reproduction_pool_size")) {
            if (Double.parseDouble(value) < 2) {
                throw new IllegalArgumentException("Reproduction pool size must be set above 2!");
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
        sb.append("momentum: ");
        sb.append(momentum);
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
        return Integer.parseInt(learningRate + "" + iteration_count + layers.size());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NeuralNetwork)) {
            return false;
        }
        return this.toString().equals(o.toString());
    }

}
