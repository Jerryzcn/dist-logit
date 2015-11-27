import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class LossFuncTest {

  private final static float FLOAT_TOLERANCE = 1e-5f;

  private float[] testFeatures;
  private float[] testLabels;

  @Before public void setUp() throws Exception {
    testFeatures = new float[] {1, -1, 0, 1};
    testLabels = new float[] {1, 0};
  }

  @Test public void testL2Loss() {
    float[] weights = new float[] {0.1f, 0.2f, 0.3f};
    float[] gradients = new float[weights.length];
    float loss = LossFunc.l2RegLoss(testFeatures, testLabels, weights, 0.1f, gradients);
    assertEquals(0.809581216, loss, FLOAT_TOLERANCE);
  }

  @Test public void testL2Grad() {
    float[] weights = new float[] {0.1f, 0.2f, 0.3f};
    float[] gradients = new float[weights.length];
    float loss = 0.0f;
    for (int i = 0; i < 800; i++) {
      loss = LossFunc.l2RegLoss(testFeatures, testLabels, weights, 0.1f, gradients);
      for (int j = 0; j < weights.length; j++) {
        weights[j] = weights[j] - 0.3f * gradients[j];
      }
      System.out.println(
          loss + " [" + weights[0] + " " + weights[1] + " " + weights[2] + "] " + gradients[0] + " "
              + gradients[1] + " " + gradients[2]);
    }
    assertEquals(0.20134231, loss, FLOAT_TOLERANCE);
  }
}
