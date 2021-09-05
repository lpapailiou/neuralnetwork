package ch.kaiki.nn.neuralnet;

import ch.kaiki.nn.util.Initializer;
import ch.kaiki.nn.util.Rectifier;

import java.io.Serializable;

/**
 * This is a helper class to assure a dynamic and configurable architecture for the neural network.
 */
class Layer implements Serializable {

    private static final long serialVersionUID = 2L;
    Rectifier rectifier;
    Matrix weight;
    Matrix bias;

    Layer(Rectifier rectifier, int m, int n) {
        weight = new Matrix(m, n);
        bias = new Matrix(m, 1);
        this.rectifier = rectifier;
    }

    void initialize(Initializer initializer, int fanIn, int fanOut) {
        weight.initialize(initializer, fanIn, fanOut, false);
        bias.initialize(initializer, fanIn, fanOut, true);
    }

    Layer copy() {
        Layer layer = new Layer(this.rectifier, this.weight.getRows(), this.weight.getCols());
        layer.weight = this.weight.copy();
        layer.bias = this.bias.copy();
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
