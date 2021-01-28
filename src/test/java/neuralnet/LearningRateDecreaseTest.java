package neuralnet;

import org.junit.Test;
import util.LearningRateDescent;

public class LearningRateDecreaseTest {

    @Test
    public void learningRateDescentTest() {
        LearningRateDescent lrd = LearningRateDescent.NONE;
        double lr = 0.8;
        for (int i = 0; i < 30; i++) {
            lr = lrd.decrease(lr, 0.001, i);
            System.out.println(lr);
        }
    }
}
