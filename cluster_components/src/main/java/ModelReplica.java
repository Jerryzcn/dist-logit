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
  private final float learningRate;
  private final float lambda;
  private final int batchSize;

  public ModelReplica(Map<InetAddress, ParamServerSettings> paramServers, DataSet dataset,
      float learningRate, float lambda, int batchSize) {
    this.dataset = dataset;
    isStopped = true;
    w = new float[dataset.getFeatures().columns()];
    this.paramServers = paramServers;
    this.pool = Executors.newFixedThreadPool(NUM_OF_THREADS);
    this.learningRate = learningRate;
    this.lambda = lambda;
    this.batchSize = batchSize;
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
    for (ParameterPuller puller : pullers) {
      pool.execute(puller);
    }
    StochasticGradientDescent sgd =
        new StochasticGradientDescent(new L2RegLogisticDenseLoss(), dataset, learningRate, lambda,
            batchSize);
    LossGrad lossGrad;
    while (!isStopped()) {
      // TODO: upload gradient
      synchronized (w) {
        lossGrad = sgd.getUpdate(w);
      }
      for (i = 0; i < paramServers.size(); i++) {
        new GradientPusher(lossGrad.gradient, ); //TODO: finish this!
      }
    }
  }

  public boolean isStopped() {
    return isStopped;
  }

  public void stop() {

  }
}
