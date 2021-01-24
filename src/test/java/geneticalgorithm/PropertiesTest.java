package geneticalgorithm;

import neuralnet.NeuralNetwork;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Properties;

import static junit.framework.TestCase.assertEquals;

public class PropertiesTest {

    @Test
    public void propertiesTest() throws IOException, URISyntaxException {
        URL defaultConfigPath2 = getClass().getClassLoader().getResource("geneticalgorithm.properties");
        File file = Paths.get(defaultConfigPath2.toURI()).toFile();
        Properties defaultProps = new Properties();
        defaultProps.load(new FileInputStream(file));
        System.out.println(defaultProps.getProperty("selectionReproductionSize"));

        /*
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String defaultConfigPath = rootPath + "geneticalgorithm.properties";
        Properties defaultProps = new Properties();
        defaultProps.load(new FileInputStream(defaultConfigPath));
        assertEquals("2", defaultProps.getProperty("selectionReproductionSize"));*/
    }
}
