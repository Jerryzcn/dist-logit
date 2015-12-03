import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.indexing.NDArrayIndex;

import java.lang.reflect.Parameter;
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
  float[] hyperParams;

  public ModelReplica(final Map<InetAddress, ParamServerSettings> paramServers, DataSet dataset,
      float[] hyperParams) {
    this.dataset = dataset;
    isStopped = true;
    w = new float[dataset.getFeatures().columns()];
    this.paramServers = paramServers;
    this.pool = Executors.newFixedThreadPool(NUM_OF_THREADS);
    this.hyperParams = hyperParams;
  }

  @Override public void run() {
    isStopped = true;
    // TODO: connect to parameter servers with new threads using shared local params.
    ParameterPuller[] pullers = new ParameterPuller[paramServers.size()];
    int i = 0;
    for (InetAddress address : paramServers.keySet()) {
      ParamServerSettings settings = paramServers.get(address);
      pullers[i] =
          new ParameterPuller(address, w, settings.downPort, settings.lowIndex, settings.highIndex);
      i++;
    }
    for (ParameterPuller puller : pullers) {
      pool.execute(puller);
    }
    StochasticGradientDescent sgd =
        new StochasticGradientDescent(new L2RegLogisticDenseLoss(), dataset, hyperParams);
    LossGrad lossGrad;
    while (!isStopped()) {
      // TODO: upload gradient
      synchronized (w) {
        lossGrad = sgd.getUpdate(w);
      }
      for (InetAddress address : paramServers.keySet()) {
        ParamServerSettings settings = paramServers.get(address);
        new GradientPusher(
            lossGrad.gradient.get(NDArrayIndex.interval(settings.lowIndex, settings.highIndex)),
            address, settings.upPort);
      }
    }
    for (ParameterPuller puller : pullers) {
      puller.stop();
    }
  }

  public boolean isStopped() {
    return isStopped;
  }

  public void stop() {

  }
}
