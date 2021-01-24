package geneticalgorithm;

import neuralnet.NeuralNetwork;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import static junit.framework.TestCase.assertEquals;

public class PropertiesTest {

    @Test
    public void propertiesTest() throws IOException {
        new GeneticAlgorithmBatch(new NeuralNetwork(2,2,2), null,1,1);
        /*
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String defaultConfigPath = rootPath + "geneticalgorithm.properties";
        Properties defaultProps = new Properties();
        defaultProps.load(new FileInputStream(defaultConfigPath));
        assertEquals("2", defaultProps.getProperty("selectionReproductionSize"));*/
    }
}
