package neuralnet;

import java.util.*;

/**
 * This is the central class of this library, allowing to create a freely configurable neural network.
 * The architecture can be set by parameters. Input and output values are designed to be double arrays.
 * It supports supervised and unsupervised machine learning.
 */
public class NeuralNetwork {

    private List<Layer> layers = new ArrayList<>();
    private int inputLayerNodes;
    private double randomizationRate = 0.1;

    /**
     * A constructor of the neural network.
     * The varag parameters will define the architecture of the neural network. You need to enter at least two parameters.
     * Please note there is another constructor which allows to set the randomization rate for clones.
     * @param layerParams the architecture of the neural network. First argument = node count of input layer; last argument = node count of output layer; arguments between = node count per hidden layer.
     */
    public NeuralNetwork(int... layerParams) {
        if (layerParams.length < 2) {
            throw new IllegalArgumentException("enter at least two arguments to create neural network!");
        }
        this.inputLayerNodes = layerParams[0];
        for (int i = 1; i < layerParams.length; i++) {
            layers.add(new Layer(layerParams[i], layerParams[i-1]));
        }
    }

    /**
     * A constructor of the neural network.
     * The varag parameters will define the architecture of the neural network. You need to enter at least two parameters.
     * Please note there is another constructor which does not require randomization rate (defaults to 0.1).
     * @param randomizationRate the randomization rate, used for unsupervised machine learning approach where cloning instead of back propagating is used.
     * @param layerParams the architecture of the neural network. First argument = node count of input layer; last argument = node count of output layer; arguments between = node count per hidden layer.
     */
    public NeuralNetwork(double randomizationRate, int... layerParams) {
        this(layerParams);
        this.randomizationRate = randomizationRate;
    }

    private NeuralNetwork(int inputLayerNodes, double randomizationRate, List<Layer> layers) {
        List<Layer> newLayerSet = new ArrayList<>();
        for (Layer layer : layers) {
            newLayerSet.add(layer.clone());
        }
        this.layers = newLayerSet;
        this.inputLayerNodes = inputLayerNodes;
        this.randomizationRate = randomizationRate;
    }

    /**
     * This method will take input nodes as parameter and return the predicted output nodes.
     * The neural net will not be modified. This method can be used for testing or the unsupervised machine learning approach.
     * @param input the input nodes as double array
     * @return the predicted output nodes as Double List
     */
    public List<Double> predict(double[] input) {
        if (input.length != inputLayerNodes) {
            throw new IllegalArgumentException("input node count does not match neural network configuration!");
        }

        Matrix tmp = Matrix.fromArray(input);

        for (Layer layer : layers) {
            tmp = Matrix.multiply(layer.weight, tmp);
            tmp.addBias(layer.bias);
            tmp.sigmoid();
        }

        return Matrix.toArray(tmp);
    }

    /**
     * This method will take input nodes as well as the expected output nodes as parameter and return the predicted output nodes.
     * The neural net will be modified and back propagate the expected values to adjust the weighed layers. This method can be used for training as supervised machine learning algorithm.
     * @param inputNodes the input nodes as double array
     * @param expectedOutputNodes the expected output nodes as double array
     * @return the actual output nodes as Double List
     */
    public List<Double> learn(double[] inputNodes, double[] expectedOutputNodes) {
        if (inputNodes.length != inputLayerNodes) {
            throw new IllegalArgumentException("input node count does not match neural network configuration!");
        }

        Matrix input = Matrix.fromArray(inputNodes);

        // forward propagate and prepare output
        List<Matrix> steps = new ArrayList<>();
        Matrix tmp = input;
        for (Layer layer : layers) {
            tmp = Matrix.multiply(layer.weight, tmp);
            tmp.addBias(layer.bias);
            tmp.sigmoid();
            steps.add(tmp);
        }

        Matrix target = Matrix.fromArray(expectedOutputNodes);

        // backward propagate to adjust weights in layers
        Matrix error = null;
        for (int i = steps.size()-1; i >= 0; i--) {
            if (error == null) {
                error = Matrix.subtract(target, steps.get(steps.size()-1));
            } else {
                error = Matrix.multiply(Matrix.transponse(layers.get(i+1).weight), error);
            }
            Matrix gradient = steps.get(i).dsigmoid();
            gradient.multiplyElementwise(error);
            gradient.multiply(randomizationRate);
            Matrix delta = Matrix.multiply(gradient, Matrix.transponse((i == 0) ? input : steps.get(i-1)));
            layers.get(i).weight.add(delta);
            layers.get(i).bias.addBias(gradient);
        }

        return Matrix.toArray(tmp);
    }

    /**
     * This method can be used to batch train the neural net with the supervised machine learning approach.
     * @param inputSet the input set of possible input node values
     * @param expectedOutputSet the output set of according expected output values
     * @param rounds the count of repetitions of the batch training
     */
    public void train(double[][] inputSet, double[][] expectedOutputSet, int rounds) {
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
        for (int i = 0; i < a.layers.size(); i++) {
            a.layers.get(i).weight = Matrix.merge(a.layers.get(i).weight, b.layers.get(i).weight);
            a.layers.get(i).bias = Matrix.merge(a.layers.get(i).bias, b.layers.get(i).bias);
        }
        return a;
    }

    /**
     * This method may be used to cleanse the output of previous runs with the neural network.
     * @param nodeList the Double list of results
     * @return the index of the max value
     */
    public static int getMaxindex(List<Double> nodeList) {
        if (nodeList == null || nodeList.size() == 0) {
            throw new IllegalArgumentException("Invalid input list!");
        }
        return nodeList.indexOf(Collections.max(nodeList));
    }

    /**
     * This method will provide a randomized clone of the current neural network. The output neural network will not be connected to the cloned neural network.
     * @return a randomized clone of this instance
     */
    @Override
    public NeuralNetwork clone() {
        NeuralNetwork net = new NeuralNetwork(inputLayerNodes, randomizationRate, layers);
        net.randomize(randomizationRate);
        return net;
    }

    private void randomize(double factor) {
        for (Layer layer : layers) {
            layer.weight.randomize(factor);
            layer.bias.randomize(factor);
        }
    }

}
