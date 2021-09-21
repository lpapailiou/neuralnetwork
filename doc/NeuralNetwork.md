# Neural Network Documentation
This documentation focuses on basic handling of the neural network.  
For visualizations, please see the [NeuralNetwork visualization documentation](NeuralNetworkVisualization.md).
  
## Table of Contents
1 [Constructor of NeuralNetwork](#constructor-of-neuralnetwork)   
2 [Methods of NeuralNetwork](#methods-of-neuralnetwork)   
3 [Supervised learning](#supervised-learning)   
4 [Genetic algorithm](#genetic-algorithm)   
5 [Persistence](#persistance)  
6 [Data](#data)

## Constructor of NeuralNetwork
The constructor is not directly available. It must be invoked by a nested builder class.  
Create a neural network with three input nodes and two output nodes:

    NeuralNetwork neuralNetwork = new NeuralNetwork.Builder(3, 2).build();
  
Create a neural network with two input nodes, two hidden layers (4 and 5 nodes) and 6 output nodes:

    NeuralNetwork neuralNetwork = new NeuralNetwork.Builder(2, 4, 5, 6).build();
  
Create a neural network adding optional parameters.

    NeuralNetwork neuralNetwork = new NeuralNetwork.Builder(2, 2)   // vararg for architecture
        // base settings
        .setInitializer(Initializer.RANDOM)
        .setDefaultRectifier(Rectifier.SIGMOID)
        .setRectifierToLayer(Rectifier.RELU, 0)
        .setLastLayerRectifier(Rectifier.SOFTMAX)
        
        // supervized learning only
        .setCostFunction(CostFunction.MSE)
        .setRegularizer(Recularizer.L1)
        .setRegularizationLambda(0.2)
        .setDropoutFactor(0.5)
        
        // learning rate
        .setLearningRate(0.8)
        .setLearningRateOptimizer(Optimizer.SGD)
        .setLearningRateMomentum(0.005)
        
        // mutation rate (genetic algorithm only)
        .setMutationRate(0.5)
        .setMutationRateOptimizer(Optimizer.SGD)
        .setMutationRateMomentum(0.07)
        
        // validates parameters, then builds and returns NeuralNetwork
        .build();

As parametrization is optional, the builder will rely on default values if a parameter is not given.
Default values for parameters can be overwritten in `neuralnetwork.properties`.

### Methods of NeuralNetwork
#### Common methods
Use the neural network to predict a value from a given input array.

    double[] in = {1,0.5};                                      // input values
    NeuralNetwork neuralNetwork = new NeuralNetwork.Builder(2, 4, 1).build();
    List<Double> out = neuralNetwork.predict(in);               // prediction

Get a full overview of the actual contents of a neural network by calling the `toString()` method.  
The output will show all matrix components (weights + biases) as well as the neural network metadata.  
  
To access the weights, following method can be used:  

    List<double[][]> weights = neuralNetwork.getWeights();
    
Every time a prediction is made, the node values (including input and output nodes) will be cached.
It can be accessed with following method: 
  
    List<List<Double>> nodeValues = neuralNetwork.getCachedNodeValues();
    
Every time a prediction is made, it will emit a property change event. Here an example to set up an
according PropertyChangeListener:
  
    neuralNetwork.addListener(e -> {
        List<List<Double>> nodeValues = neuralNetwork.getCachedNodeValues();
        // do something...
    });
    
Properties can be set by editing the properties file (see details in section [Properties](#properties)). They
can be also set programmatically. An instance of the neural network will load the properties during initialization.

    String learningRatePropertyValue = NeuralNetwork.Builder.getProperty("learning_rate");
    
    NeuralNetwork.Builder.setProperty("rectifier", "sigmoid");
    
#### Supervised learning
Fit the neural network model to given input array and the expected output.
The neural network will return a prediction and adjust accordingly.

    double[] in = {1,0.5};                                      // input
    double[] out = {1,0};                                       // expected output
    NeuralNetwork neuralNetwork = new Builder.NeuralNetwork(2, 4, 1).build();
    List<Double> prediction = neuralNetwork.fit(in, out);       // prediction and adjustment of model
    
Fit the neural network model sequentially with according input and expected output sets.

    double[] in = {{1,0.5}, {0.2,0.9};                          // input set
    double[] out = {{1,0}, {0,1}};                              // expected output set
    NeuralNetwork neuralNetwork = new NeuralNetwork.Builder(2, 4).build(); 
    neuralNetwork.fit(in, out, 2000);                           // adjustment of model in 2000 iterations

#### Genetic algorithm 
There are two ways to implement this algorithm. You can either rely on the `genetic` package 
(see [Genetic algorithm implementation](#genetic-algorithm-implementation)) or do the implementation manually.  
For manual implementation, the required methods are listed below.
  
Merge two instances of NeuralNetwork. This will work only, if both neural networks are of the same architecture. 
Properties / metadata will be copied from the first instance.

    NeuralNetwork neuralNetworkA = new NeuralNetwork.Builder(2, 4, 2).build();
    NeuralNetwork neuralNetworkB = new NeuralNetwork.Builder(2, 4, 2).build();
    NeuralNetwork result = NeuralNetwork.merge(neuralNetworkA, neuralNetworkB);
    
Mutate a neural network according to the current settings.

    NeuralNetwork neuralNetwork = new NeuralNetwork.Builder(2, 4, 2).build();
    neuralNetwork.mutate();
    
If you want to decrease the learning rate manually, you may call `decreaseLearningRate()`. Please note you
should then select another type than `LearningRateOptimizer.NONE` (and according learning rate and momentum).
As the decreasing learning rate is optional and has to be performed on a neural network instance chosen
for reproduction only, it has to be called separately.

    NeuralNetwork neuralNetwork = new NeuralNetwork.Builder(2, 4, 2).build();
    neuralNetwork.mutate();
    // do something
    neuralNetwork.decreaseLerningRate();

Obtain an identical copy of a NeuralNetwork instance.

    NeuralNetwork master = new NeuralNetwork.Builder(2, 4, 2).build();
    NeuralNetwork copy = master.copy();
    
### Supervised learning implementation
See below a full xor test of the neural network with supervised learning: 

    // prepare set of expected input values
    double[][] in = {{0,0}, {1,0}, {0,1}, {1,1}};
    
    // prepare corresponding expected output values
    double[][] out = {{0}, {1}, {1}, {0}};
    
    // create and configure neural network
    Rectifier rectifier = Rectifier.SIGMOID
    NeuralNetwork neuralNetwork = new NeuralNetwork.Builder(2, 4, 1);
        .setDefaultRectifier(rectifier)
        .setLearningRate(0.8)
        .setLearningRateOptimizer(LearningRateOptimizer.NONE)
        .build();
    
    // train neural network with the input set, the corresponding expected output set and the training epochs
    neuralNetwork.fit(in, out, 2000);
    
    // use the trained NeuralNetwork to predict the output values given to the input set
    System.out.println("test with rectifier: " + rectifier.getDescription());
    System.out.println("combo 1: " + neuralNetwork.predict(in[0]));
    System.out.println("combo 2: " + neuralNetwork.predict(in[1]));
    System.out.println("combo 3: " + neuralNetwork.predict(in[2]));
    System.out.println("combo 4: " + neuralNetwork.predict(in[3]));

Output:  
    
    test with recitifier: Sigmoid (SIGMOID)
    combo 1: [0.05062227783220413]          // close to 0
    combo 2: [0.9461083391423777]           // close to 1
    combo 3: [0.9425030935131657]           // close to 1
    combo 4: [0.07157249157075309]          // close to 0

### Genetic algorithm implementation
The package `genetic` offers a convenient base for implementing the genetic algorithm easily.  
There are two implementations to be taken care of: 
- `GeneticAlgorithmObject`: may be extended from the abstract class `GeneticAlgorithmObject` or implemented from 
the interface `IGeneticAlgorithmObject`. It will hold a NeuralNetwork instance, feed its input nodes and react
to the according outputs. Additionally, it should be able to indicate if the action taken was leading to success or not.
- `GeneticAlgorithmBatch`: with this class, the genetic algorithm is implemented and executed from.
The 'actual machine learning part' is processed within the library, and does not have to be taken care of.

Step one is to create an own class which extends or implements the required functionality:

    public class GeneticObjectExample extends GeneticAlgorithmObject {
    
        public GeneticObject(NeuralNetwork neuralNetwork) {  
            super(neuralNetwork);
        }
    
        @Override
        public boolean perform() {
            return false;       // implement action to be taken on one 'step'
        }
    
        @Override
        public long getFitness() {
            return 0;           // metric of how well this instance performed
        }
    
        @Override
        public boolean isImmature() {
            return false;       // if true, the reproduction process will take a shortcut (no roulette selection)
        }
    
        @Override
        public boolean hasReachedGoal() {
            return false;       // if true, customizable log messages will be printed to the console
        }
        
    }

For more details, see javadoc.  
  
Then create an according batch to start doing what you want to do:  

    NeuralNetwork seed = new NeuralNetwork.Builder(4, 8, 4).build();
    int populationSize = 1000;
    int generationCount = 50;
     
    GeneticAlgorithmBatch<GeneticObjectExample> batch = new GeneticAlgorithmBatch<>(GeneticObjectExample.class, seed, populationSize, generationCount);
    
    while (seed != null) {
        seed = batch.processGeneration();
    }

... and then do whatever you like to do with it.

