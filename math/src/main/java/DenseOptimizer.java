/**
 * Given the gradient and the loss, compute the update of the parameters to optimize
 * the loss function.
 */
public interface DenseOptimizer {
  public LossGrad getUpdate(float[] weights);
}
