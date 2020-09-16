package neuralnet;

import org.junit.Test;

public class MatrixTest {

    @Test
    public void mutationTest() {
        double[][] d = new double[][] {{1,1},{1,1},{1,1}};
        Matrix a = new Matrix(d);
        a.print();
        a.randomize(0.9);
        a.print();
        a.randomize();
        a.print();
    }


    public void transponseTest() {
        double[][] d = new double[][] {{2,1},{3,5},{7,4}};
        Matrix a = new Matrix(d);
        a.print();
        Matrix b = Matrix.transponse(a);
        b.print();
    }


    public void testMultiply() {
        double[][] d = new double[][] {{2,1},{1,0},{2,0}};
        double[][] c = new double[][] {{1},{2}};
        Matrix a = new Matrix(d);
        a.print();
        Matrix b = new Matrix(c);
        b.print();
        Matrix m = Matrix.multiply(a, b);
        m.print();

    }


    public void testMatrix() {
        double[][] d = new double[][] {{0,1},{2,3},{4,5}};
        Matrix m = new Matrix(d);
        m.print();
        m.add(m);
        m.print();
    }



    public void randomizeTest() {
        Matrix m = new Matrix(2,3);
        m.print();
        m.randomize();
        m.print();
    }
}
