import org.apache.log4j.Logger;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;

import static org.nd4j.linalg.factory.Nd4j.sum;
import static org.nd4j.linalg.ops.transforms.Transforms.log;
import static org.nd4j.linalg.ops.transforms.Transforms.sigmoid;

public class L2RegLogisticDenseLoss implements DenseLossFunction {

  private static Logger logger = Logger.getLogger(L2RegLogisticDenseLoss.class);

  /**
   * Calculates the L2 regularized logistic/cross entropy loss, and its gradient. The function will
   * append bias term to the feature vectors.
   *
   * @param X       the feature vectors
   * @param Y       the label vector
   * @param weights the parameter of the function, the first weight is the weight for bias term
   * @param lambda  the regularization parameter
   * @return the loss and gradient w.r.t weights of the function
   */
  @Override public LossGrad compute(INDArray X, INDArray Y, float[] weights, float lambda) {
    int batchSize = Y.length();
    INDArray w = Nd4j.create(weights).transposei();
    INDArray activation = sigmoid(X.mmul(w));
    INDArray noBiasW = w.get(NDArrayIndex.interval(1, w.length()));
    INDArray squaredW = noBiasW.mul(noBiasW);
    INDArray loss =
        sum(Y.neg().muli(log(activation)).subi((Y.rsub(1)).muli(log(activation.rsub(1)))))
            .muli(1.0 / batchSize).addi(sum(squaredW).muli(lambda / 2.0 / batchSize));
    INDArray tmp = activation.sub(Y).transposei().mmul(X).muli(1.0 / batchSize);
    INDArray biasGradient = tmp.getScalar(0);
    INDArray regularizedGrad =
        tmp.get(NDArrayIndex.interval(1, w.length())).addi(noBiasW.muli(lambda / batchSize));
    return new LossGrad(loss.getFloat(0), Nd4j.concat(1, biasGradient, regularizedGrad));
  }
}
