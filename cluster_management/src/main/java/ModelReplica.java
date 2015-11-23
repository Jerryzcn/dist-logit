import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.factory.Nd4jBackend;

import java.net.InetAddress;
import java.util.Map;

/**
 * TODO:
 */
public class ModelReplica implements Runnable {
  private boolean isStopped;
  private final DataSet dataset;
  private INDArray w;

  public ModelReplica(Map<InetAddress, Integer> paramServers, DataSet dataset) {
    this.dataset = dataset;
    isStopped = true;
    w = null;
  }

  @Override public void run() {
    isStopped = true;
    // TODO: connect to parameter servers with new threads using shared local params.
    while (!isStopped()) {
      Nd4j.getExecutioner();
      // TODO: download params
      // TODO: upload gradient
    }
  }

  public boolean isStopped() {
    return isStopped;
  }

  public void stop() {

  }
}
