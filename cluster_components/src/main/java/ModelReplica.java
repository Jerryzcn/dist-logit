import org.apache.log4j.Logger;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.indexing.NDArrayIndex;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Run the optimization on the local copy of the model.
 * Pushes and pulls updates to and from parameter servers.
 */
public class ModelReplica implements Runnable {
  private static final int NUM_OF_THREADS = 6;

  private static Logger logger = Logger.getLogger(ModelReplica.class);

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
    w = new float[dataset.getFeatures().columns() + 1]; // +1 for bias term
    this.paramServers = paramServers;
    this.pool = Executors.newFixedThreadPool(NUM_OF_THREADS);
    this.hyperParams = hyperParams;
  }

  @Override public void run() {
    isStopped = false;
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
    logger.info("parameter puller started.");
    StochasticGradientDescent sgd =
        new StochasticGradientDescent(new L2RegLogisticDenseLoss(), dataset, hyperParams);
    logger.info("optimizer initialized.");
    LossGrad lossGrad;
    DatagramSocket[] sockets = new DatagramSocket[paramServers.size()];
    try {
      for (i = 0; i < sockets.length; i++) {
        sockets[i] = new DatagramSocket();
      }
      logger.info("connections to parameter servers established.");
      while (!isStopped()) {
        synchronized (w) {
          lossGrad = sgd.getUpdate(w);
        }

        logger.info(lossGrad);

        i = 0;
        for (InetAddress address : paramServers.keySet()) {
          ParamServerSettings settings = paramServers.get(address);
          // TODO: allocation in heap. need optimization in the future
          DenseNetworkVector update =
              new DenseNetworkVector(settings.highIndex - settings.lowIndex);
          INDArray grad =
              lossGrad.gradient.get(NDArrayIndex.interval(settings.lowIndex, settings.highIndex));
          update.setVector(grad);
          pool.execute(new GradientPusher(update, sockets[i], address, settings.upPort));
          i++;
          Thread.yield();
        }
      }
    } catch (SocketException e) {
      logger.fatal(e.getStackTrace());
    } finally {
      Arrays.stream(sockets).forEach(DatagramSocket::close);
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
