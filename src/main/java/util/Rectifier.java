package util;

/**
 * The rectifier is the activation function for the nodes of the neural networks. Each type holds a short description,
 * the activation function and the derivation function (the latter is used for supervised learning only).
 */
public enum Rectifier {

    /**
     * The identity will not modify the value of the input.
     * formula:    f(x)  = x
     * derivation: f(x)' = 1
     */
    IDENTITY("Identity") {
        @Override
        public double activate(double value) {
            return value;
        }

        @Override
        public double derive(double value) {
            return 1;
        }
    },
    /**
     * The sigmoid is a common activation function as it performs well and its results are quite accurate.
     * The derivation function is simplified in order to get better performance.
     * formula:    f(x)  = 1 / (1 + e^(-x))
     * derivation: f(x)' = x * (1 - x)
     */
    SIGMOID("Sigmoid") {
        @Override
        public double activate(double value) {
            return 1 / (1 + Math.exp(-value));
        }

        @Override
        public double derive(double value) {
            return value * (1 - value);
        }
    },
    /**
     * The sigmoid is a common activation function as it performs well and its results are quite accurate.
     * In this enum type the derivation is mathematically accurate, but performs slightly slower than SIGMOID.
     * formula:    f(x)  = 1 / (1 + e^(-x))
     * derivation: f(x)' = e^(-x) / (e^(-x) + 1)^2
     */
    SIGMOID_ACCURATE("Sigmoid with accurate derivation") {
        @Override
        public double activate(double value) {
            return 1 / (1 + Math.exp(-value));
        }

        @Override
        public double derive(double value) {
            return Math.exp(-value) / Math.pow((Math.exp(-value) + 1), 2);
        }
    },
    /**
     * The sigmoid linear unit is based on the sigmoid function. It's result is comparable with an overshooting
     * RELU function.
     * This simplified version uses the same derivation as the sigmoid, as it performs a bit faster.
     * formula:    f(x)  = x / (1 + e^(-x))
     * derivation: f(x)' = x * (1 - x)
     */
    SILU("Sigmoid Linear Unit") {
        @Override
        public double activate(double value) {
            double step0 = value;
            double step1 = (1 + Math.exp(-value));
            double activation = step0 / step1;
            if (Double.isInfinite(activation)) {
                System.out.println(step0 + " " + step1);
            }
            return activation;
        }

        @Override
        public double derive(double value) {
            double step0 = value;
            double step1 = (1 - value);
            double derivation = step0 * step1;
            if (Double.isInfinite(derivation)) {
                if (derivation < 0) {
                    derivation = Double.MIN_VALUE;
                } else {
                    derivation = Double.MAX_VALUE;
                }
            }
            return derivation;
        }
    },
    /**
     * The sigmoid linear unit is based on the sigmoid function. It's result is comparable with an overshooting
     * RELU function.
     * In this enum type the derivation is mathematically accurate, but performs slightly slower than SILU.
     * formula:    f(x)  = x / (1 + e^(-x))
     * derivation: f(x)' = (e^(x) * (x + e^(x) + 1)) / (e^(x) + 1)^2
     */
    SILU_ACCURATE("Sigmoid Linear Unit with accurate derivation") {
        @Override
        public double activate(double value) {
            return value / (1 + Math.exp(-value));
        }

        @Override
        public double derive(double value) {
            double step0 = (Math.exp(value) * (value + Math.exp(value) + 1));
            double step1 = Math.pow(Math.exp(value) + 1, 2);
            double derivation = step0 / step1;
            if (Double.isInfinite(step0) && Double.isInfinite(step1)) {
                if ((step0 >= 0 && step1 < 0) || (step0 < 0 && step1 >= 0)) {
                    derivation = -1;
                } else {
                    derivation = 1;
                }
            }
            return derivation;
        }
    },
    /**
     * The rectified linear unit is linear growth for values above 0 and 0 for values less or equal to 0.
     * This is a quite common activation function for neural networks. It is quite fragile, as neurons can die during
     * the training.
     * formula:    f(x)  = x        if x greater than 0, else 0
     * derivation: f(x)' = 1        if x greater than 0, else 0
     */
    RELU("Rectified Linear Unit") {
        @Override
        public double activate(double value) {
            return value > 0 ? value : 0;
        }

        @Override
        public double derive(double value) {
            return value > 0 ? 1 : 0;
        }
    },
    /**
     * The leaky rectified linear unit is linear growth for values above 0 and alpha=0.2 for values less or equal to 0.
     * This is a quite common activation function for neural networks. It solves the dead relu problem as
     * gradients will not get suck at 0.
     * formula:    f(x)  = x        if x greater than 0, else 0.2
     * derivation: f(x)' = 1        if x greater than 0, else 0.2
     */
    LEAKY_RELU("Leaky Rectified Linear Unit") {
        @Override
        public double activate(double value) {
            return value > 0 ? value : value * 0.2;
        }

        @Override
        public double derive(double value) {
            return value > 0 ? 1 : 0.2;
        }
    },
    /**
     * The tangent hyperbolic function is similar to the sigmoid, but maps values below zero
     * to negative output values.
     * formula:    f(x)  = tanh(x)
     * derivation: f(x)' = 1 - (x)^2
     */
    TANH("Tangent Hyperbolic Function") {
        @Override
        public double activate(double value) {
            return Math.tanh(value);
        }

        @Override
        public double derive(double value) {
            return 1 - (value * value);
        }
    },
    /**
     * The gaussian error linear unit is the state-of-the-art activation function as it allows good results.
     * On the other hand, it performs slower as the implementation is more complex. This implementation
     * is a simplified version to improve performance.
     * formula:    f(x)  = 0.5 * x * (1 + tanh(sqrt(2 / pi) * (x + 0.044715 * x^3))
     * derivation: f(x)' = 0.5 * tanh(0.0356774 * x^3 + 0.797885 * x) + (0.0535161 * x^3 + 0.398942 * x) * (1 / cosh(0.0356774 * x^3 + 0.797885 * x))^2 + 0.5
     */
    GELU("Gaussian Error Linear Unit") {
        @Override
        public double activate(double value) {
            return 0.5 * value * (1 + Math.tanh(Math.sqrt(2 / Math.PI) * (value + 0.044715 * Math.pow(value, 3))));
        }

        @Override
        public double derive(double value) {
            double step0 = 0.5 * Math.tanh(0.0356774 * Math.pow(value, 3) + 0.797885 * value);
            double step1a = (0.0535161 * Math.pow(value, 3) + 0.398942 * value);
            double step1b = Math.pow((1 / Math.cosh(0.0356774 * Math.pow(value, 3) + 0.797885 * value)), 2);
            double step1 = step1a * step1b;
            if (step1b == 0) {
                step1 = 0;
            }
            double step2 = 0.5;
            double derivation = step0 + step1 + step2;
            return derivation;
        }
    },
    /**
     * The softplus activation function - also popular - is similar to a smoothed RELU, where low negative x values reach a low positive result.
     * formula:    f(x)  = ln(1 + e^x)
     * derivation: f(x)' = e^x / (1 + e^x)
     */
    SOFTPLUS("Softplus") {
        @Override
        public double activate(double value) {
            double activation = Math.log(1 + Math.exp(value));
            if (Double.isInfinite(activation)) {
                if (activation < 0) {
                    activation = Double.MIN_VALUE;
                } else {
                    activation = Double.MAX_VALUE;
                }
            }
            return activation;
        }

        @Override
        public double derive(double value) {
            double step0 = Math.exp(value);
            double step1 = 1 + Math.exp(value);
            double derivation = step0 / step1;
            if (Double.isInfinite(step0) && Double.isInfinite(step1)) {
                if ((step0 >= 0 && step1 < 0) || (step0 < 0 && step1 >= 0)) {
                    derivation = -1;
                } else {
                    derivation = 1;
                }
            }
            return derivation;
        }
    };

    private final String description;

    Rectifier(String description) {
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
     * Returns the result of the according activation function.
     *
     * @param value the input value x
     * @return the result f(x)
     */
    public abstract double activate(double value);

    /**
     * Returns the result of the derivate of the according activation function.
     *
     * @param value the input value x
     * @return the result f(x)'
     */
    public abstract double derive(double value);

}
