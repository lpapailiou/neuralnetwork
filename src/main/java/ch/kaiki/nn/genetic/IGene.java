package ch.kaiki.nn.genetic;


import java.util.List;

public interface IGene {

    IGene crossover(List<IGene> genes);

    IGene initialize();

    IGene mutate();

    void decreaseRate();

    List<Double> predict(double[] inputNodes);
}
