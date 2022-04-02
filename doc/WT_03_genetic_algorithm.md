# walkthrough: genetic algorithm
implementation example

## Genetic algorithm implementation
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

