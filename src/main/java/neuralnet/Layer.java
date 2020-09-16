package neuralnet;

/**
 * This is a helper class to assure a dynamic and configurable architecture for the neural network.
 */
class Layer {

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
}
