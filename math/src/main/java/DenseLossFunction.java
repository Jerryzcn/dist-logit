import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;

/** Loss function calculates the loss and the gradients */
public interface DenseLossFunction {
  public LossGrad compute(INDArray X, INDArray Y, float[] weights, float lambda);
}
