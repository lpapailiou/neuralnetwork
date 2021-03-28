package util;

/**
 * Ths enmum defines different initializers for initializing the matrix components of a neural network.
 */
public enum Initializer {

    /**
     * The static initializer will return the parameter value without further modifications.
     */
    STATIC("Static initialization (scalable)") {
        @Override
        public double getValue(double value, int fanIn, int fanOut, boolean isBias) {
            return value;
        }
    },
    /**
     * The random initializer will return a random value between -1 and 1, scaled by the parameter value.
     */
    RANDOM("Random initialization (scalable)") {
        @Override
        public double getValue(double value, int fanIn, int fanOut, boolean isBias) {
            return ((Math.random() * 2) - 1) * value;
        }
    },
    /**
     * The Xavier initializer will return a random value between -1 and 1 scaled by
     * sqrt(6) / sqrt(fan in / fan out). Bias weights will be set to 0;
     */
    XAVIER("Xavier initialization") {
        @Override
        public double getValue(double value, int fanIn, int fanOut, boolean isBias) {
            if (isBias) {
                return 0;
            }
            return ((Math.random() * 2) - 1) * (Math.sqrt(6) / Math.sqrt(fanIn + fanOut));
        }
    },
    /**
     * The Kaiming initializer will return a random value between -1 and 1 scaled by
     * sqrt(fan in / 2). Bias weights will be set to 0;
     */
    KAIMING("Kaimin initialization") {
        @Override
        public double getValue(double value, int fanIn, int fanOut, boolean isBias) {
            if (isBias) {
                return 0;
            }
            return ((Math.random() * 2) - 1) * Math.sqrt(fanIn / 2);
        }
    };

    private final String description;

    Initializer(String description) {
        this.description = description;
    }

    /**
     * Gets short description of the initializer.
     * @return the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns initial value for a weight or bias matrix component.
     * Depending on the chosen enum, not all parameters will be read.
     * @param value the value is either used as constant or scalar.
     * @param fanIn the fan in is the count of incoming values.
     * @param fanOut the fan out is the count of outgoing values.
     * @param isBias this boolean indicates if the value is used to initialize a bias or a weight.
     * @return the initialization value for a matrix component.
     */
    public double getValue(double value, int fanIn, int fanOut, boolean isBias) {
        return 0;
    }

}
