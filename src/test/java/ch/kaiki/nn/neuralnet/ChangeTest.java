package ch.kaiki.nn.neuralnet;

import org.junit.Test;
import ch.kaiki.nn.util.Initializer;

public class ChangeTest {


    @Test
    public void changeTest() {
        Matrix matrix = new Matrix(2, 1);
        Matrix matrixB = new Matrix(2, 1);
        matrix.initialize(Initializer.RANDOM, 0, 1, false);
        matrixB.initialize(Initializer.RANDOM, 0, 1, false);
        System.out.println(matrix);
        System.out.println();

        Matrix tmp = CostFunction.MSE_NAIVE.gradient(matrixB, matrix);

        System.out.println(tmp);
        System.out.println();

        tmp = Matrix.apply(tmp, Regularizer.NONE.gradient(tmp, 0), Double::sum);

        System.out.println(tmp);
        System.out.println();

        tmp = Matrix.subtract(matrixB, matrix);

        System.out.println(tmp);
        System.out.println();
        //regularizer.get(cachedNodeValueVector.get(cachedNodeValueVector.size()-1), regularizationLambda);
    }
}
