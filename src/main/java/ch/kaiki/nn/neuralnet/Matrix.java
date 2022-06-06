package ch.kaiki.nn.neuralnet;


import ch.kaiki.nn.util.Initializer;
import ch.kaiki.nn.util.Rectifier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a helper class to build the layers of the neural network.
 */
public class Matrix implements Serializable {

    private static final Logger LOG = Logger.getLogger("Matrix logger");
    private static final long serialVersionUID = 2L;
    private double[][] data;
    private int rows;
    private int cols;

    /**
     * The constructor to create a randomized matrix for given type.
     *
     * @param rows the row count of the matrix.
     * @param cols the column count of the matrix.
     */
    Matrix(int rows, int cols) {
        data = new double[rows][cols];
        this.rows = rows;
        this.cols = cols;
    }

    /**
     * Constructor used for testing
     *
     * @param input the input 2d array to be converted to a matrix.
     */
    Matrix(double[][] input) {
        data = input;
        rows = input.length;
        cols = input[0].length;
    }

    static Matrix subtract(Matrix a, Matrix b) {
        if (a.cols != b.cols) {
            throw new IllegalArgumentException("wrong input matrix dimensions! " + a.getType() + " vs. " + b.getType());
        }
        Matrix tmp = a.copy();
        for (int i = 0; i < tmp.rows; i++) {
            for (int j = 0; j < tmp.cols; j++) {
                tmp.data[i][j] -= b.data[i][j];
            }
        }
        return tmp;
    }

    static Matrix merge(Matrix... matrices) {
        if (matrices.length < 2) {
            throw new IllegalArgumentException("Matrix count of " + matrices.length + " cannot be merged!");
        }
        Matrix tmp = matrices[0].copy();
        for (int i = 0; i < tmp.rows; i++) {
            for (int j = 0; j < tmp.cols; j++) {
                double value = 0;
                for (Matrix m : matrices) {
                    value += m.data[i][j];
                }
                tmp.data[i][j] = value / matrices.length;
            }
        }
        return tmp;
    }

    static Matrix crossover(int crossoverSliceCount, Matrix... matrices) {
        if (matrices.length < 2) {
            throw new IllegalArgumentException("Matrix count of " + matrices.length + " cannot be crossed over!");
        }
        double[][][] slices = new double[matrices.length][][];
        int[] sliceIndices = new int[]{0};
        Random random = new Random();

        // calculate random indexes for slice markers
        if (crossoverSliceCount > 1) {
            sliceIndices = new int[crossoverSliceCount-1];
            for (int i = 0; i < sliceIndices.length; i++) {
                sliceIndices[i] = random.nextInt(matrices[0].getRows() * matrices[0].getCols());
            }
        }

        Arrays.sort(sliceIndices);

        // collect according slices from all matrices
        for (int i = 0; i < matrices.length; i++) {
            slices[i] = Matrix.slice(matrices[i], sliceIndices);
        }

        double[][] selectedSlices = new double[sliceIndices.length+1][];

        // select random slices
        for (int i = 0; i <= sliceIndices.length; i++) {
            selectedSlices[i] = slices[random.nextInt(matrices.length)][i];
        }

        // flatten slices
        double[] flattenedSlices = Matrix.merge(selectedSlices);

        // return reconstructed matrix
        return Matrix.fromArray(flattenedSlices, matrices[0].getRows(), matrices[0].getCols());
    }

    static Matrix apply(Matrix a, Function<Double, Double> function) {
        Matrix m = a.copy();
        for (int i = 0; i < m.rows; i++) {
            for (int j = 0; j < m.cols; j++) {
                m.data[i][j] = function.apply(m.data[i][j]);
            }
        }
        return m;
    }

    static Matrix apply(Matrix a, Matrix b, BiFunction<Double, Double, Double> function) {
        Matrix m = a.copy();
        for (int i = 0; i < m.rows; i++) {
            for (int j = 0; j < m.cols; j++) {
                m.data[i][j] = function.apply(m.data[i][j], b.data[i][j]);
            }
        }
        return m;
    }

    static double dotProduct(Matrix a, Matrix b) {
        double sum = 0;
        if (a.cols != b.cols || a.rows != b.rows) {
            throw new IllegalArgumentException("wrong input matrix dimensions!");
        }
        for (int i = 0; i < a.rows; i++) {
            for (int j = 0; j < a.cols; j++) {
                sum += a.data[i][j] * b.data[i][j];
            }
        }
        return sum;
    }

    static Matrix multiply(Matrix a, Matrix b) {
        if (a.cols != b.rows) {
            throw new IllegalArgumentException("wrong input matrix dimensions for multiplication! " + a.getType() + " " + b.getType());
        }
        Matrix tmp = new Matrix(a.rows, b.cols);
        for (int i = 0; i < tmp.rows; i++) {
            for (int j = 0; j < tmp.cols; j++) {
                double sum = 0;
                for (int k = 0; k < a.cols; k++) {
                    double sideA = a.data[i][k];
                    double sideB = b.data[k][j];
                    if ((Double.isInfinite(sideA) && sideB == 0) || (Double.isInfinite(sideB) && sideA == 0)) {
                        sum = 0;
                        log("Multiplication evaluated to Infinity! Value converted to zero, as one of the operands was zero.");
                    } else {
                        sum += sideA * sideB;
                    }
                    if (Double.isInfinite(sum)) {
                        sum = sum < 0 ? Double.MIN_VALUE : Double.MAX_VALUE;
                        log("Multiplication of " + sideA + " and " + sideB + " evaluated to Infinity! Value converted to " + sum);
                    } else if (Double.isNaN(sum)) {
                        log("Multiply operation evaluated to NaN!");
                        throw new ArithmeticException("Multiply operation evaluated to NaN!");
                    }
                }
                tmp.data[i][j] = sum;
            }
        }
        return tmp;
    }

    static double crossEntropy(Matrix actual, Matrix target) {
        Matrix z = new Matrix(actual.rows, actual.cols);
        for (int i = 0; i < actual.rows; i++) {
            for (int j = 0; j < actual.cols; j++) {
                z.data[i][j] = (target.data[i][j] * Math.log(actual.data[i][j])) + ((1 - target.data[i][j]) * Math.log(1 - actual.data[i][j]));
            }
        }
        return -z.sum();
    }

    static Matrix crossEntropyGradient(Matrix actual, Matrix target) {
        Matrix z = new Matrix(actual.rows, actual.cols);
        for (int i = 0; i < actual.rows; i++) {
            for (int j = 0; j < actual.cols; j++) {
                z.data[i][j] = (actual.data[i][j] - target.data[i][j]) / ((1 - actual.data[i][j]) * (actual.data[i][j]));
            }
        }
        return z;
    }

    static Matrix divide(Matrix m, int divisor) {
        Matrix z = new Matrix(m.rows, m.cols);
        for (int i = 0; i < m.rows; i++) {
            for (int j = 0; j < m.cols; j++) {
                z.data[i][j] = (m.data[i][j] / divisor);
            }
        }
        return z;
    }

    static Matrix transpose(Matrix m) {
        Matrix tmp = new Matrix(m.cols, m.rows);
        for (int i = 0; i < m.rows; i++) {
            for (int j = 0; j < m.cols; j++) {
                tmp.data[j][i] = m.data[i][j];
            }
        }
        return tmp;
    }

    static Matrix fromArray(double[] arr) {
        Matrix tmp = new Matrix(arr.length, 1);
        for (int i = 0; i < arr.length; i++) {
            tmp.data[i][0] = arr[i];
        }
        return tmp;
    }

    static Matrix fromArray(double[] arr, int rows, int cols) {
        double[][] m = new double[rows][cols];
        int index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                m[i][j] = arr[index];
                index++;
            }
        }
        return new Matrix(m);
    }

    static Matrix fromList(List<Double> list) {
        Matrix tmp = new Matrix(list.size(), 1);
        for (int i = 0; i < list.size(); i++) {
            tmp.data[i][0] = list.get(i);
        }
        return tmp;
    }

    static List<Double> asList(Matrix m) {
        List<Double> tmp = new ArrayList<>();
        for (int i = 0; i < m.rows; i++) {
            for (int j = 0; j < m.cols; j++) {
                tmp.add(m.data[i][j]);
            }
        }
        return tmp;
    }

    static double[] asArray(Matrix m) {
        int index = 0;
        double[] tmp = new double[m.rows * m.cols];
        for (int i = 0; i < m.rows; i++) {
            for (int j = 0; j < m.cols; j++) {
                tmp[index] = m.data[i][j];
                index++;
            }
        }
        return tmp;
    }

    static double[][] slice(Matrix m, int... sliceIndex) {
        double[] inMatrix = asArray(m);
        double[][] outMatrix = new double[sliceIndex.length+1][];

        int startIndex = 0;
        int endIndex;

        for (int i = 0; i <= sliceIndex.length; i++) {
            endIndex = i == sliceIndex.length ? inMatrix.length : sliceIndex[i];
            double[] slice = Arrays.copyOfRange(inMatrix, startIndex, endIndex);
            outMatrix[i] = slice;
            startIndex = endIndex;
        }
        return outMatrix;
    }

    static double[] merge(double[]... slices) {
        int length = 0;
        for (double[] slice : slices) {
            length += slice.length;
        }
        double[] out = new double[length];
        int index = 0;
        for (int i = 0; i < slices.length; i++) {
            for (int j = 0; j < slices[i].length; j++) {
                out[index] = slices[i][j];
                index++;
            }
        }
        return out;
    }

    void add(Matrix m) {
        if (cols != m.cols || rows != m.rows) {
            throw new IllegalArgumentException("wrong input matrix dimensions for addition!");
        }
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double sideA = data[i][j];
                double sideB = m.data[i][j];
                double value = sideA + sideB;
                if (Double.isInfinite(value)) {
                    value = value < 0 ? Double.MIN_VALUE : Double.MAX_VALUE;
                    log("Addition operation evaluated to Infinity! Value converted to " + value);
                } else if (Double.isNaN(value)) {
                    log("Bias addition evaluated to NAN!");
                    throw new ArithmeticException("Addition operation evaluated to NaN");
                }
                data[i][j] = value;
            }
        }
    }

    void addBias(Matrix m) {
        if (cols != m.cols) {
            throw new IllegalArgumentException("wrong input matrix dimensions! expected " + cols + " columns instead of " + m.cols + " columns.");
        }
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double value = data[i][j] + m.data[i][j];
                if (Double.isInfinite(value)) {
                    value = value < 0 ? Double.MIN_VALUE : Double.MAX_VALUE;
                    log("Bias addition evaluated to Infinity! Value converted to " + value);
                } else if (Double.isNaN(value)) {
                    log("Bias addition evaluated to NAN!");
                    throw new ArithmeticException("Bias addition evaluated to NaN");
                }
                data[i][j] = value;
            }
        }
    }

    void subtract(Matrix m) {
        if (cols != m.cols || rows != m.rows) {
            throw new IllegalArgumentException("wrong input matrix dimensions for addition!");
        }
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double sideA = data[i][j];
                double sideB = m.data[i][j];
                double value = sideA - sideB;
                if (Double.isInfinite(value)) {
                    value = value < 0 ? Double.MIN_VALUE : Double.MAX_VALUE;
                    log("Bias addition evaluated to Infinity! Value converted to " + value);
                } else if (Double.isNaN(value)) {
                    log("Bias addition evaluated to NAN!");
                    throw new ArithmeticException("Addition operation evaluated to NaN");
                }
                data[i][j] = value;
            }
        }
    }

    void subtractBias(Matrix m) {
        if (cols != m.cols) {
            throw new IllegalArgumentException("wrong input matrix dimensions!");
        }
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double value = data[i][j] - m.data[0][j];
                if (Double.isInfinite(value)) {
                    value = value < 0 ? Double.MIN_VALUE : Double.MAX_VALUE;
                    log("Bias addition evaluated to Infinity! Value converted to " + value);
                } else if (Double.isNaN(value)) {
                    log("Bias addition evaluated to NAN!");
                    throw new ArithmeticException("Bias addition evaluated to NaN");
                }
                data[i][j] = value;
            }
        }
    }

    void multiply(double scalar) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                data[i][j] *= scalar;
            }
        }
    }

    void divide(double scalar) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                data[i][j] /= scalar;
            }
        }
    }

    void multiply(Matrix m) {
        if (cols != m.cols || rows != m.rows) {
            throw new IllegalArgumentException("wrong input matrix dimensions!");
        }
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                data[i][j] *= m.data[i][j];
            }
        }
    }

    void activate(Rectifier rectifier) {
        boolean softmax = rectifier == Rectifier.SOFTMAX;
        double expSum = 0;
        if (softmax) {
            expSum = expSum();
        }
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double activation;
                if (softmax) {
                    activation = rectifier.activate(data[i][j], expSum);
                } else {
                    activation = rectifier.activate(data[i][j]);
                }
                if (Double.isNaN(activation)) {
                    log("Activation operation evaluated to NaN!");
                    throw new ArithmeticException("Activation operation evaluated to NaN!");
                } else if (Double.isInfinite(activation)) {
                    log("Activation operation evaluated to Infinity!");
                    throw new ArithmeticException("Activation operation evaluated to Infinity!");
                }
                data[i][j] = activation;
            }
        }
    }

    Matrix derive(Rectifier rectifier) {
        Matrix tmp = new Matrix(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double derivation = rectifier.derive(data[i][j]);
                if (Double.isNaN(derivation)) {
                    log("Derivation operation evaluated to NaN!");
                    throw new ArithmeticException("Derivation operation evaluated to NaN!");
                } else if (Double.isInfinite(derivation)) {
                    log("Derivation operation evaluated to Infinity!");
                    throw new ArithmeticException("Derivation operation evaluated to Infinity!");
                }
                tmp.data[i][j] = derivation;
            }
        }
        return tmp;
    }

    String getType() {
        return "(" + rows + ", " + cols + ")";
    }

    int getRows() {
        return rows;
    }

    int getCols() {
        return cols;
    }

    void initialize(Initializer initializer, int fanIn, int fanOut, boolean isBias) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                data[i][j] = initializer.getValue(fanIn, fanOut, isBias);
            }
        }
    }

    void randomize(double factor, double mutationRate) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (Math.random() < mutationRate) {
                    double value = data[i][j] + (Math.random() * 2 - 1) * factor;
                    data[i][j] = value;
                }
            }
        }
    }

    Matrix dropout(double factor) {
        Matrix tmp = this.copy();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (Math.random() < factor) {
                    tmp.data[i][j] = 0;
                } else {
                    continue;
                }
            }
        }
        return tmp;
    }

    double[][] getData() {
        return data.clone();
    }

    double sum() {
        double sum = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                sum += data[i][j];
            }
        }
        return sum;
    }

    double expSum() {
        double sum = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                sum += Math.exp(data[i][j]);
            }
        }
        return sum;
    }

    void print() {
        System.out.println(this.toString());
    }

    Matrix copy() {
        Matrix m = new Matrix(rows, cols);
        for (int i = 0; i < m.rows; i++) {
            if (m.cols >= 0) System.arraycopy(this.data[i], 0, m.data[i], 0, m.cols);
        }
        return m;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < rows; i++) {
            sb.append("[");
            for (int j = 0; j < cols; j++) {
                sb.append(data[i][j]);
                sb.append(", ");
            }
            sb.replace(sb.length() - 2, sb.length(), "");
            sb.append("],\n");
        }
        sb.replace(sb.length() - 2, sb.length(), "");
        sb.append("]");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Integer.parseInt(rows + "" + cols);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Matrix)) {
            return false;
        }
        return this.toString().equals(o.toString());
    }

    private static void log(String message) {
        LOG.log(Level.INFO, message);
    }
}
