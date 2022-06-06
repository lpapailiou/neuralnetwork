package ch.kaiki.nn.neuralnet;

import org.junit.Test;

import java.util.Arrays;

public class SliceTest {

    @Test
    public void sliceTest() {
        //double[][] da = new double[][]{{0, 1}, {2, 3}, {4, 5}};
        double[][] da = new double[][]{{0, 0}, {0, 0}, {0, 0}};
        double[][] db = new double[][]{{1, 1}, {1, 1}, {1, 1}};
        Matrix a = new Matrix(da);
        Matrix b = new Matrix(db);
        Matrix c = Matrix.crossover(3, a, b);
        System.out.println(c);

        /*
        double[][] slices = Matrix.slice(a, 1,1,2);
        double[] flatSlices = Matrix.merge(slices);
        Matrix x = Matrix.fromArray(flatSlices, a.getRows(), a.getCols());

        System.out.println(Arrays.deepToString(slices));
        System.out.println(Arrays.toString(flatSlices));
        System.out.println(x);*/
    }
}
