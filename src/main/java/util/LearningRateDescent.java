package util;

public enum LearningRateDescent {

    NONE("Static implementation") {
        @Override
        public double decrease(double initialLearningRate, double momentum, double iteration) {
            return initialLearningRate;
        }
    },
    SGD("Stochastic Gradient Descent") {
        @Override
        public double decrease(double initialLearningRate, double momentum, double iteration) {
            return initialLearningRate * (1 / (1 + momentum * iteration));
        }
    };

    private final String description;

    LearningRateDescent(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public abstract double decrease(double initialLearningRate, double momentum, double iteration);
}
