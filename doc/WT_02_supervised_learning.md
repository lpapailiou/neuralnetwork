# walkthrough: supervised learning
implementation example
    
## Supervised learning implementation
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



