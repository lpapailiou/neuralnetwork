package neuralnet;

import org.junit.Test;
import util.LearningRateDescent;

public class LearningRateDecreaseTest {

    @Test
    public void learningRateDescentTest() {
        LearningRateDescent lrd = LearningRateDescent.SGD;
        double lrinit = 0.8;
        double lr = 0.8;
        for (int i = 0; i < 100; i++) {
            lr = lrd.decrease(lrinit, 0.005, i);
            System.out.println(lr);
        }
    }
}
