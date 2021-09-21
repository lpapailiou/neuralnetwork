package ch.kaiki.nn.data;

import ch.kaiki.nn.neuralnet.NeuralNetwork;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NNSerializer {

    private static final Logger LOG = Logger.getLogger("NNSerializer logger");
    private static final String fileName = "NeuralNetwork";
    private static final String fileType = ".ser";

    public static void serialize(NeuralNetwork neuralNetwork, String path, boolean addTimeStamp) {

        long timeStamp = System.currentTimeMillis();

        String filePath = path + "\\" + fileName;
        if (addTimeStamp) {
            filePath += "_" + timeStamp;
        }
        filePath += fileType;

        try (   FileOutputStream file = new FileOutputStream(filePath);
                ObjectOutputStream out = new ObjectOutputStream(file);
                ) {

            out.writeObject(neuralNetwork);
            LOG.log(Level.INFO, "Saved serialized neural network successfully to " + filePath + ".");

        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error during serialization occurred!", e);
        }
    }

    public static void serializeToTempDirectory(NeuralNetwork neuralNetwork) {
        try {
            Path tempFile = Files.createTempFile(fileName + "_", fileType);
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(tempFile.toFile()));
            out.writeObject(neuralNetwork);
            LOG.log(Level.INFO, "Saved serialized neural network successfully to " + tempFile + ".");
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error during serialization occurred!", e);
        }
    }

    public static NeuralNetwork deserialize(String path) {
        NeuralNetwork neuralNetwork = null;

        try (
        FileInputStream file = new FileInputStream(path);
        ObjectInputStream in = new ObjectInputStream(file);
        ) {

        neuralNetwork = (NeuralNetwork) in.readObject();

        in.close();
        file.close();

        LOG.log(Level.INFO, "Deserialized NeuralNetwork successfully.");

        return neuralNetwork;

        } catch (IOException | ClassNotFoundException e) {
            LOG.log(Level.WARNING, "Error during deserialization occurred!", e);
        }

        return neuralNetwork;
    }

}
