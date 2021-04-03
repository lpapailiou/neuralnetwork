package neuralnet;

public enum Regularizer {       // TODO

    NONE("No regularization") {
        @Override
        public double get(Matrix matrix, double lambda) {
            return 0;
        }

        @Override
        public Matrix gradient(Matrix matrix, double lambda) {
            return new Matrix(matrix.getRows(), matrix.getCols());
        }
    },
    L1("L1 regularization") {
        @Override
        public double get(Matrix matrix, double lambda) {
            return lambda * Matrix.apply(matrix, Math::abs).sum();
        }

        @Override
        public Matrix gradient(Matrix matrix, double lambda) {
            return Matrix.apply(matrix, x -> lambda * Math.signum(x));
        }
    },
    L2("L2 regularization") {
        @Override
        public double get(Matrix matrix, double lambda) {
            return lambda * Matrix.apply(matrix, x -> x * x).sum();
        }

        @Override
        public Matrix gradient(Matrix matrix, double lambda) {
            return Matrix.apply(matrix, x -> 2 * lambda * x);
        }
    },
    ELASTIC("Elastic net regularization") {
        @Override
        public double get(Matrix matrix, double lambda) {
            return ((1 - lambda) * Matrix.apply(matrix, Math::abs).sum()) + (lambda * Matrix.apply(matrix, x -> x * x).sum());
        }

        @Override
        public Matrix gradient(Matrix matrix, double lambda) {
            return Matrix.apply(matrix, x -> (1 - lambda) * Math.signum(x) + (2 * lambda * x));
        }
    },
    DROPOUT("Dropout") {
        @Override
        public double get(Matrix matrix, double lambda) {
            return 0;
        }

        @Override
        public Matrix gradient(Matrix matrix, double lambda) {
            return new Matrix(matrix.getRows(), matrix.getCols());
        }
    };

    private final String description;

    Regularizer(String description) {
        this.description = description;
    }

    public double get(Matrix matrix, double lambda) {
        return 0;
    }

    public Matrix gradient(Matrix matrix, double lambda) {
        return null;
    }

}
