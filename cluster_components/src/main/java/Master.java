import com.google.common.primitives.Floats;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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

  // pre-define a data length to avoid expending the array list
  private final static int INIT_TRAIN_DATA_SIZE = 10000;

  private static Logger logger = Logger.getLogger(Master.class);

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
  private boolean isStopped;

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

    try (final Scanner dataScan = new Scanner(new File(params.get("training_data")))) {
      // read, shuffle, shard training data.
      List<String> lineList = new ArrayList<>(INIT_TRAIN_DATA_SIZE);
      logger.info("start reading training data.");
      while (dataScan.hasNextLine()) {
        lineList.add(dataScan.nextLine());
      }
      logger.info("training data loaded. Start shuffle");
      Collections.shuffle(lineList);
      logger.info("shuffle finished");
      int workerSize = workerAddresses.size();
      int eachWorkerLoad = lineList.size() / workerSize;
      List<Float>[] dividedData = new ArrayList[workerSize];

      String[] lineData = null;

      // manipulate the training data
      //      ArrayList<Float> tempTrainingData = new ArrayList<>(INI_TRAIN_DATA_SIZE);
      int currentWorker = 0;
      for (int i = 0; i < lineList.size(); i++) {
        if (i % eachWorkerLoad == 0) {
          currentWorker = i / eachWorkerLoad;
          dividedData[currentWorker] = new ArrayList<>();
        }

        lineData = lineList.get(i).split(",");
        for (int j = 0; j < lineData.length; j++) {
          dividedData[currentWorker].add(Float.parseFloat(lineData[j]));
        }
      }
      int trainingDataWidth = lineData.length;
      logger
          .info("sharding finished. the length of an example (with label) is " + trainingDataWidth);

      // convert array list to array
      //      float[] trainingData = Floats.toArray(tempTrainingData);

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
            new ParamServerConnection(builders[i], paramSockets.get(i), trainingDataWidth);
        parameterServers.add(paramConnection);
        new Thread(paramConnection).run();
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
          // since we divide the data to each worker
          //trainingData, // training data that is copied in hacky way
          trainingDataWidth // training data width that is obtained in hacky way
      );


      for (int i = 0; i < workerSockets.size(); i++) {
        float[] workerData = Floats.toArray(dividedData[i]);
        WorkerConnection workerConnection =
            new WorkerConnection(workerSockets.get(i), info, workerData);
        workers.add(workerConnection);
        new Thread(workerConnection).start();
      }
    } catch (IOException e) {
      logger.fatal(e);
    }
  }

  @Override public void run() {
    isStopped = false;
    try (final ServerSocket clientServer = new ServerSocket(port);
        final Socket clientSocket = clientServer.accept();
        final BufferedReader inBuf = new BufferedReader(
            new InputStreamReader(clientSocket.getInputStream()));
        final BufferedOutputStream outBuf = new BufferedOutputStream(clientSocket.getOutputStream())
    ) {
      logger.info("client connected");
      String line;
      boolean readHeader = true;
      while (readHeader && (line = inBuf.readLine()) != null) {
        int pos = line.indexOf('=');
        if (pos != -1) {
          String param = line.substring(0, pos).trim();
          String value = line.substring(pos + 1).trim();
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
      logger.info("configuration file read.");

      for (InetSocketAddress worker : workerAddresses) {
        workerSockets.add(new Socket(worker.getAddress(), worker.getPort()));
      }
      for (InetSocketAddress param : parameterServerAddresses) {
        paramSockets.add(new Socket(param.getAddress(), param.getPort()));
      }

      init();
      logger.info("initialization finished.");
      while (!isStopped()) {
        String command = inBuf.readLine();
        if (command != null) {
          if (command.equals(Message.STOP)) {
            stop();
          }
        }
      }
    } catch (IOException e) {
      logger.fatal("Cannot connect to master", e);
    }
  }

  public void stop() {
    isStopped = true;
  }

  public boolean isStopped() {
    return isStopped;
  }

  public static void main(String args[]) {
    BasicConfigurator.configure();
    int port = 31415;
    if (args.length > 0) {
      port = Integer.parseInt(args[0]);
    }
    Master master = new Master(port);
    master.run();
  }
}
