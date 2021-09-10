package ch.kaiki.nn.neuralnet;

import org.junit.Test;
import ch.kaiki.nn.util.Rectifier;

import java.util.ArrayList;
import java.util.List;

public class BackpropagationTest {

    double[][] in = {{0, 0}, {1, 0}, {0, 1}, {1, 1}};
    double[][] out = {{0}, {1}, {1}, {0}};

    @Test
    public void backpropagationTest() {
        Rectifier rectifier = Rectifier.SIGMOID;
        double[] input = new double[]{-1, -2};
        double[] weights = new double[]{2, -3};
        Matrix bias = Matrix.fromArray(new double[]{-3});
        Matrix v = Matrix.fromArray(input);
        Matrix w = Matrix.fromArray(weights);
        w = Matrix.transpose(w);

        Matrix node = Matrix.multiply(w, v);
        System.out.println("node: " + node);
        node.addBias(bias);
        System.out.println("after bias addition " + node);
        node.activate(rectifier);
        System.out.println("after activation: " + node);


        List<Matrix> steps = new ArrayList<>();
        steps.add(node);


        Matrix target = Matrix.fromArray(new double[]{1});

        Matrix error = null;
        for (int i = steps.size() - 1; i >= 0; i--) {
            /*if (error == null) {
                error = Matrix.subtract(target, steps.get(steps.size()-1));
            } else {
                error = Matrix.multiply(Matrix.transpose(w), error);
            }*/
            error = target;


            System.out.println("error: " + error);
            Matrix gradient = steps.get(i).derive(rectifier);
            System.out.println("derived: " + steps.get(i));
            gradient.multiply(error);
            System.out.println("gradient 1: " + gradient);
            gradient.multiply(0.8);
            System.out.println("gradient 2: " + gradient);
            Matrix delta = Matrix.multiply(gradient, Matrix.transpose((i == 0) ? v : steps.get(i - 1)));
            System.out.println("delta: " + delta);
            w.add(delta);
            bias.addBias(gradient);
        }

        System.out.println("weight " + w);
        System.out.println("bias " + bias);

    }
}
