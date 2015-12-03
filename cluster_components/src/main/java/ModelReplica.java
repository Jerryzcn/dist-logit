import org.nd4j.linalg.dataset.DataSet;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TODO:
 */
public class ModelReplica implements Runnable {
  private static final int NUM_OF_THREADS = 6;
  private final Map<InetAddress, ParamServerSettings> paramServers;
  private boolean isStopped;
  private final DataSet dataset;
  private float[] w;
  private final ExecutorService pool;

  public ModelReplica(Map<InetAddress, ParamServerSettings> paramServers, DataSet dataset) {
    this.dataset = dataset;
    isStopped = true;
    w = new float[dataset.getFeatures().columns()];
    this.paramServers = paramServers;
    this.pool = Executors.newFixedThreadPool(NUM_OF_THREADS);
  }

  @Override public void run() {
    isStopped = true;
    // TODO: connect to parameter servers with new threads using shared local params.
    ParameterPuller[] pullers = new ParameterPuller[paramServers.size()];
    int i = 0;
    for (InetAddress address : paramServers.keySet()) {
      ParamServerSettings settings = paramServers.get(address);
      pullers[i] =
          new ParameterPuller(address, settings.downPort, settings.lowIndex, settings.highIndex);
      i++;
    }
    while (!isStopped()) {
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
