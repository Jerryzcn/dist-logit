import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Pulls new parameters from parameter servers, and update the local copy.
 */
public class ParameterPuller implements Runnable {
  private final static byte[] REQUEST = new byte[] {PullHandler.VALID_REQUEST};

  private static Logger logger = Logger.getLogger(ParameterPuller.class);

  private final InetAddress address;
  private final int port;
  private final int low;
  private final int high;

  private float[] w;
  private boolean isStopped;

  public ParameterPuller(InetAddress address, float[] w, int downPort, int lowIndex,
      int highIndex) {
    this.address = address;
    this.port = downPort;
    this.low = lowIndex;
    this.high = highIndex;
    this.w = w;
    isStopped = true;
  }

  @Override public void run() {
    isStopped = false;
    try (DatagramSocket socket = new DatagramSocket()) {
      DenseNetworkVector parameters = new DenseNetworkVector(high - low);
      DatagramPacket inPacket = new DatagramPacket(new byte[parameters.size()], parameters.size());
      while (!isStopped()) {
        socket.send(new DatagramPacket(REQUEST, REQUEST.length, address, port));
        socket.receive(inPacket);
        parameters.readBytes(inPacket.getData());
        float[] update = parameters.getVector();
        for (int i = low; i < high; i++) {
          w[i] = update[i - low];
        }
        // update completes
        Thread.yield();
      }
    } catch (SocketException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public boolean isStopped() {
    return isStopped;
  }

  public void stop() {
    isStopped = true;
  }
}
