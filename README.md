# neural network library
This is a maven library for neural networks in java 8.  
  
## Table of Contents
1. [Features](#features)  
	1.1 [Architecture](#architecture)  
	1.2 [Supported algorithms](#supported-algorithms)  
    	1.2.1 [Genetic algorithm](#genetic-algorithm)  
    1.3 [Rectifiers](#rectifiers)  
    1.4 [Learning rate descent](#learning-rate-descent)  
    1.5 [Configuration](#configuration)   
2. [Examples](#examples)  
    2.1 [Constructor of NeuralNetwork](#constructor-of-neuralnetwork)   
    2.2 [Supervised learning](#supervised-learning)   
    2.3 [Genetic algorithm](#genetic-algorithm)   
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

### Learning rate descent
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
  
Create a neural network with two output nodes, two hidden layers (4 and 5 nodes) and 6 output nodes:

    NeuralNetwork neuralNetwork = new NeuralNetwork(2, 4, 5, 6);
  
Create a neural network adding hyper parameters (builder pattern may be used as well).

    NeuralNetwork neuralNetwork = new NeuralNetwork(2, 2);
    neuralNetwork.setRectifier(Rectifier.SIGMOID);
    neuralNetwork.setLearningRateDescent(LearningRateDescent.SGD);
    neuralNetwork.setLearningRate(0.8);
    neuralNetwork.setMomentum(0.005);

### Supervised learning
See below a full test of the neural network with supervised learning: 

    // prepare input set
    double[][] in = {{0,0}, {1,0}, {0,1}, {1,1}};
    double[][] out = {{0}, {1}, {1}, {0}};
    
    // create neural network
    NeuralNetwork net = new NeuralNetwork(2, 4, 1);
    net.setRectifier(Rectifier.SIGMOID).setLearningRate(0.8).setLearningRateDescent(LearningRateDescent.NONE);
    
    // train neural network
    net.train(in, out, 2000);
    
    // print output values
    System.out.println("test with rectifier: " + Rectifier.SIGMOID.getDescription());
    System.out.println("combo 1: " + net.predict(in[0]));
    System.out.println("combo 2: " + net.predict(in[1]));
    System.out.println("combo 3: " + net.predict(in[2]));
    System.out.println("combo 4: " + net.predict(in[3]));

Output:  

    combo 1: [0.05062227783220413]
    combo 2: [0.9461083391423777]
    combo 3: [0.9425030935131657]
    combo 4: [0.07157249157075309]

### Genetic algorithm
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
            <version>2.2</version>    
        </dependency>    
    </dependencies>    
    
Please note the `neuralnetwork.properties` file should be imported as well to your `resources` folder.  
In case it is not present, you may include following plugin to your `pom.xml` file:  

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

and run `mvn build` to complete the import of the properties file.

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
