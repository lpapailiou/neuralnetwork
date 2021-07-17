package nn;

public enum CostFunction {

    /**
     * cost:       f(x)  = 0.5 * sum((actual - expected)^2)
     * gradient:   f(x)' = (actual - expected)
     */
    MSE_NAIVE("Mean squared error (naïve)") {
        @Override
        public double cost(Matrix actual, Matrix expected) {
            Matrix difference = Matrix.subtract(actual, expected);
            return 0.5 * Matrix.dotProduct(difference, difference);
        }

        @Override
        public Matrix gradient(Matrix actual, Matrix expected) {
            Matrix difference = Matrix.subtract(actual, expected);
            return difference;
        }
    },
    /**
     * cost:       f(x)  = 1/m * sum((actual - expected)^2)
     * gradient:   f(x)' = 2 * (actual - expected)
     */
    MSE("Mean squared error") {
        @Override
        public double cost(Matrix actual, Matrix expected) {
            Matrix difference = Matrix.subtract(actual, expected);
            return (1.0 / actual.getRows()) * Matrix.dotProduct(difference, difference);
        }

        @Override
        public Matrix gradient(Matrix actual, Matrix expected) {
            Matrix difference = Matrix.subtract(actual, expected);
            return Matrix.apply(difference, x -> x * 2);
        }
    },
    /**
     * cost:       f(x)  = -sum(expected * ln(actual) + (1 - expected) * ln(1 - actual))
     * gradient:   f(x)' = (actual - expected) / ((1 - actual) * actual)
     */
    CROSS_ENTROPY("Cross entropy") {
        @Override
        public double cost(Matrix actual, Matrix expected) {
            return Matrix.crossEntropy(actual, expected);
        }

        @Override
        public Matrix gradient(Matrix actual, Matrix expected) {
            return Matrix.crossEntropyGradient(actual, expected);
        }
    },
    /**
     * takes parameter tau
     * cost:       f(x)  = tau * e^((1 / tau) * sum((actual - expected)^2))
     * gradient:   f(x)' = (2 / tau) * (actual - expected) * cost(actual, expected)
     */
    EXPONENTIAL("Exponential") {
        double tau = 0.1;

        @Override
        public double cost(Matrix actual, Matrix expected) {
            Matrix difference = Matrix.subtract(actual, expected);
            double sum = Matrix.dotProduct(difference, difference);
            return tau * Math.exp((1 / tau) * sum);

        }

        @Override
        public Matrix gradient(Matrix actual, Matrix expected) {
            Matrix difference = Matrix.subtract(actual, expected);
            Matrix m = Matrix.apply(difference, x -> x * (2 / tau));
            m.multiply(cost(actual, expected));
            return m;
        }
    },
    /**
     * Used for positive values only, ideally between 0 and 1.
     * cost:       f(x)  = (1 / sqrt(2)) * sum((sqrt(actual) - sqrt(expected))^2)
     * gradient:   f(x)' = (sqrt(actual) - sqrt(expected)) / (sqrt(2) * sqrt(actual))
     */
    HELLINGER_DISTANCE("Hellinger distance") {
        @Override
        public double cost(Matrix actual, Matrix expected) {
            Matrix difference = Matrix.subtract(Matrix.apply(actual, Math::sqrt), Matrix.apply(expected, Math::sqrt));
            return (1 / Math.sqrt(2)) * Matrix.dotProduct(difference, difference);

        }

        @Override
        public Matrix gradient(Matrix actual, Matrix expected) {
            Matrix difference = Matrix.subtract(Matrix.apply(actual, Math::sqrt), Matrix.apply(expected, Math::sqrt));
            return Matrix.apply(difference, x -> x / (Math.sqrt(2) * Math.sqrt(x)));
        }
    },
    /**
     * cost:       f(x)  = sum(expected * ln(expected/actual))
     * gradient:   f(x)' = -(expected / actual)
     */
    KLD("Kullback-Leibler divergence") {
        @Override
        public double cost(Matrix actual, Matrix expected) {
            Matrix m = Matrix.apply(actual, expected, (a, e) -> e * Math.log(e / a));
            return m.sum();
        }

        @Override
        public Matrix gradient(Matrix actual, Matrix expected) {
            return Matrix.apply(actual, expected, (a, e) -> e * -e / a);
        }
    },
    /**
     * cost:       f(x)  = sum(expected * ln(expected/actual)) - sum(expected) + sum(actual)
     * gradient:   f(x)' = -((actual - expected) / actual)
     */
    GKLD("Generalized Kullback-Leibler divergence") {
        @Override
        public double cost(Matrix actual, Matrix expected) {
            Matrix m = Matrix.apply(actual, expected, (a, e) -> e * Math.log(e / a));
            return m.sum() - expected.sum() + actual.sum();

        }

        @Override
        public Matrix gradient(Matrix actual, Matrix expected) {
            return Matrix.apply(actual, expected, (a, e) -> (a - e) / a);
        }
    },
    /**
     * cost:       f(x)  = sum((expected / actual) - ln(expected / actual) - 1)
     * gradient:   f(x)' = (actual - expected) / (actual)^2
     */
    ISD("Ikura-Saito distance") {
        @Override
        public double cost(Matrix actual, Matrix expected) {
            return Matrix.apply(actual, expected, (a, e) -> (e / a) - Math.log(e / a) - 1).sum();

        }

        @Override
        public Matrix gradient(Matrix actual, Matrix expected) {
            return Matrix.apply(actual, expected, (a, e) -> (a - e) / (a * a));
        }
    };

    private final String description;


    CostFunction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public double cost(Matrix actual, Matrix expected) {
        return 0;
    }

    public Matrix gradient(Matrix actual, Matrix expected) {
        return null;
    }

}
