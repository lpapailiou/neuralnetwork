package neuralnet;

import java.io.Serializable;

/**
 * This is a helper class to assure a dynamic and configurable architecture for the neural network.
 */
class Layer implements Serializable {

    private static final long serialVersionUID = 2L;
    Matrix weight;
    Matrix bias;

    Layer(int m, int n) {
        weight = new Matrix(m, n);
        bias = new Matrix(n, 1);

        // randomize matrices for initial setup
        weight.randomize();
        bias.randomize();
    }

    @Override
    protected Layer clone() {
        Layer layer = new Layer(this.weight.getRows(), this.weight.getCols());
        layer.weight = this.weight.clone();
        layer.bias = this.bias.clone();
        return layer;
    }

    @Override
    public String toString() {
        String weight = "weight: " + this.weight.getType() + "\n" + this.weight.toString();
        String bias = "bias: " + this.bias.getType() + "\n " + this.bias.toString();
        return weight + "\n" + bias + "\n";
    }

    @Override
    public int hashCode() {
        return Integer.parseInt(weight.getCols() + "" + bias.getRows());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Layer)) {
            return false;
        }
        return this.toString().equals(o.toString());
    }
}
