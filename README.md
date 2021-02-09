# neural network library
This is a maven library for neural networks in java 8.  
  
## Table of Contents
1. [Features](#features)  
	1.1 [Architecture](#architecture)  
	1.2 [Supported algorithms](#supported-algorithms)  
    1.3 [Rectifiers](#rectifiers)  
    1.4 [Learning rate descent](#learning-and-mutation-rate-descent)  
    1.5 [Configuration](#configuration)   
2. [Examples](#examples)  
    2.1 [Constructor of NeuralNetwork](#constructor-of-neuralnetwork)   
    2.2 [Methods of NeuralNetwork](#methods-of-neuralnetwork)   
    2.3 [Supervised learning](#supervised-learning)   
    2.4 [Genetic algorithm](#genetic-algorithm)   
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

#### Genetic algorithm
The package `geneticalgorithm` offers a convenient base for implementing the genetic algorithm easily.  
There are two interfaces to be taken care of: 
- `GeneticAlgorithmObject`: may be extended from the abstract class `GeneticAlgorithmObject` or implemented from the interface `IGeneticAlgorithmObject`. This will be the implementation
for the instance being capable of acting according to NeuralNetwork inputs.
- `GeneticAlgorithmBatch`: with this class, the genetic algorithm is implemented and executed from.
The 'actual machine learning part' is processed within the library, and does not have to be taken care of.

### Rectifiers
Implemented are following activation functions:
- Identity
- Sigmoid
- Sigmoid (accurate)
- SILU
- SILU (accurate)
- RELU
- TANH
- GELU
- Softplus

### Learning and mutation rate descent
- none (static learning rate)
- stochastic gradient descent

### Configuration
The configuration of the neural network can be done as following:
- during initialization (vararg for architecture + builder pattern)
- during runtime (according setters)
- by `neuralnetwork.properties` in case default values are used constantly
  
## Examples
### Constructor of NeuralNetwork
Create a neural network with three input nodes and two output nodes:

    NeuralNetwork neuralNetwork = new NeuralNetwork(3, 2);
  
Create a neural network with two input nodes, two hidden layers (4 and 5 nodes) and 6 output nodes:

    NeuralNetwork neuralNetwork = new NeuralNetwork(2, 4, 5, 6);
  
Create a neural network adding parameters (builder pattern may be used as well).

    NeuralNetwork neuralNetwork = new NeuralNetwork(2, 2);
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
Merge two instances of NeuralNetwork, where as the first instance will be returned modified.
This will work only, if both neural networks are of the same architecture.

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

    combo 1: [0.05062227783220413]          // close to 0
    combo 2: [0.9461083391423777]           // close to 1
    combo 3: [0.9425030935131657]           // close to 1
    combo 4: [0.07157249157075309]          // close to 0

### Genetic algorithm implementation
Step one is to create an own class which extends or implements the required functionality:

    public class GeneticObjectExample extends GeneticAlgorithmObject {
    
        public GeneticObject(NeuralNetwork neuralNetwork) {  
            super(neuralNetwork);
        }
    
        @Override
        public boolean perform() {
            return false;       // implement action to be taken
        }
    
        @Override
        public long getFitness() {
            return 0;           // decides how well this instance performed
        }
    
        @Override
        public boolean isImmature() {
            return false;       // semi-important for reproduction
        }
    
        @Override
        public boolean isPerfectScore() {
            return false;       // not important - used to print pretty console log messages
        }
    }

Then create an according batch to start doing what you want to do:  

    NeuralNetwork seed = new NeuralNetwork(4, 8, 4);
    int populationSize = 1000;
    int generationCount = 50;
     
    GeneticAlgorithmBatch<GeneticObjectExample> batch = new GeneticAlgorithmBatch<>(GeneticObjectExample.class, seed, populationSize, generationCount);
    
    while (seed != null) {
        seed = batch.processGeneration();
    }

... and then do whatever you like to do with it.

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

    # **************************************************************************************************** #
    # ***********                              COMMON PROPERTIES                               *********** #
    # **************************************************************************************************** #
    
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
    learning_decay_momentum=0.005
    
    # **************************************************************************************************** #
    # ***********                            GENETIC ALGORITHM ONLY                            *********** #
    # **************************************************************************************************** #
    
    # the reproduction pool is the count of NeuralNetworks chosen for reproduction to be seeded to
    # the new generation to come. value must not be below 2.
    genetic_reproduction_pool_size=3
    
    # the mutation rate is the percentage of the mutated components of the neural network matrices.
    # must have a value between 0.0 and 1.0
    genetic_mutation_rate=0.5
