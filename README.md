![architecture](doc/img/nn_architecture.png)    

# neural network
This is a maven library for neural networks in java 8.  
Different visualization options are provided with javafx.  

## Table of Contents
1. [About](#about)
2. [Scope](#scope)  
    2.1 [Architecture](#architecture)  
    2.2 [Supported algorithms](#supported-algorithms)  
    2.3 [Initializers](#initializers)  
    2.4 [Rectifiers](#rectifiers)  
    2.5 [Cost functions](#cost-functions)  
    2.6 [Optimizers](#learning-and-mutation-rate-optimizer)  
    2.7 [Parametrization](#parametrization)  
    2.8 [Persistence](#persistence)  
    2.9 [UI](#ui)  
3. [Samples](#samples)  
   3.1 [Overview](#overview)   
   3.2 [Walkthrough](#walkthrough)
4. [Implementation](#implementation)  
5. [Releases](#releases)
6. [References](#references)

## About
The motivation for this library originated in the study project [snakeML](https://github.com/lpapailiou/SnakeML),
where the game snake was to be solved by neural networks. The rudimentary approach was extracted to this library
and improved over time to study both neural networks and play around with java.  
The goal of this library is to make neural networks easily accessible in terms of 'how does it work' and to
provide an easy-to-use and plug-and-play-tool for other projects.  
While the first steps focused on functionality, later work focused on different approaches of visualization with javafx.

## Scope
### Architecture
- fully connected
- none to many hidden layers supported
- node count per layer is configurable

### Supported algorithms
- Supervised learning
- Genetic algorithm

### Initializers
- Static
- Random
- Xavier
- Kaiming

### Rectifiers
Implemented are following rectifiers:
- Identity
- RELU
- Leaky RELU
- Sigmoid
- Sigmoid (accurate)
- SILU
- SILU (accurate)
- TANH
- ELU
- GELU
- Softplus
- Softmax

## Cost functions
- MSE native
- MSE
- Cross entropy
- Exponential
- Hellinger distance
- KLD
- GKLD
- ISD

### Optimizers
- none (static)
- stochastic gradient descent (applicable to learning rate and mutation rate)
- dropout

### Parametrization
The parametrization of the hyperparameters of the neural network can be done as following:
- programmatically
- by `neuralnetwork.properties` in case default values are used constantly

### Persistence
- neural network instances are fully serializable

### UI
With the additional ui package, you may be able to visualize the neural network and additional metrics interactively based on the ``javafx`` framework.
  
## Samples
### Overview
In order to have an idea about the look and feel, see following samples which were created with this library.  

Sample code for a minimal prediction task:

    double[] in = {1,0.5};                                              // input values
    NeuralNetwork neuralNetwork = new NeuralNetwork.Builder(2, 4, 1)
        .setDefaultRectifier(Rectifier.SIGMOID)
        .setLearningRate(0.8)
        .build();                                                       // initialization
    List<Double> out = neuralNetwork.predict(in);                       // prediction

Live visualization of a predicting neural network:   

![architecture](doc/img/nn_graph_demo.gif)

Line charts of available rectifiers:  

![rectifiers](doc/img/rectifier.png)

Visualization of weights of a layer per node and overall, trained on mnist:    

![layer weights](doc/img/weight_plots.png)

Confusion matrix visualization:  

![confusion matrix](doc/img/confusion_matrix.png)

Binary decision boundaries in 2D and 3D, manually refreshed while training on xor dataset:

![binary decision boundaries](doc/img/2d_db_demo.gif)

Multiclass decision boundaries in 3D, animated:

![multiclass decision boundaries](doc/img/3d_db_demo.gif)  

[Fully integrated sample ui for rudimentary analysis of tsp problem](https://github.com/lpapailiou/tspwalker):

![multiclass decision boundaries](doc/img/tsp_demo.gif)
### Walkthrough
Detailed examples are available here:

| Topic 	| Description 	| 
|-----	|---------	|
| [basic usage](doc/WT_01_basic_usage.md) | constructor, methods, basic features |
| [supervised learning](doc/WT_02_supervised_learning.md) | implementation example |
| [genetic algorithm](doc/WT_03_genetic_algorithm.md) | implementation example |
| [visualization](doc/WT_04_visualizations.md) | charts, decision boundaries, confusion matrix, layer weights etc. in 2D and 3D |

## Implementation
This library can be either implemented by jar file or as maven dependency.  
Detailed instructions are documented [here](doc/implementation.md).

## Releases
As this project started 'fun project' and the concept of 'free time' is more a fairy tale than reality, 
there is not a proper version control (yet).  
In general, the neural netowrk algorithm is quite stable, no big changes are to be expected soon.  
Before new features are introduced, a stable, consistent realease will be made.

| Release 	| Description 	| 
|-----	|---------	|
| upcoming   	| stable, consistent release, focusing on consistency        	|
| [3.1](https://github.com/lpapailiou/neuralnetwork/releases/latest)   	| mostly minor fixes and features added, currently treated as snapshot        	|
| 3.0    	| introduction of charts and other visualizations, lot of refactoring        	|
| <= 2.5    | multiple releases focusing on the neural network algorithm        	|

## References
Code:
- [Sonawane, Suyash: JAvaNet, GitHub Repository, 2021.](https://github.com/SuyashSonawane/JavaNet) (first steps)
- [https://stackoverflow.com/](https://stackoverflow.com/) (coding hints)

Literature:
- [Bialas, Piotr: _Implementation of artifical intelligence in Snake game using genetic algorithm and neural networks_, CEUR 2468, 2019.](http://ceur-ws.org/Vol-2468/p9.pdf)
- Chollet, Francois: _Deepl Learning mit Python und Kears. Das Praxis-Handbuch_. mitp Verlags GmbH & Co. KG, Frechen, 2018. (978-3-95845-838-3)
- Geron, Aurelien: _Hands.On Machine Learning with Scikit-Learn, Keras & TensorFlow. Concepts, Tools, and Techniques to Build Intelligent Systems_. Second Edition, O'Reilly, Canada 2019. (978-1-492-03264-9)
- [Hansen, Casper: _Neural Networks: Feedforward and Backpropagation Explained & Optimization_. Deep Learning, mlfromscratch, 5.08.2019.](https://mlfromscratch.com/activation-functions-explained/#/)
- [Hansen, Casper: _Activation Functions Explained - GELU, SELU, ELU, ReLU and more_. Deep Learning, mlfromscratch, 22.08.2019.](https://mlfromscratch.com/activation-functions-explained/#/)
- [Karpathy, Andrej: _CS231n Winter 2016_, Stanford Universy, Youtube, 2016.](https://www.youtube.com/watch?v=NfnWJUyUJYU&list=PLkt2uSq6rBVctENoVBg1TpCC7OQi31AlC)
- Lapan, Maxim: _Deep Reinforcement Learning Hands-On_. Second Edition, Packt Publishing, Birmingham, 2020. (978-1-83882-699-4)
- Steinwendner, Joachim et. al: _Neuronale Netzw programmieren mit Python. 2_. Auflage, Rheinwerk Verlag, 2020. (978-3-8362-7450-0)