package ch.kaiki.nn.data;

import ch.kaiki.nn.neuralnet.NeuralNetwork;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NNSerializer {

    private static final Logger LOG = Logger.getLogger("NNSerializer logger");
    private static final String fileType = ".ser";

    public static void serialize(Object obj, String path, boolean addTimeStamp) {

        long timeStamp = System.currentTimeMillis();

        String filePath = path + "\\" + obj.getClass().getSimpleName();
        if (addTimeStamp) {
            filePath += "_" + timeStamp;
        }
        filePath += fileType;

        try (   FileOutputStream file = new FileOutputStream(filePath);
                ObjectOutputStream out = new ObjectOutputStream(file);
                ) {

            out.writeObject(obj);
            LOG.log(Level.INFO, "Saved serialized object successfully to " + filePath + ".");

        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error during serialization occurred!", e);
        }
    }

    public static void serializeToTempDirectory(Object obj) {
        try {
            Path tempFile = Files.createTempFile(obj.getClass().getSimpleName() + "_", fileType);
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(tempFile.toFile()));
            out.writeObject(obj);
            LOG.log(Level.INFO, "Saved serialized object successfully to " + tempFile + ".");
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error during serialization occurred!", e);
        }
    }

    public static NeuralNetwork deserializeNeuralNetwork(String path) {
        NeuralNetwork neuralNetwork = null;

        try (
        FileInputStream file = new FileInputStream(path);
        ObjectInputStream in = new ObjectInputStream(file);
        ) {

        neuralNetwork = (NeuralNetwork) in.readObject();

        in.close();
        file.close();

        LOG.log(Level.INFO, "Deserialized NeuralNetwork successfully.");

        } catch (IOException | ClassNotFoundException e) {
            LOG.log(Level.WARNING, "Error during deserialization occurred!", e);
        }

        return neuralNetwork;
    }

    public static Dataset deserializeDataset(String path) {
        Dataset dataset = null;

        try (
                FileInputStream file = new FileInputStream(path);
                ObjectInputStream in = new ObjectInputStream(file);
        ) {

            dataset = (Dataset) in.readObject();

            in.close();
            file.close();

            LOG.log(Level.INFO, "Deserialized Dataset successfully.");

        } catch (IOException | ClassNotFoundException e) {
            LOG.log(Level.WARNING, "Error during deserialization occurred!", e);
        }

        return dataset;
    }

}
