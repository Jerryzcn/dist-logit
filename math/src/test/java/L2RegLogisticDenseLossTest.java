import org.junit.Before;
import org.junit.Test;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import static org.junit.Assert.assertEquals;

public class L2RegLogisticDenseLossTest {

  // When change constant, please make sure they sure they will yield the correct result.
  // eg. changing the TEST_FEATURES will result in different TEST_LOSS and CONVERGED_LOSS
  private final static float FLOAT_TOLERANCE = 1e-5f;
  private final static float[] TEST_FEATURES = new float[] {1, -1, 0, 1};
  private final static float[] TEST_LABELS = new float[] {1, 0};
  private final static float[] TEST_WEIGHTS = new float[] {0.1f, 0.2f, 0.3f};
  private static final int ITERATION = 300;
  private static final float LEARNING_RATE = 0.3f;
  private static final float TEST_LOSS = 0.80633121f;
  private static final float CONVERGED_LOSS = 0.20134231f;
  private static final float LAMBDA = 0.1f;

  private DenseLossFunction testLoss;
  private DataSet dataset;

  @Before public void setUp() throws Exception {
    dataset = new DataSet(Nd4j.create(TEST_FEATURES,
        new int[] {TEST_LABELS.length, TEST_FEATURES.length / TEST_LABELS.length}),
        Nd4j.create(TEST_LABELS, new int[] {TEST_LABELS.length, 1}));
    testLoss = new L2RegLogisticDenseLoss();
  }

  @Test public void testL2Loss() {
    float[] weights = TEST_WEIGHTS;
    LossGrad result = testLoss.compute(dataset, weights, LAMBDA);
    assertEquals(TEST_LOSS, result.loss, FLOAT_TOLERANCE);
  }

  @Test public void testL2Grad() {
    float[] weights = TEST_WEIGHTS.clone();
    LossGrad result = null;
    for (int i = 0; i < ITERATION; i++) {
      result = testLoss.compute(dataset, weights, LAMBDA);
      for (int j = 0; j < weights.length; j++) {
        weights[j] = weights[j] - LEARNING_RATE * result.gradient.getFloat(j);
      }
    }
    assertEquals(CONVERGED_LOSS, result.loss, FLOAT_TOLERANCE);
  }
}
