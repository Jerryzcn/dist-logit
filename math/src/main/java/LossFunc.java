import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;

import static org.nd4j.linalg.factory.Nd4j.sum;
import static org.nd4j.linalg.ops.transforms.Transforms.*;

/** Loss function calculates the loss and the gradients */
public class LossFunc {
  public static float l2RegLoss(float[] features, float[] labels, float[] weights, float lambda,
      int batchSize, float[] gradients) {
    INDArray w = Nd4j.create(weights).transposei();
    INDArray X = Nd4j.concat(1, Nd4j.ones(labels.length, 1),
        Nd4j.create(features, new int[] {features.length / labels.length, labels.length}));
    INDArray Y = Nd4j.create(labels);
    INDArray activation = sigmoid(X.mmul(w));
    INDArray loss = sum(Y.negi().muli(log(activation))).muli(1.0 / batchSize)
        .sub((Y.rsub(1)).muli(log(activation.rsubi(1))))
        .addi(sum(w.get(NDArrayIndex.interval(1,features.length)).norm2()).mul(lambda / 2.0 / batchSize));
    return loss.getFloat(0);
  }
}
