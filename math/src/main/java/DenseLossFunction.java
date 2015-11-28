import org.nd4j.linalg.dataset.DataSet;

/** Loss function calculates the loss and the gradients */
public interface DenseLossFunction {
  public LossGrad compute(DataSet dataset, float[] weights, float lambda);
}
