import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Update the parameters in parameter servers.
 */
public class ParameterUpdater implements Runnable {
  private final InetAddress workerAddress;
  private final int port;

  private DenseNetworkVector gradient;
  private float[] parameters;
  private int packetLength;
  private boolean isStopped;

  public ParameterUpdater(float[] parameters, InetAddress workerAddress, int port,
      int packetLength) {
    this.parameters = parameters;
    this.workerAddress = workerAddress;
    this.port = port;
    gradient = new DenseNetworkVector(parameters.length);
    this.isStopped = true;
  }

  @Override public void run() {
    isStopped = false;
    byte[] buf = new byte[gradient.size()];
    while (!isStopped()) {
      try (DatagramSocket in = new DatagramSocket(port)) {
        in.receive(new DatagramPacket(buf, packetLength));
        gradient.readBytes(buf);
        float[] update = gradient.getVector();
        for (int i = 0; i < update.length; i++) {
          parameters[i] = update[i];
        }
      } catch (SocketException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public boolean isStopped() {
    return isStopped;
  }

  public void stop() {
    isStopped = true;
  }
}
