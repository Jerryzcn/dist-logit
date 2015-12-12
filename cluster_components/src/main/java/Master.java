
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
public class Master implements Runnable{
  @Override public void run() {

  }
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

  private List<InetSocketAddress> workerAddresses;
  private List<InetSocketAddress> parameterServerAddresses;

  private List<WorkerConnection> workers;
  private List<ParamServerConnection> parameterServers;

  // store the
  private float[][] trainingData;
  private float[][] testData;


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
          final Scanner dataScan = new Scanner(new File(params.get("training_data")))
      ) {

        Map<InetAddress, ParamServerSettings> paramServerSettingsMap = new HashMap<>();
        for (int i = 0; i < parameterServerAddresses.size(); i++) {
          int upPort = 0;
          int downPort = 0;
          int lowIndex = 0;
          int highIndex = 0;
          ParamServerSettings.Builder builder = new ParamServerSettings.Builder();
          // TODO: pass this builder around to set all the parameters

          paramServerSettingsMap.put(parameterServerAddresses.get(i).getAddress(),
              //              new ParamServerSettings(upPort, downPort, lowIndex, highIndex));
              builder.build());
        }

        // set Hyper Params in Config: [0 => learning_rate, 1 => reg_constant, 2 => batch_size]
        // hard-code it for now, maybe later as well... who knows...
        float[] hyperPrams = new float[3];
        hyperPrams[0] = Float.parseFloat(params.get("learning_rate"));
        hyperPrams[1] = Float.parseFloat(params.get("reg_constant"));
        hyperPrams[2] = Float.parseFloat(params.get("batch_size"));

        // manipulate the training data
        int trainingDataWidth = 0;
        ArrayList<Float> trainingData = new ArrayList<>(INI_TRAIN_DATA_SIZE);
        while(dataScan.hasNextLine()) {

          // TODO: string tokenizer is way faster then split, will change it later
          String[] lineData = dataScan.nextLine().split(",");
          for (int i = 0; i < lineData.length; i++) {
            trainingData.add(Float.parseFloat(lineData[i]));
          }

          // TODO: this is really really really bad style cuz it's updating it again and again
          // will fix it later
          trainingDataWidth = lineData.length;
        }

        WorkerInitInfo info = new WorkerInitInfo(
            paramServerSettingsMap,
            hyperPrams,

            // TODO: this is not right, copy all the elments to array
            trainingData,
            trainingDataWidth,
            );

        WorkerConnection workerConnection = new WorkerConnection(workerSocket, info);
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
