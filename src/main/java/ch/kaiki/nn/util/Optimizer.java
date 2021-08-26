package ch.kaiki.nn.util;

/**
 * The learning rate optimizer is a contract how the learning rate should decrease over multiple iterations.
 * It may make sense to start with a high learning rate in order to get good results fast. Over multiple
 * iterations, the learning rate decrease will optimize the result.
 */
public enum Optimizer {

    /**
     * The static implementation will have no effect on the learning rate.
     */
    NONE("Static implementation") {
        @Override
        public double decrease(double initialLearningRate, double momentum, double iteration) {
            return initialLearningRate;
        }
    },
    /**
     * The stochastic gradient descent sgd will decrease the learning rate over multiple iterations.
     * It applies smoothness features additionally.
     */
    SGD("Stochastic Gradient Descent") {
        @Override
        public double decrease(double initialLearningRate, double momentum, double iteration) {
            double newLearningRate = initialLearningRate * (1 / (1 + momentum * iteration));
            return Math.max(newLearningRate, 0);
        }
    };

    private final String description;

    Optimizer(String description) {
        this.description = description + " (" + this.name() + ")";
    }

    /**
     * Gets short description of the learning rate optimizer function.
     *
     * @return the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * This method will decrease the learning rate according to the chosen enum type.
     * It will return values between 0.0 and 1.0.
     *
     * @param initialLearningRate the initial learning rate at the time of initialization of the neural network.
     * @param momentum            the momentum, having effect on the amount of the decrease.
     * @param iteration           the current iteration number of the algorithm.
     * @return the new, decreased learning rate.
     */
    public abstract double decrease(double initialLearningRate, double momentum, double iteration);
}
