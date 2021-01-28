package util;

public enum Rectifier {

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
    SIGMOID("Sigmoid") {
        @Override
        public double activate(double value) {
            return 1/(1 + Math.exp(-value));
        }
        @Override
        public double derive(double value) {
            return value * (1 - value);
        }
    },
    SIGMOID_ACCURATE("Sigmoid with accurate derivation") {
        @Override
        public double activate(double value) {
            return 1/(1 + Math.exp(-value));
        }
        @Override
        public double derive(double value) {
            return Math.exp(-value) / Math.pow((Math.exp(-value) + 1), 2);
        }
    },
    SILU("Sigmoid Linear Unit") {
        @Override
        public double activate(double value) {
            return value/(1 + Math.exp(-value));
        }
        @Override
        public double derive(double value) {
            return value * (1 - value);
        }
    },
    SILU_ACCURATE("Sigmoid Linear Unit with accurate derivation") {
        @Override
        public double activate(double value) {
            return value/(1 + Math.exp(-value));
        }
        @Override
        public double derive(double value) {
            return (Math.exp(value) * (value + Math.exp(value) + 1)) / Math.pow(Math.exp(value) + 1, 2);
        }
    },
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
    GELU("Gaussian Error Linear Unit") {
        @Override
        public double activate(double value) {
            return 0.5 * value * (1 + Math.tanh(Math.sqrt(2 / Math.PI) * (value+ 0.044715 * (value * Math.pow(value, 3)))));
        }
        @Override
        public double derive(double value) {
            return 0.5 * Math.tanh(0.0356774*Math.pow(value, 3)+ 0.797885 * value) + (0.0535161 * Math.pow(value, 3)+ 0.398942 * value) * Math.pow((1/Math.cosh(0.0356774 * Math.pow(value, 3) + 0.797885 * value)), 2) + 0.5;
        }
    },
    SOFTPLUS("Softplus") {
        @Override
        public double activate(double value) {
            return Math.log(1 + Math.exp(value));
        }
        @Override
        public double derive(double value) {
            return Math.exp(value) / (1 + Math.exp(value));
        }
    };

    private final String description;

    Rectifier(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public abstract double activate(double value);

    public abstract double derive(double value);
}
