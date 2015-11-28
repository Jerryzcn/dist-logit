import org.nd4j.linalg.api.ndarray.INDArray;

/** Loss, gradient pair */
public class LossGrad {
  public final float loss;
  // Note this is mutable, since INDArray provides in place operation. We do not want to new a
  // object every time we do multiplication, since it is expensive.
  public final INDArray gradient;

  public LossGrad(float loss, INDArray gradient) {
    this.loss = loss;
    this.gradient = gradient;
  }

  @Override public String toString() {
    return "loss: " + loss + " gradient: " + gradient;
  }
}
