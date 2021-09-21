package ch.kaiki.nn.data;

import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.util.Rectifier;
import org.junit.Test;

import java.io.IOException;

public class SerializerTest {

    @Test
    public void serializeTest() {
        NeuralNetwork neuralNetwork = new NeuralNetwork.Builder(3,2,1).setDefaultRectifier(Rectifier.TANH).build();
        //NNSerializer.serialize(neuralNetwork, "C:\\Users\\Lena Papailiou\\AppData\\Local\\Temp", false);
        NNSerializer.serializeToTempDirectory(neuralNetwork);
    }

    @Test
    public void deserializeTest() {
        NeuralNetwork neuralNetwork = NNSerializer.deserialize("C:\\Users\\Lena Papailiou\\AppData\\Local\\Temp\\NeuralNetwork.ser");
        System.out.println(neuralNetwork);
    }


}
