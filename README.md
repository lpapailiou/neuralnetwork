# neural network library
This is a maven library for neural networks in java 8.  
  
## Table of Contents
1. [Features](#features)  
	1.1 [Architecture](#architecture)  
	1.2 [Supported algorithms](#supported-algorithms)  
    1.3 [Rectifiers](#rectifiers)  
    1.4 [Learning rate descent](#learning-and-mutation-rate-descent)  
    1.5 [Parametrization](#parametrization)  
    1.6 [Persistence](#persistence)  
    1.7 [UI](#ui)  
2. [Examples](#examples)  
    2.1 [Constructor of NeuralNetwork](#constructor-of-neuralnetwork)   
    2.2 [Methods of NeuralNetwork](#methods-of-neuralnetwork)   
    2.3 [Supervised learning](#supervised-learning)   
    2.4 [Genetic algorithm](#genetic-algorithm)   
    2.5 [Graphic representation](#graphic-representation)   
3. [Implementation](#implementation)  
    3.1 [From a Jar file](#from-a-jar-file)  
    3.2 [From a Maven dependency](#from-a-maven-dependency)  
    3.3 [Properties](#properties)  

## Features
### Architecture
- fully connected
- none to many hidden layers supported
- node count per layer is configurable

### Supported algorithms
- Supervised learning
- Genetic algorithm

### Rectifiers
Implemented are following rectifiers:
- Identity
- Sigmoid
- Sigmoid (accurate)
- SILU
- SILU (accurate)
- RELU
- TANH
- GELU
- Softplus

Rectifiers can be quite sensitive to hyperparameters (e.g. normalization, learning rate). 

### Learning and mutation rate descent
- none (static learning rate)
- stochastic gradient descent

### Parametrization
The parametrization of the neural network can be done as following:
- during initialization (vararg for architecture + builder pattern)
- during runtime (according setters)
- by `neuralnetwork.properties` in case default values are used constantly

### Persistence
- neural network instances are fully serializable

### UI
With the additional ui package, you may be able to visualize the neural network interactively with a javafx framework.
  
## Examples
### Constructor of NeuralNetwork
Create a neural network with three input nodes and two output nodes:

    NeuralNetwork neuralNetwork = new NeuralNetwork(3, 2);
  
Create a neural network with two input nodes, two hidden layers (4 and 5 nodes) and 6 output nodes:

    NeuralNetwork neuralNetwork = new NeuralNetwork(2, 4, 5, 6);
  
Create a neural network adding parameters (builder pattern may be used as well).

    NeuralNetwork neuralNetwork = new NeuralNetwork(0.7, 2, 2);    // first parameter is initial randomziation rate
    neuralNetwork.setNormalized(false);
    neuralNetwork.setRectifier(Rectifier.SIGMOID);
    neuralNetwork.setLearningRateDescent(Descent.SGD);
    neuralNetwork.setLearningRate(0.8);
    neuralNetwork.setLearningRateMomentum(0.005);
    neuralNetwork.setMutationRateDescent(Descent.SGD);
    neuralNetwork.setMutationRate(0.5);
    neuralNetwork.setMutationRateMomentum(0.07);

### Methods of NeuralNetwork
#### Common methods
Use the neural network to predict a value from a given input array.

    double[] in = {1,0.5};                                      // input values
    NeuralNetwork neuralNetwork = new NeuralNetwork(2, 4, 1);
    List<Double> out = neuralNetwork.predict(in);               // prediction

Get a full overview of the actual contents of a neural network by calling the `toString()` method.  
The output will show all matrix components (weights + biases) as well as the neural network metadata.  
  
To access the weights, following method can be used:  

    double[][] weights = neuralNetwork.getWeights(layerIndex);
    
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

    String learningRatePropertyValue = NeuralNetwork.getProperty("learning_rate");
    
    NeuralNetwork.setProperty("rectifier", "sigmoid");
    
#### Supervised learning
Fit the neural network model to given input array and the expected output.
The neural network will return a prediction and adjust accordingly.

    double[] in = {1,0.5};                                      // input
    double[] out = {1,0};                                       // expected output
    NeuralNetwork neuralNetwork = new NeuralNetwork(2, 4, 1);
    List<Double> prediction = neuralNetwork.fit(in, out);       // prediction and adjustment of model
    
Fit the neural network model sequentially with according input and expected output sets.

    double[] in = {{1,0.5}, {0.2,0.9};                          // input set
    double[] out = {{1,0}, {0,1}};                              // expected output set
    NeuralNetwork neuralNetwork = new NeuralNetwork(2, 4); 
    neuralNetwork.fit(in, out, 2000);                           // adjustment of model in 2000 iterations

#### Genetic algorithm 
There are two ways to implement this algorithm. You can either rely on the `geneticalgorithm` package 
(see [Genetic algorithm implementation](#genetic-algorithm-implementation)) or do the implementation manually.  
For manual implementation, the required methods are listed below.
  
Merge two instances of NeuralNetwork. This will work only, if both neural networks are of the same architecture. 
Properties / metadata will be copied from the first instance.

    NeuralNetwork neuralNetworkA = new NeuralNetwork(2, 4, 2);
    NeuralNetwork neuralNetworkB = new NeuralNetwork(2, 4, 2);
    NeuralNetwork result = NeuralNetwork.merge(neuralNetworkA, neuralNetworkB);
    
Mutate a neural network according to the current settings.

    NeuralNetwork neuralNetwork = new NeuralNetwork(2, 4, 2);
    neuralNetwork.mutate();
    
If you want to decrease the learning rate manually, you may call `decreaseLearningRate()`. Please note you
should then select another type than `LearningRateDescent.NONE` (and according learning rate and momentum).
As the decreasing learning rate is optional and has to be performed on a neural network instance chosen
for reproduction only, it has to be called separately.

    NeuralNetwork neuralNetwork = new NeuralNetwork(2, 4, 2);
    neuralNetwork.mutate();
    // do something
    neuralNetwork.decreaseLerningRate();

Obtain an identical copy of a NeuralNetwork instance.

    NeuralNetwork master = new NeuralNetwork(2, 4, 2);
    NeuralNetwork copy = master.copy();
    
### Supervised learning implementation
See below a full xor test of the neural network with supervised learning: 

    // prepare set of expected input values
    double[][] in = {{0,0}, {1,0}, {0,1}, {1,1}};
    
    // prepare corresponding expected output values
    double[][] out = {{0}, {1}, {1}, {0}};
    
    // create and configure neural network
    Rectifier rectifier = Rectifier.SIGMOID
    NeuralNetwork neuralNetwork = new NeuralNetwork(2, 4, 1);
    neuralNetwork.setRectifier(rectifier).setLearningRate(0.8).setLearningRateDescent(LearningRateDescent.NONE);
    
    // train neural network with the input set, the corresponding expected output set and the training epochs
    neuralNetwork.train(in, out, 2000);
    
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
The package `geneticalgorithm` offers a convenient base for implementing the genetic algorithm easily.  
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

    NeuralNetwork seed = new NeuralNetwork(4, 8, 4);
    int populationSize = 1000;
    int generationCount = 50;
     
    GeneticAlgorithmBatch<GeneticObjectExample> batch = new GeneticAlgorithmBatch<>(GeneticObjectExample.class, seed, populationSize, generationCount);
    
    while (seed != null) {
        seed = batch.processGeneration();
    }

... and then do whatever you like to do with it.

### Graphic representation
With the package `ui` you will get access to the neural network visualizer. It uses the javafx framework.
  
It will build a graph of a specific neural network and is able to visualize its architecture, weight distribution and current
node values. See here an example (code is available in the `test/java/ui` directory):

![graph of neural network](https://github.com/lpapailiou/neuralnetwork/blob/master/src/main/resources/img/neural_network_visualizer.png)

The visualizer can be initialized with an available GraphicsContext object only. At this point, no neural network is available yet
for display, so no graph will appear so far.

    NNVisualizer visualizer = new NNVisualizer(context);
    
As soon as the object is created, the neural network can be set. As the visualizer sets a property change listener to the neural
network, it will react automatically as soon as a prediction was made.

    NeuralNetwork neuralNetwork = new NeuralNetwork(2, 3, 2);
    visualizer.setNeuralNetwork(neuralNetwork);              // at this point, the graph becomes visible
    
    neuralNetwork.predict(new double[]{1, 0});               // at this point, the visualizer will react

If you like to change the colors, you can do so by setting a custom color palette.

    NNColorPalette colors = new NNColorPalette(    
        WHITESMOKE,                                 // background color
        BLACK,                                      // node color
        BLACK,                                      // line color
        ROYALBLUE.brighter(),                       // flashed node color (max. output value)
        GAINSBORO,                                  // inactive input node color
        STEELBLUE,                                  // upper value node color
        INDIANRED,                                  // lower value node color
        STEELBLUE.darker(),                         // upper value weight color
        INDIANRED.darker());                        // lower value weight color
        
    visualizer.setColorPalette(colors);

The thresholds for the color switch between negative / neutral / positive may be changed individually for nodes and weights.

    double lowerBound = 0.3;
    double upperBound = 0.7;
    
    visualizer.setNodeColorThreshold(lowerBound, upperBound);
    visualizer.setWeightColorThreshold(lowerBound, upperBound);
    
Further parametrization possibilities are listed below:

    visualizer.setWidthOffset(20.0);
    visualizer.setInputNodeLabels(new String[]{"a", "b"});
    visualizer.setOutputNodeLabels(new String[]{"0", "1"});
    visualizer.setNodeRadius(7.0);
    visualizer.setLineWidth(2.0);
    visualizer.setTextLineWidth(2.0);
    visualizer.setFontSize(12.0);
    
There is also the edge case covered where your neural network has less input nodes than you want to display.
Now, you can pretend there would be more input nodes and just add them graphically. You need to set the 
total count of input nodes the graph should have and list the indexes of the input nodes which should appear as inactive. 

    int totalNodes = 3;
    int[] inactiveIndexes = new int[]{1};
    visualizer.setGraphInputNodeCount(totalNodes, inactiveIndexes);

## Implementation
### From a Jar file
You can download the Jar file directly from the [latest release](https://github.com/lpapailiou/neuralnetwork/releases/latest). Alternatively, you can build it yourself.  
Just add the jar as external library. Be sure to copy the ``neuralnetwork.properties`` file to your `resources` folder.
  
### From a Maven dependency  
Add following snippets to your ``pom.xml`` file to import the library:

    <repositories>    
        <repository>    
            <id>neuralnetwork</id>    
            <url>https://github.com/lpapailiou/neuralnetwork/raw/master</url>    
        </repository>    
    </repositories>      
  
    <dependencies>    
        <dependency>    
            <groupId>neuralnetwork</groupId>    
            <artifactId>neural-network-repo</artifactId>    
            <version>2.5</version>    
        </dependency>    
    </dependencies>    
    
Please note the `neuralnetwork.properties` file should be imported as well to your `resources` folder.  
In case it is not present, you may include following plugin to your `pom.xml` file:  

    <build>
        <outputDirectory>target/classes</outputDirectory>
        <resources>
            <resource>
                <directory>
                    src/main/resources
                </directory>
            </resource>
        </resources>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <configuration>
                        <source>8</source>
                        <target>8</target>
                        <includes>
                            <include>directory path/*.jar</include>
                        </includes>
                    </configuration>
                </plugin>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-dependency-plugin</artifactId>
                    <version>3.1.1</version>
                    <executions>
                        <execution>
                            <id>resource-dependencies</id>
                            <phase>compile</phase>
                            <goals>
                                <goal>unpack</goal>
                            </goals>
                            <configuration>
                                <artifactItems>
                                    <artifactItem>
                                        <groupId>neuralnetwork</groupId>
                                        <artifactId>neural-network-repo</artifactId>
                                        <version>${neuralnetwork.version}</version>
                                        <type>jar</type>
                                        <overWrite>true</overWrite>
                                    </artifactItem>
                                </artifactItems>
                                <includes>**/*.properties</includes>
                                <outputDirectory>${project.basedir}/src/main/resources</outputDirectory>
                            </configuration>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>

and run `mvn compile` to complete the import of the properties file.

### Properties
Below an overview of the `neuralnetwork.properties` file.

    # ************************************************************************************************************ #
    # ***********                                  COMMON PROPERTIES                                   *********** #
    # ************************************************************************************************************ #
    
    # the randomization percentage at the initialization of the neural network matrices.
    # must have a value between 0.0 and 1.0.
    initial_randomization=1.0
    
    # if normalized, matrix components are bound to values between -1 and 1.
    # available values: true|false
    normalize_matrices=false
    
    # the learning rate must have a value between 0.0 and 1.0.
    learning_rate=0.8
    
    # the default rectifier as activation function for the neural network.
    # available values: gelu|identity|relu|sigmoid|sigmoid_accurate|silu|silu_accurate|softplus|tanh.
    rectifier=sigmoid
    
    # the descent of the learning rate between iterations.
    # available values: none|sgd
    learning_rate_descent=sgd
    
    # the learning rate decay as momentum.
    # if learning_rate_descent is set to 'none', this value will have no effect.
    # must have a value between 0.0 and 1.0.
    learning_decay_momentum=0.01
    
    # ************************************************************************************************************ #
    # ***********                                GENETIC ALGORITHM ONLY                                *********** #
    # ************************************************************************************************************ #
    
    # the reproduction pool is the count of NeuralNetworks chosen for reproduction to be seeded to
    # the new generation to come. value must not be below 2.
    genetic_reproduction_pool_size=3
    
    # the mutation rate is the percentage of the mutated components of the neural network matrices.
    # must have a value between 0.0 and 1.0
    genetic_mutation_rate=0.5
    
    # the descent of the mutation rate between iterations.
    # available values: none|sgd
    mutation_rate_descent=sgd
    
    # the mutation rate decay as momentum.
    # if mutation_rate_descent is set to 'none', this value will have no effect.
    # must have a value between 0.0 and 1.0.
    mutation_decay_momentum=0.01
