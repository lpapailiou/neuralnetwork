package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NetUtils {

    /**
     * This method may be used to cleanse the output of previous runs with the neural network.
     *
     * @param nodeList the Double list of results
     * @return the index of the max value
     */
    public static int getMaxindex(List<Double> nodeList) {
        if (nodeList == null || nodeList.size() == 0) {
            throw new IllegalArgumentException("Invalid input list!");
        }
        return nodeList.indexOf(Collections.max(nodeList));
    }

    /**
     * This method may be used to cleanse the output of previous runs with the neural network.
     *
     * @param nodeList the Double list of results
     * @return the standardized list, where all values will be rounded to 0 or 1
     */
    public static List<Double> getStandardizedOutputList(List<Double> nodeList) {
        if (nodeList == null || nodeList.size() == 0) {
            throw new IllegalArgumentException("Invalid input list!");
        }
        List<Double> sList = new ArrayList<>();
        for (Double node : nodeList) {
            sList.add((node < 0.5) ? 0.0 : 1.0);
        }
        return sList;
    }

}
