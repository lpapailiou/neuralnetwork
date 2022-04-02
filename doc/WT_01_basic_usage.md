# walkthrough: basic usage
constructor, methods, basic features  
  
## Table of Contents
1. [Concept](#concept)    
   1.1 [Hyperparameter handling](#hyperparameter-handling)    
   1.2 [Data format](#data-format)   
2. [Constructor](#scope)  
3. [Methods](#methods)  
   3.1 [Common methods](#common-methods)  
   3.2 [Supervised algorithm](#supervised-learning)   
   3.3 [Genetic algorithm](#genetic-algorithm)  
4. [Properties](#properties)  
5. [Listener](#listener)  
6. [Persistence](#persistence)  

## Concept
### Hyperparameter handling
One one hand, there are a lot of hyperparameters to handle. On the other hand, this library should
be easy to use and provide stability.  
For this reason, the neural network is to be initialized with a builder pattern.

1. Selected hyperparameters can be passed to the builder for initialization.
2. If not passed, a hyperparameter will fall back to a default value.
3. The default values are configurable by [properties](#properties).

The downside of this approach that it may impact speed (e.g. during batch copy processes using the genetic algorithm).

### Data format
Internal computations are handled with the ``double[]`` type. This is why the input data
must be of the same format.  
The output format of predictions is by default a ``List<Double>``, which is especially handy for classification tasks.

## Constructor
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
Default values for parameters can be overwritten by changing the [properties](#properties).

## Methods
### Common methods
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
    
### Supervised learning
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

For more details, see [supervised learning walkthrough](WT_02_supervised_learning.md).

### Genetic algorithm 
There are two ways to implement this algorithm. You can either rely on the `genetic` package 
(see [genetic algorithm walkthrough](WT_03_genetic_algorithm.md)) or do the implementation manually.  
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

## Properties
Default properties can be set by editing the[properties file](../src/main/resources/neuralnetwork.properties). They
can be also overridden programmatically. An instance of the neural network will load the properties during initialization.

    String learningRatePropertyValue = NeuralNetwork.Builder.getProperty("learning_rate");
    
    NeuralNetwork.Builder.setProperty("rectifier", "sigmoid");

## Listener
Every time a prediction is made, it will emit a property change event. Here an example to set up an
according PropertyChangeListener:

    neuralNetwork.addListener(e -> {
        List<List<Double>> nodeValues = neuralNetwork.getCachedNodeValues();
        // do something...
    });

## Persistence
If a trained neural network is to be persisted, it can be serialized.

    NeuralNetwork neuralNetwork = new NeuralNetwork.Builder(3,2,1).setDefaultRectifier(Rectifier.TANH).build();
    NNSerializer.serializeToTempDirectory(neuralNetwork);

Deserialization occurs as following:

    NeuralNetwork neuralNetwork = NNSerializer.deserializeNeuralNetwork("C:\\path\\to\\file\\NeuralNetwork.ser");
    System.out.println(neuralNetwork);



