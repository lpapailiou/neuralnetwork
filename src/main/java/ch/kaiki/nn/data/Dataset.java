package ch.kaiki.nn.data;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Dataset implements Serializable {

    private static final long serialVersionUID = 2L;
    private DatasetType type;
    private static final String COMMA_DELIMITER = ",";
    private static final String SEMICOLON_DELIMITER = ";";

    private String[] featureLabels = new String[0];
    List<double[]> _X = new ArrayList<>();
    List<double[]> _y = new ArrayList<>();
    private double[][] X;
    private double[][] y;
    private double[][] X_validation;
    private double[][] y_validation;
    private double[][] X_test;

    public Dataset(DatasetType type) {
        this.type = type;
        switch (type) {
            case XOR:
                loadXOrData();
                break;
            case CUSTOM:
                loadCustomData();
                break;
            case IRIS:
                loadIrisData();
                break;
            case MNIST:
                loadMnistData();
                break;
            default:
                break;

        }
    }

    private void loadXOrData() {
        URL inputStream = this.getClass().getClassLoader().getResource("samples/xor/xor.csv");
        try (BufferedReader br = new BufferedReader(new FileReader(new File(inputStream.toURI())))) {

            String line;
            boolean skip = true;
            while ((line = br.readLine()) != null) {
                if (skip) {
                    skip = false;
                    continue;
                }
                String[] values = line.split(SEMICOLON_DELIMITER);
                _X.add(convertStringArrayToDoubleArray(values[0].split(COMMA_DELIMITER)));
                _y.add(convertStringArrayToDoubleArray(values[1].split(COMMA_DELIMITER)));
            }

            shuffle(true);

            split(1);

            featureLabels = new String[] {"true / false"};

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void loadCustomData() {
        URL inputStream = this.getClass().getClassLoader().getResource("samples/custom/custom.csv");
        try (BufferedReader br = new BufferedReader(new FileReader(new File(inputStream.toURI())))) {

            String line;
            boolean skip = true;
            while ((line = br.readLine()) != null) {
                if (skip) {
                    skip = false;
                    continue;
                }
                String[] values = line.split(SEMICOLON_DELIMITER);
                _X.add(convertStringArrayToDoubleArray(values[0].split(COMMA_DELIMITER)));
                _y.add(convertStringArrayToDoubleArray(values[1].split(COMMA_DELIMITER)));
            }

            shuffle(false);

            split(1);

            featureLabels = new String[] {"0","1","2","3"};

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void loadIrisData() {
        URL inputStream = this.getClass().getClassLoader().getResource("samples/iris/iris.csv");
        try (BufferedReader br = new BufferedReader(new FileReader(new File(inputStream.toURI())))) {
            featureLabels = new String[] {"Setosa","Versicolor","Virginica"};
            String line;
            boolean skip = true;
            while ((line = br.readLine()) != null) {
                if (skip) {
                    skip = false;
                    continue;
                }
                String[] values = line.split(COMMA_DELIMITER);
                String[] strArr = new String[4];
                for (int i = 0; i < 4; i++) {
                    strArr[i] = values[i + 1];
                }
                _X.add(convertStringArrayToDoubleArray(strArr));
                for (int i = 0; i < featureLabels.length; i++) {
                    if (featureLabels[i].equals(values[4])) {
                        _y.add(new double[]{i});
                    }
                }

            }

            shuffle(true);

            split(1);


        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void loadMnistData() {
        URL inputStreamTrain = this.getClass().getClassLoader().getResource("samples/mnist/mnist_train.csv");
        URL inputStreamTest = this.getClass().getClassLoader().getResource("samples/mnist/mnist_test.csv");
        try (BufferedReader brTrain = new BufferedReader(new FileReader(new File(inputStreamTrain.toURI())));
             BufferedReader brTest = new BufferedReader(new FileReader(new File(inputStreamTest.toURI())));) {

            String line;
            boolean skip = true;
            while ((line = brTrain.readLine()) != null) {
                if (skip) {
                    skip = false;
                    continue;
                }
                String[] values = line.split(COMMA_DELIMITER);
                String[] strArr = new String[28*28];
                for (int i = 0; i < 28*28; i++) {
                    strArr[i] = values[i + 1];
                }
                _X.add(convertStringArrayToDoubleArray(strArr));
                _y.add(toOneHot(values[0], 10));
            }

            shuffle(false);

            split(1);
            featureLabels = new String[] {"0","1","2","3","4","5","6","7","8","9"};

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void split(double split) {
        int splitIter = (int) (this._X.size() * split);
        double[][] x = new double[splitIter][];
        double[][] y = new double[splitIter][];
        for (int i = 0; i < _y.size(); i++) {
            x[i] = _X.get(i);
            y[i] = _y.get(i);
        }
        this.X = x;
        this.y = y;

        int splitIterTest = _X.size() - splitIter;
        double[][] x_test = new double[splitIterTest][];
        double[][] y_test = new double[splitIterTest][];
        for (int i = 0; i < splitIterTest; i++) {
            x_test[i] = _X.get(splitIter+i);
            y_test[i] = _y.get(splitIter+i);
        }
        this.X_validation = x_test;
        this.y_validation = y_test;
    }

    private void shuffle(boolean shuffle) {
        if (!shuffle || true) {
            return;
        }
        Random seed = new Random(System.nanoTime());
        Collections.shuffle(_X, seed);
        Collections.shuffle(_y, seed);
    }

    private double[] toOneHot(String digit, int length) {
        int index = Integer.parseInt(digit);
        double[] result = new double[length];
        result[index] = 1;
        return result;
    }

    private double[] convertStringArrayToDoubleArray(String[] str) {
        double[] d = new double[str.length];
        try {
            for (int i = 0; i < str.length; i++) {
                d[i] = Double.parseDouble(str[i]);
            }
        } catch (Exception e) {

        }
        return d;
    }

    public String[] getFeatureLabels() {
        return featureLabels;
    }

    public double[][] getX() {
        return X;
    }

    public double[][] getY() {
        return y;
    }

    public double[][] getXValidation() {
        return X_validation;
    }

    public double[][] getYValidation() {
        return y_validation;
    }

    public double[][] getXTest() {
        return X_test;
    }
}
