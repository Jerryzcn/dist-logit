import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Stores part of the parameters of the model.
 * Supplies the parameters to the worker, and update the parameter as worker send the gradient back.
 */
public class ParamServer implements Runnable {
  private static final int BUFFER_SIZE = 8;
  // Connections needed: tcp from master to parameter servers
  //                     udp from workers to parameter servers

  private final ServerSocket masterSocket;

  private static Logger logger = Logger.getLogger(ParamServer.class);

  private Map<InetAddress, Integer> workers;
  private float[] parameters;
  private boolean isStopped;

  public static void main(String[] args) {
    BasicConfigurator.configure();
    if (args.length != 1) {
      printUsage();
      return;
    }

    int port = Integer.parseInt(args[0]);
    ParamServer paramServer = null;
    try {
      paramServer = new ParamServer(port);
    } catch (IOException e) {
      logger.fatal(e);
    }
    paramServer.run();
  }

  private static void printUsage() {
    System.out.println("usage: <Server port>");
  }

  /**
   * Constructs a parameter server.
   *
   * @param port the tcp port to master
   */
  public ParamServer(int port) throws IOException {
    masterSocket = new ServerSocket(port);
    parameters = null;
    isStopped = true;
  }

  @Override public void run() {
    isStopped = false;
    try (final Socket masterConnection = masterSocket.accept();
        final BufferedReader inBuf = new BufferedReader(
            new InputStreamReader(masterConnection.getInputStream()));
        final BufferedOutputStream outBuf = new BufferedOutputStream(
            masterConnection.getOutputStream())) {
      int paramLength = Integer.parseInt(inBuf.readLine());
      parameters = new float[paramLength];
      try (
          final ParameterUpdater paramUpdaters = new ParameterUpdater(parameters);
          final PullHandler pullHandler = new PullHandler(parameters)) {
        int pushPort = paramUpdaters.getLocalPort();
        int pullPort = pullHandler.getLocalPort();
        respondToMaster(inBuf, outBuf, pushPort, pullPort);
        outBuf.write(Message.INITIALIZED.getBytes("UTF-8"));
        outBuf.flush();
        new Thread(paramUpdaters).start();
        while (!isStopped()) {
          // communicate with master.
          String command = inBuf.readLine();
          if (command != null) {
            switch (command) {
              case Message.STOP:
                stop();
              case Message.GET_WEIGHT:
                getWeight(outBuf);
            }
          }
        }
      }
    } catch (IOException e) {
      logger.fatal(e.getStackTrace());
    }
  }

  public boolean isStopped() {
    return isStopped;
  }

  /** Stops the parameter server */
  public void stop() {
    isStopped = true;
    try {
      masterSocket.close();
    } catch (IOException e) {
      logger.fatal(e);
    }
  }

  private void respondToMaster(BufferedReader inBuf, BufferedOutputStream outBuf, int pushPort,
      int pullPort) throws IOException {
    // TODO: setup connection with workers.
    ByteBuffer outToMaster = ByteBuffer.allocate(BUFFER_SIZE);
    outBuf.write(outToMaster.putInt(pushPort).putInt(pullPort).array());
  }

  public void getWeight(BufferedOutputStream outBuf) throws IOException {
    // TODO:
    ByteBuffer buffer = ByteBuffer.allocate(NetworkUtil.FLOAT_SIZE);
    for (float weight : parameters) {
      outBuf.write(buffer.putFloat(weight).array());
      buffer.rewind();
    }
    outBuf.flush();
  }
}
