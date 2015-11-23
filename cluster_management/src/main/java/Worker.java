import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by Jerry on 11/19/2015.
 * <p>
 * Calculates the gradient vector to update the parameters.
 * <p>
 * Claimed by Andy Li
 */
public class Worker implements Runnable {
  // Connections needed: tcp from master to workers
  //                     udp from workers to parameter servers

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
    while (!isStopped()) {
      try (ServerSocket workerSocket = new ServerSocket(tcpPort)) {

        }catch(IOException e){
          e.printStackTrace();
        }
      }
    }
  }
