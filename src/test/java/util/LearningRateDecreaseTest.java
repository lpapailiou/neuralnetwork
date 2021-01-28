package util;

import org.junit.Test;

public class LearningRateDecreaseTest {

    @Test
    public void learningRateDescentTest() {
        descentTest(LearningRateDescent.NONE);
        descentTest(LearningRateDescent.SGD);
    }

    void descentTest(LearningRateDescent lrd) {
        double lrinit = 0.8;
        double lr = 0;
        System.out.println("testing with: " + lrd.getDescription());
        int iter = 250;
        for (int i = 0; i < iter; i++) {
            lr = lrd.decrease(lr, 0.005, i);
        }
        System.out.println("learning rate " + lr + " after " + iter + " iterations.\n");
    }
}
