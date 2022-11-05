package ch.kaiki.nn.genetic;


import java.io.Serializable;
import java.util.List;

public interface IGene extends Serializable {

    IGene crossover(List<IGene> genes);

    IGene initialize();

    IGene mutate();

    void decreaseRate();

    List<Double> predict(double[] inputNodes);
}
