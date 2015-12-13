import com.google.common.primitives.Floats;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
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
public class Master implements Runnable {

  // Connections needed: tcp from master to workers
  //                     tcp from client to master
  //                     tcp from master to parameter serverss


  public enum Config {
    TRAINING_DATA, EVAL_DATA, MODEL_OUTPUT, LOG_PATH, REG_CONSTANT, BATCH_SIZE
  }


  private final static int BUFFER_SIZE = 1024;

  // pre-define a data length to avoid expending the array list
  private final static int INI_TRAIN_DATA_SIZE = 10000;

  private Map<String, String> params;
  private int port;

  // the list of addresses of all worker addresses
  private List<InetSocketAddress> workerAddresses;

  // the list of addresses of all parameter servers
  private List<InetSocketAddress> parameterServerAddresses;

  private List<WorkerConnection> workers;
  private List<ParamServerConnection> parameterServers;

  // we want to keep worker socket and param socket open
  private List<Socket> workerSockets;
  private List<Socket> paramSockets;

  public Master(int port) {
    this.port = port;
    this.params = new HashMap<>();
    this.workerAddresses = new ArrayList<>();
    this.parameterServerAddresses = new ArrayList<>();
    this.workers = new ArrayList<>();
    this.parameterServers = new ArrayList<>();
    this.workerSockets = new ArrayList<>();
    this.paramSockets = new ArrayList<>();
  }

  private void init() {

    try (
        final Scanner dataScan = new Scanner(new File(params.get("training_data")))
    ) {
      // manipulate the training data
      ArrayList<Float> tempTrainingData = new ArrayList<>(INI_TRAIN_DATA_SIZE);

      String[] lineData = null;
      if (dataScan.hasNextLine()) {
        lineData = dataScan.nextLine().split(",");
        for (int i = 0; i < lineData.length; i++) {
          tempTrainingData.add(Float.parseFloat(lineData[i]));
        }
      }
      int trainingDataWidth = lineData.length;

      float[] trainingData = Floats.toArray(tempTrainingData);

      // divide to to worker

      Map<InetAddress, ParamServerSettings> paramServerSettingsMap = new HashMap<>();


      ParamServerSettings.Builder[] builders =
          new ParamServerSettings.Builder[parameterServerAddresses.size()];

      for (int i = 0; i < builders.length; i++) {
        builders[i] = new ParamServerSettings.Builder();
      }
      int residual = trainingDataWidth % paramSockets.size();
      int partitionWidth = trainingDataWidth / paramSockets.size();
      int low = 0;
      for (int i = 0; i < paramSockets.size(); i++) {
        ParamServerConnection paramConnection =
            new ParamServerConnection(builders[i], paramSockets.get(i));
        parameterServers.add(paramConnection);
        new Thread(paramConnection).start();
        builders[i].setLowIndex(low);
        int high = residual > 0 ? low + partitionWidth + 1 : low + partitionWidth;
        builders[i].setHighIndex(high);
        low = high + 1;
        residual--;
      }

      int parameterServerAddressSize = parameterServerAddresses.size();

      for (int i = 0; i < parameterServerAddressSize; i++) {
        while (!builders[i].isReadyToBuild()) {
          Thread.yield();
        }
        paramServerSettingsMap
            .put(parameterServerAddresses.get(i).getAddress(), builders[i].build());
      }

      // set Hyper Params in Config: [0 => learning_rate, 1 => reg_constant, 2 => batch_size]
      // hard-code it for now, maybe later as well... who knows...
      float[] hyperPrams = new float[3];
      hyperPrams[0] = Float.parseFloat(params.get("learning_rate"));
      hyperPrams[1] = Float.parseFloat(params.get("reg_constant"));
      hyperPrams[2] = Float.parseFloat(params.get("batch_size"));

      WorkerInitInfo info = new WorkerInitInfo(paramServerSettingsMap, hyperPrams,
          // hyper prams that is obtained from a wrapper object
          trainingData, // training data that is copied in hacky way
          trainingDataWidth // training data width that is obtained in hacky way
      );

      for (int i = 0; i < workerSockets.size(); i++) {
        WorkerConnection workerConnection = new WorkerConnection(workerSockets.get(i), info);
        workers.add(workerConnection);
        new Thread(workerConnection).start();
      }

      // TODO: read, shuffle, shard training data.
      while (dataScan.hasNextLine()) {
        lineData = dataScan.nextLine().split(",");
        for (int i = 0; i < lineData.length; i++) {
          tempTrainingData.add(Float.parseFloat(lineData[i]));
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override public void run() {
    try (final ServerSocket clientServer = new ServerSocket(port);
        final Socket clientSocket = clientServer.accept();
        final BufferedReader inBuf = new BufferedReader(
            new InputStreamReader(clientSocket.getInputStream()));
        final BufferedOutputStream outBuf = new BufferedOutputStream(clientSocket.getOutputStream())
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
          }
        }
      }

      for (InetSocketAddress worker : workerAddresses) {
        workerSockets.add(new Socket(worker.getAddress(), worker.getPort()));
      }

      for (InetSocketAddress param : parameterServerAddresses) {
        paramSockets.add(new Socket(param.getAddress(), param.getPort()));
      }

      init();
    } catch (IOException e) {
      System.err.println("Cannot connect to master");
      e.printStackTrace();
    }
  }

  public static void main(String args[]) {
    int port = 31415;
    if (args.length > 0) {
      port = Integer.parseInt(args[0]);
    }
    Master master = new Master(port);
    master.run();
  }
}
