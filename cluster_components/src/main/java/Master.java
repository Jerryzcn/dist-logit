import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.*;

/**
 * Takes command from client and shard the training data.
 * Sends sharded training data to worker node, and the relevant information in config file using TCP.
 * Sends relevant information associate with the worker to parameter server.
 * Monitors the training loss of all the workers.
 * Launches parameter servers.
 * Receives statistics of parameters from parameter server.
 * Periodically send updates to client.
 * Periodically send command to parameter server to write the model to disk, and evaluates the model
 * on eval data.
 */
public class Master {
  // Connections needed: tcp from master to workers
  //                     tcp from client to master
  //                     tcp from master to parameter serverss


  public enum Config {
    TRAINING_DATA, EVAL_DATA, MODEL_OUTPUT, LOG_PATH, REG_CONSTANT, BATCH_SIZE
  }

  private final static int BUFFER_SIZE = 1024;

  public Map<String, String> params;
  public int port;

  public List<InetSocketAddress> workerAddresses;
  public List<InetSocketAddress> parameterServerAddresses;

  public List<WorkerConnection> workers;
  public List<ParamServerConnection> parameterServers;


  public Master(int port) {
    this.port = port;
    this.params = new HashMap<>();
    this.workerAddresses = new ArrayList<>();
    this.parameterServerAddresses = new ArrayList<>();
    this.workers = new ArrayList<>();
    this.parameterServers = new ArrayList<>();

    try (final ServerSocket clientServer = new ServerSocket(port);
         final Socket clientSocket = clientServer.accept();
         final BufferedReader inBuf = new BufferedReader(new InputStreamReader(
                 clientSocket.getInputStream()));
         final BufferedOutputStream outBuf = new BufferedOutputStream(
                 clientSocket.getOutputStream());
    ) {
      String line;
      boolean readHeader = true;
      while ((line = inBuf.readLine()) != null) {
        if (readHeader) {
          int pos = line.indexOf('=');
          if (pos != -1) {
            String param = line.substring(0, pos);
            String value = line.substring(pos + 1);
            if (param.equals("worker")) {
              workerAddresses.add(NetworkUtil.getAddress(value));
            } else if (param.equals("parameter_server")) {
              parameterServerAddresses.add(NetworkUtil.getAddress(value));
            } else
              params.put(param, value);
          } else if (line.equals("header_end")) {
            readHeader = false;
            init();
          }
        }
      }
    } catch (IOException e) {
      System.err.println("Cannot connect to master");
      e.printStackTrace();
    }
  }

  private void init() {

    for (InetSocketAddress worker : workerAddresses) {

      try (
        final Socket workerSocket = new Socket(worker.getAddress(), worker.getPort());
      ) {
        WorkerConnection workerConnection = new WorkerConnection(this, workerSocket);
        workers.add(workerConnection);
        Thread thread = new Thread(workerConnection);
        thread.run();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static void main(String args[]) {
    int port = 31415;
    if (args.length > 0) {
      port = Integer.parseInt(args[0]);
    }
    Master master = new Master(port);
  }
}
