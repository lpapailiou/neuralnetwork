package neuralnet;

import util.Rectifier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a helper class to build the layers of the neural network.
 */
public class Matrix implements Serializable {

    private static final long serialVersionUID = 2L;
    private double[][] data;
    private int rows;
    private int cols;

    /**
     * The constructor to create a randomized matrix for given type.
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
     * @param input the input 2d array to be converted to a matrix.
     */
    Matrix(double[][] input) {
        data = input;
        rows = input.length;
        cols = input[0].length;
    }

    void add(Matrix m, boolean normalize) {
        if (cols != m.cols || rows != m.rows) {
            throw new IllegalArgumentException("wrong input matrix dimensions for addition!");
        }
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double sideA = data[i][j];
                double sideB = m.data[i][j];
                double value = sideA + sideB;
                if (normalize) {
                    if (value < -1) {
                        value = -1;
                    } else if (value > 1) {
                        value = 1;
                    }
                }
                if (Double.isInfinite(value)) {
                    value = value < 0 ? Double.MIN_VALUE : Double.MAX_VALUE;
                } else if (Double.isNaN(value)) {
                    throw new ArithmeticException("Addition operation evaluated to NaN");
                }
                data[i][j] = value;
            }
        }
    }

    void addBias(Matrix m, boolean normalize) {
        if (cols != m.cols) {
            throw new IllegalArgumentException("wrong input matrix dimensions!");
        }
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double value = data[i][j] + m.data[0][j];
                if (normalize) {
                    if (value < -1) {
                        value = -1;
                    } else if (value > 1) {
                        value = 1;
                    }
                }
                if (Double.isInfinite(value)) {
                    value = value < 0 ? Double.MIN_VALUE : Double.MAX_VALUE;
                } else if (Double.isNaN(value)) {
                    throw new ArithmeticException("Bias addition evaluated to NaN");
                }
                data[i][j] = value;
            }
        }
    }

    static Matrix subtract(Matrix a, Matrix b) {
        if (a.cols != b.cols) {
            throw new IllegalArgumentException("wrong input matrix dimensions! " + a.getType() + " vs. " + b.getType());
        }
        for (int i = 0; i < a.rows; i++) {
            for (int j = 0; j < a.cols; j++) {
                a.data[i][j] -= b.data[i][j];
            }
        }
        return a;
    }

    static Matrix merge(Matrix a, Matrix b) {
        if (a.rows != b.rows || a.cols != b.cols) {
            throw new IllegalArgumentException("wrong input matrix dimensions! " + a.getType() + " vs. " + b.getType());
        }
        for (int i = 0; i < a.rows; i++) {
            for (int j = 0; j < a.cols; j++) {
                a.data[i][j] = (a.data[i][j] + b.data[i][j]) / 2;
            }
        }
        return a;
    }

    void multiply(double scalar) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                data[i][j] *= scalar;
            }
        }
    }

    void multiplyElementwise(Matrix m) {
        if (cols != m.cols || rows != m.rows) {
            throw new IllegalArgumentException("wrong input matrix dimensions!");
        }
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                data[i][j] *= m.data[i][j];
            }
        }
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
                    } else {
                        sum += sideA * sideB;
                    }
                    if (Double.isInfinite(sum)) {
                        sum = sum < 0 ? Double.MIN_VALUE : Double.MAX_VALUE;
                    } else if (Double.isNaN(sum)) {
                        throw new ArithmeticException("Multiply operation evaluated to NaN!");
                    }
                }
                tmp.data[i][j] = sum;
            }
        }
        return tmp;
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

    void activate(Rectifier rectifier) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double activation = rectifier.activate(data[i][j]);
                if (Double.isNaN(activation)) {
                    throw new ArithmeticException("Activation operation evaluated to NaN!");
                } else if (Double.isInfinite(activation)) {
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
                    throw new ArithmeticException("Derivation operation evaluated to NaN!");
                } else if (Double.isInfinite(derivation)) {
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

    static Matrix fromArray(double[] arr, boolean normalize) {
        Matrix tmp = new Matrix(arr.length, 1);
        for (int i = 0; i < arr.length; i++) {
            tmp.data[i][0] = arr[i];
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

    void randomize(double initialRandomization) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                data[i][j] = (Math.random() * 2 - 1) * initialRandomization;
            }
        }
    }

    void randomize(double factor, double mutationRate, boolean normalize) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (Math.random() < mutationRate) {
                    double value = data[i][j] + (Math.random() * 2 - 1) * factor;
                    if (normalize) {
                        if (value > 1) {
                            value = 1;
                        } else if (value < -1) {
                            value = -1;
                        }
                    }
                    data[i][j] = value;
                }
            }
        }
    }

    double[][] getData() {
        return data.clone();
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
            sb.replace(sb.length()-2, sb.length(), "");
            sb.append("],\n");
        }
        sb.replace(sb.length()-2, sb.length(), "");
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

}
