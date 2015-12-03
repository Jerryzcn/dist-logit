import org.nd4j.linalg.dataset.DataSet;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Calculates the gradient vector to update the parameters.
 */
public class Worker implements Runnable {
  // Connections needed: tcp from master to workers
  //                     udp from workers to parameter servers

  private static final int NUM_OF_THREADS = 4;

  private float[] hyperParams;
  private int workerId;
  private boolean isStopped;
  private int tcpPort;

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
      Map<InetAddress, ParamServerSettings> paramServers = new HashMap<>();
      DataSet dataset = initialize(connectionToMaster, paramServers);
      while (!isStopped()) {
        ModelReplica model = new ModelReplica(paramServers, dataset, hyperParams);
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
  private DataSet initialize(Socket connectionToMaster,
      Map<InetAddress, ParamServerSettings> paramServers) {
    // TODO: get packets from master and sets training data, label, etc.
    try (final BufferedInputStream in = new BufferedInputStream(
        connectionToMaster.getInputStream())) {

    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

}
