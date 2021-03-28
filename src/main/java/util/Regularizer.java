package util;

public enum Regularizer {       // TODO

    NONE("No regularization"),
    L1("L1 regularization"),
    L2("L2 regularization"),
    L1_L2("L1 and L1 regularization"),
    DROPOUT("Dropout");

    private final String description;

    Regularizer(String description) {
        this.description = description;
    }

    public double getValue(double value, double sum, double lambda, double lambda2) {
        return 0;
    }

}
