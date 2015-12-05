import org.junit.Before;
import org.junit.Test;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import static org.junit.Assert.assertEquals;

public class StochasticGradientDescentTest {

  // When change constant, please make sure they sure they will yield the correct result.
  // eg. changing the TEST_FEATURES will result in different FIRST_LOSS, SECOND_LOSS and
  // FIRST_GRAD.
  private final static float FLOAT_TOLERANCE = 1e-2f;
  private final static float[] TEST_FEATURES = new float[] {1, -1, 0, 1};
  private final static float[] TEST_LABELS = new float[] {1, 0};
  private final static float[] TEST_WEIGHTS = new float[] {0.1f, 0.2f, 0.3f};
  private final static float FIRST_LOSS = 0.6996f;
  private static final float SECOND_LOSS = 0.919515f;
  private static final float FIRST_GRAD = -0.05f;
  private static final float LEARNING_RATE = 0.1f;
  private static final float LAMBDA = 0.1f;
  private static final int BATCH_SIZE = 1;
  private static final float[] HYPER_PARAMS = new float[] {LEARNING_RATE, LAMBDA, BATCH_SIZE};

  private DenseLossFunction testLoss;
  private DataSet dataset;
  StochasticGradientDescent sgd;

  @Before public void setUp() throws Exception {
    dataset = new DataSet(Nd4j.create(TEST_FEATURES,
        new int[] {TEST_LABELS.length, TEST_FEATURES.length / TEST_LABELS.length}),
        Nd4j.create(TEST_LABELS, new int[] {TEST_LABELS.length, 1}));
    testLoss = new L2RegLogisticDenseLoss();
    sgd = new StochasticGradientDescent(new L2RegLogisticDenseLoss(), dataset, HYPER_PARAMS);
  }

  @Test public void testGetUpdateLoss() {
    float[] weights = TEST_WEIGHTS;
    LossGrad lossGrad = sgd.getUpdate(weights);
    assertEquals(FIRST_LOSS, lossGrad.loss, FLOAT_TOLERANCE);
    lossGrad = sgd.getUpdate(weights);
    assertEquals(SECOND_LOSS, lossGrad.loss, FLOAT_TOLERANCE);
  }

  @Test public void testGetUpdateGrad() {
    float[] weights = TEST_WEIGHTS;
    LossGrad lossGrad = sgd.getUpdate(weights);
    assertEquals(FIRST_GRAD, lossGrad.gradient.getFloat(0), FLOAT_TOLERANCE);
  }
}
