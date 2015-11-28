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

  public StochasticGradientDescent(DenseLossFunction lossFunction, DataSet dataset,
      float learningRate, float lambda, int batchSize) {
    this.dataSets =
        dataset.batchBy(batchSize).stream().map(DataSet::merge).collect(Collectors.toList());
    this.learningRate = learningRate;
    this.lossFunction = lossFunction;
    this.lambda = lambda;
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
