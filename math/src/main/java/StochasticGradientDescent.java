import org.apache.log4j.Logger;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Mini-batch gradient descent with fixed learning rate */
public class StochasticGradientDescent implements DenseOptimizer {

  public final static int LEARNING_RATE = 0;
  public final static int REG_CONSTANT = 1;
  public final static int BATCH_SIZE = 2;

  private static Logger logger = Logger.getLogger(StochasticGradientDescent.class);

  private final List<DataSet> dataSets;
  private final float learningRate;
  private final DenseLossFunction lossFunction;
  private final float lambda;
  private int batchIndex;

  /**
   * Provides function to compute a loss and a update that is in the direction of the gradient of
   * the loss w.r.t parameters.
   *
   * @param lossFunction The loss function
   * @param dataset      Training data and labels
   * @param hyperParams  Hyper-parameters: 0: learning rate, 1: regularization constant,
   *                     2: batch size
   */
  public StochasticGradientDescent(DenseLossFunction lossFunction, DataSet dataset,
      float[] hyperParams) {
    this.dataSets = batchBy(dataset, (int) hyperParams[BATCH_SIZE]);
    this.learningRate = hyperParams[LEARNING_RATE];
    this.lossFunction = lossFunction;
    this.lambda = hyperParams[REG_CONSTANT];
    batchIndex = -1;
    logger.info("learning rate: " + hyperParams[0] + " regularization constant: " + hyperParams[1]
        + " batch size: " + hyperParams[2]);
  }

  @Override public LossGrad getUpdate(float[] weights) {
    batchIndex++;
    if (batchIndex >= dataSets.size()) {
      batchIndex = 0;
    }
    LossGrad result = lossFunction.compute(dataSets.get(batchIndex), weights, lambda);
    result.gradient.muli(learningRate);
    return result;
  }

  private List<DataSet> batchBy(DataSet dataset, int num) {
    int numOfExamples = dataset.getLabels().length();
    int numOfBatches = numOfExamples / num;
    INDArray features = dataset.getFeatures();
    INDArray labels = dataset.getLabels();
    List<DataSet> miniBatches = new ArrayList<>(numOfBatches);
    for (int i = 0; i < numOfExamples; i += num) {
      INDArrayIndex indexes = NDArrayIndex.interval(i, i + num < numOfExamples ? i + num : numOfExamples);
      miniBatches.add(new DataSet(features.get(indexes, NDArrayIndex.all()),
          labels.get(indexes, NDArrayIndex.all())));
    }
    return miniBatches;
  }
}
