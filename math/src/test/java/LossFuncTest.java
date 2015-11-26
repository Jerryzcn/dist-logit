import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class LossFuncTest {

  private float[] testFeatures;
  private float[] testLabels;

  @Before public void setUp() throws Exception {
    testFeatures = new float[] {1, -1, 0, 1};
    testLabels = new float[] {1, 0};
  }

  @Test public void testL2Loss() {
    float[] weights = new float[] {0.1f, 0.1f, 0.1f};
    float[] gradients = new float[weights.length];
    LossFunc.l2RegLoss(testFeatures, testLabels, weights, 0.1f, testLabels.length, gradients);
  }
}
