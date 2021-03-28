package neuralnet;

import org.junit.Test;
import util.Initializer;
import util.Rectifier;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class MatrixTest {

    @Test
    public void additionTest() {
        double[][] d = new double[][] {{0,1},{2,3},{4,5}};
        double[][] da = new double[][] {{0,2},{4,6},{8,10}};
        Matrix m = new Matrix(d);
        m.print();
        m.add(m, false);
        m.print();
        Matrix ma = new Matrix(da);
        assertEquals(m, ma);
    }

    @Test
    public void biasTest() {
        double[][] d = new double[][] {{0,1},{1,1},{1,1}};
        Matrix a = new Matrix(d);
        double[][] db = new double[][] {{1,2}};
        Matrix b = new Matrix(db);
        double[][] de = new double[][] {{1,3},{2,3},{2,3}};
        Matrix e = new Matrix(de);
        a.addBias(b, false);
        a.print();
        assertEquals(a, e);
    }

    @Test
    public void subtractionTest() {
        double[][] d = new double[][] {{0,1},{2,3},{4,5}};
        double[][] da = new double[][] {{0,0},{0,0},{0,0}};
        Matrix m = new Matrix(d);
        m.print();
        m = Matrix.subtract(m, m);
        m.print();
        Matrix ma = new Matrix(da);
        assertEquals(m, ma);
    }

    @Test
    public void mergeTest() {
        double[][] d = new double[][] {{0,1},{2,3},{4,5}};
        double[][] dm = new double[][] {{1,0},{0,5},{6,0}};
        double[][] de = new double[][] {{-1,1},{2,-2},{-2,5}};
        Matrix m = new Matrix(d);
        Matrix mm = new Matrix(dm);
        m.print();
        m = m.subtract(m, mm);
        m.print();
        Matrix ma = new Matrix(de);
        assertEquals(m, ma);
    }

    @Test
    public void multiplicationScalarTest() {
        double[][] d = new double[][] {{2,1},{1,0},{2,0}};
        double scaler = 2.5;
        double[][] de = new double[][] {{5,2.5},{2.5,0},{5,0}};
        Matrix a = new Matrix(d);
        a.print();
        a.multiply(scaler);
        a.print();
        Matrix b = new Matrix(de);
        assertEquals(a, b);
    }

    @Test
    public void multiplicationElementwiseTest() {
        double[][] d = new double[][] {{2,1},{1,0},{2,0}};
        double[][] dm = new double[][] {{2,0},{1,0},{0.5,0}};
        double[][] de = new double[][] {{4,0},{1,0},{1,0}};
        Matrix a = new Matrix(d);
        Matrix b = new Matrix(dm);
        Matrix e = new Matrix(de);
        a.print();
        a.multiplyElementwise(b);
        a.print();
        assertEquals(a, e);
    }

    @Test
    public void multiplicationTest() {
        double[][] d = new double[][] {{2,1},{1,0},{2,0}};
        double[][] c = new double[][] {{1},{2}};
        double[][] mul = new double[][] {{4},{1},{2}};
        Matrix a = new Matrix(d);
        a.print();
        Matrix b = new Matrix(c);
        b.print();
        Matrix m = Matrix.multiply(a, b);
        m.print();
        Matrix mt = new Matrix(mul);
        assertEquals(m, mt);
    }

    @Test
    public void transponseTest() {
        double[][] d = new double[][] {{2,1},{3,5},{7,4}};
        double[][] dt = new double[][] {{2,3,7},{1,5,4}};
        Matrix a = new Matrix(d);
        a.print();
        Matrix b = Matrix.transpose(a);
        b.print();
        Matrix c = new Matrix(dt);
        assertEquals(b, c);
    }

    @Test
    public void sigmoidTest() {
        double[][] d = new double[][] {{0,-1},{1,2}};
        Matrix a = new Matrix(d);
        a.activate(Rectifier.SIGMOID);
        a.print();
        List<Double> list = Matrix.asList(a);
        assertEquals(0.5, Double.parseDouble(list.get(0)+""), 0.001);
        assertEquals(0.25, Double.parseDouble(list.get(1)+""), 0.05);
        assertEquals(0.75, Double.parseDouble(list.get(2)+""), 0.2);
        assertEquals(0.85, Double.parseDouble(list.get(3)+""), 0.1);
    }

    @Test
    public void diSigmoidTest() {
        double[][] d = new double[][] {{0.25,5},{1,2}};
        Matrix a = new Matrix(d);
        a = a.derive(Rectifier.SIGMOID);
        a.print();
        List<Double> list = Matrix.asList(a);
        assertEquals(0.2, Double.parseDouble(list.get(0)+""), 0.1);
        assertEquals(-20, Double.parseDouble(list.get(1)+""), 0.05);
        assertEquals(0, Double.parseDouble(list.get(2)+""), 0.05);
        assertEquals(-2, Double.parseDouble(list.get(3)+""), 0.05);
    }

    @Test
    public void utilitiesTest() {
        double[] d = new double[] {0.25,5};
        Matrix m = Matrix.fromArray(d, false);
        List<Double> newList = Matrix.asList(m);
        List<Double> oldList = new ArrayList<>();
        for (double dbl : d) {
            oldList.add(dbl);
        }
        assertEquals(oldList, newList);
    }

    @Test
    public void randomizationTest() {
        double[][] d = new double[][] {{1,1},{1,1},{1,1}};
        Matrix a = new Matrix(d);
        a.print();
        Matrix b = a.copy();
        Matrix c = a.copy();
        b.print();
        assertEquals(a, b);
        b.randomize(0.9, 0.5, false);
        b.print();
        assertNotEquals(a, b);
        c.initialize(Initializer.RANDOM, 0, 0, 2, true);
        assertNotEquals(a, c);
    }
}
