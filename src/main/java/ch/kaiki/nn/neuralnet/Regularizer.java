package ch.kaiki.nn.neuralnet;


public enum Regularizer {       // TODO

    NONE("No regularization") {
        @Override
        public double costSummand(Matrix matrix, double lambda) {
            return 0;
        }

        @Override
        public Matrix gradient(Matrix matrix, double lambda) {
            return matrix;
        }

        @Override
        public Matrix weight(Matrix matrix, double lambda, double learningRate) {
            return matrix;
        }
    },
    L1("L1 regularization") {
        @Override
        public double costSummand(Matrix matrix, double lambda) {
            return lambda / (2*matrix.getRows()*matrix.getCols()) * Matrix.apply(matrix, Math::abs).sum();
        }

        @Override
        public Matrix gradient(Matrix matrix, double lambda) {
            return Matrix.apply(matrix, x -> lambda * Math.signum(x));
        }

        @Override
        public Matrix weight(Matrix matrix, double lambda, double learningRate) {
            return matrix; //Matrix.apply(matrix, x -> lambda * Math.signum(x));
        }
    },
    L2("L2 regularization") {
        @Override
        public double costSummand(Matrix matrix, double lambda) {
            return lambda / (2*matrix.getRows()*matrix.getCols()) * Matrix.apply(matrix, x -> x * x).sum();
        }

        @Override
        public Matrix gradient(Matrix matrix, double lambda) {
            return Matrix.apply(matrix, x -> 2 * lambda * x);
        }

        @Override
        public Matrix weight(Matrix matrix, double lambda, double learningRat) {
            return matrix; //Matrix.apply(matrix, x -> 2 * lambda * x);
        }
    },
    ELASTIC("Elastic net regularization") {
        @Override
        public double costSummand(Matrix matrix, double lambda) {
            return ((1 - lambda) * Matrix.apply(matrix, Math::abs).sum()) + (lambda * Matrix.apply(matrix, x -> x * x).sum());
        }

        @Override
        public Matrix gradient(Matrix matrix, double lambda) {
            return Matrix.apply(matrix, x -> (1 - lambda) * Math.signum(x) + (2 * lambda * x));
        }

        @Override
        public Matrix weight(Matrix matrix, double lambda, double learningRate) {
            return matrix; //Matrix.apply(matrix, x -> (1 - lambda) * Math.signum(x) + (2 * lambda * x));
        }
    };

    private final String description;

    Regularizer(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public double costSummand(Matrix matrix, double lambda) {
        return 0;
    }

    public Matrix gradient(Matrix matrix, double lambda) {
        return null;
    }

    public Matrix weight(Matrix matrix, double lambda, double learningRate) {
        return null;
    }

}
