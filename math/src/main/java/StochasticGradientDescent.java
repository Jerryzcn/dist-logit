import org.nd4j.linalg.dataset.DataSet;

import java.util.List;
import java.util.stream.Collectors;

/** Mini-batch gradient descent with fixed learning rate */
public class StochasticGradientDescent implements DenseOptimizer {
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
    this.dataSets = dataset.batchBy((int) hyperParams[2]).stream().map(DataSet::merge)
        .collect(Collectors.toList());
    this.learningRate = hyperParams[0];
    this.lossFunction = lossFunction;
    this.lambda = hyperParams[1];
    batchIndex = -1;
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
}
