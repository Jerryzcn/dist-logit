import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Calculates the gradient vector to update the parameters.
 */
public class Worker implements Runnable {
  // Connections needed: tcp from master to workers
  //                     udp from workers to parameter servers

  private static final int NUM_OF_THREADS = 4;
  private static final int BUFFER_SIZE = 8192;

  // name of values from configuration file that worker will store and process
  private static final String[] CONFIG_VALUES = new String[]
      {"log_path", "reg_constant", "batch_size", "learning_rate"};

  // store the names into a set checker
  private static final Set<String> CONFIG = new HashSet<>(Arrays.asList(CONFIG_VALUES));

  private float[] hyperParams;
  private int workerId;
  private boolean isStopped;
  private int tcpPort;

  private WorkerInitInfo info;

  public static void main(String args[]) throws Exception {
    if (args.length != 1) {
      printUsage();
      return;
    }

    int port = Integer.parseInt(args[0]);
    Worker worker = new Worker(port);
    worker.run();
  }

  public Worker(int tcpPort) {
    isStopped = true;
    this.tcpPort = tcpPort;
  }

  public int getWorkerId() {
    return workerId;
  }

  public void setWorkerId(int workerId) {
    this.workerId = workerId;
  }



  public void stop() {
    isStopped = true;
  }

  public boolean isStopped() {
    return isStopped;
  }

  private static void printUsage() {
    System.out.println("usage: Server port");
  }

  @Override public void run() {
    isStopped = true;
    try (ServerSocket workerSocket = new ServerSocket(tcpPort)) {
      Socket connectionToMaster = workerSocket.accept();

      // return this data set
      DataSet dataset = initialize(connectionToMaster);
      while (!isStopped()) {
        ModelReplica model = new ModelReplica(info.paramServerSettingsMap, dataset, hyperParams);
        new Thread(model).start();
        BufferedReader buf =
            new BufferedReader(new InputStreamReader(connectionToMaster.getInputStream()));
        String command = buf.readLine();
        // TODO: handle requests from master and sends report back to master.
        model.stop();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // Sets relevant information for the worker.
  private DataSet initialize(Socket connectionToMaster) {

    // TODO: get packets from master and sets training data, label, etc.
    DataSet dataset;
    try (final ObjectInputStream in = new ObjectInputStream(
        connectionToMaster.getInputStream())) {
      info = (WorkerInitInfo) in.readObject();
      float[] data = (float[]) in.readObject();
      INDArray d = Nd4j.create(data, new int[] {data.length/info.trainingDataWidth,info.trainingDataWidth});
      // TODO: work on read into Dataset
      dataset = new DataSet(d.get(new INDArrayIndex(), ), d.getColumn(0));
    } catch (IOException e1) {
      e1.printStackTrace();
    } catch (ClassNotFoundException e2) {
      e2.printStackTrace();
    }
    return dataset;
  }

}
