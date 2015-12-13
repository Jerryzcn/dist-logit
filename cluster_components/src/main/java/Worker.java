import org.apache.log4j.BasicConfigurator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Calculates the gradient vector to update the parameters.
 */
public class Worker implements Runnable {
  // Connections needed: tcp from master to workers
  //                     udp from workers to parameter servers

  // store the names into a set checker

  private boolean isStopped;
  private int tcpPort;

  private WorkerInitInfo info;

  public static void main(String args[]) throws Exception {
    BasicConfigurator.configure();
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
      ModelReplica model =
          new ModelReplica(info.paramServerSettingsMap, dataset, info.hyperParameters);
      new Thread(model).start();
      try (BufferedReader buf = new BufferedReader(
          new InputStreamReader(connectionToMaster.getInputStream()))) {
        while (!isStopped()) {
          String command = buf.readLine();
          if (command.equals(Message.STOP)) {
            model.stop();
            stop();
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // Sets relevant information for the worker.
  private DataSet initialize(Socket connectionToMaster) {
    DataSet dataset = null;
    try (final ObjectInputStream in = new ObjectInputStream(connectionToMaster.getInputStream())) {
      info = (WorkerInitInfo) in.readObject();
      float[] data = (float[]) in.readObject();
      INDArray d = Nd4j.create(data,
          new int[] {data.length / info.trainingDataWidth, info.trainingDataWidth});
      dataset =
          new DataSet(d.get(NDArrayIndex.interval(1, info.trainingDataWidth)), d.getColumn(0));
    } catch (IOException e1) {
      e1.printStackTrace();
    } catch (ClassNotFoundException e2) {
      e2.printStackTrace();
    }
    return dataset;
  }

}
