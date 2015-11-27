import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;

import static org.nd4j.linalg.factory.Nd4j.sum;
import static org.nd4j.linalg.ops.transforms.Transforms.*;

/** Loss function calculates the loss and the gradients */
public class LossFunc {
  /**
   * Calculates the L2 regularized logistic/cross entropy loss, and its gradient.
   *
   * @param features the feature vectors
   * @param labels   the label of the feature vector
   * @param weights  the parameter of the function, the first weight is the weight for bias term
   * @param lambda   the regularization parameter
   * @param gradient the gradient of the loss with respect to the the weights
   * @return the loss of the function
   */
  public final static float l2RegLoss(float[] features, float[] labels, float[] weights,
      float lambda, float[] gradient) {
    int batchSize = labels.length;
    INDArray w = Nd4j.create(weights).transposei();
    INDArray X = Nd4j.concat(1, Nd4j.ones(batchSize, 1),
        Nd4j.create(features, new int[] {features.length / batchSize, batchSize}));
    INDArray Y = Nd4j.create(labels);
    INDArray activation = sigmoid(X.mmul(w));
    INDArray noBiasW = w.get(NDArrayIndex.interval(1, w.length()));
    INDArray squaredW = noBiasW.mul(noBiasW);
    INDArray loss =
        sum(Y.neg().muli(log(activation)).subi((Y.rsub(1)).muli(log(activation.rsub(1)))))
            .muli(1.0 / batchSize).addi(sum(squaredW).mul(lambda / 2.0 / batchSize));
    INDArray tmp = activation.sub(Y).transposei().mmul(X).muli(1.0 / batchSize);
    gradient[0] = tmp.getFloat(0);
    INDArray regularizedGrad =
        tmp.get(NDArrayIndex.interval(1, w.length())).add(noBiasW.muli(lambda / batchSize));
    for (int i = 1; i < gradient.length; i++) {
      gradient[i] = regularizedGrad.getFloat(i - 1);
    }
    return loss.getFloat(0);
  }
}
