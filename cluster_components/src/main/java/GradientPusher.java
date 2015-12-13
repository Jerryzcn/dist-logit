import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/** Pushes the newly calculated gradient to parameter servers. */
public class GradientPusher implements Runnable {

  private final InetAddress address;
  private final int port;
  private final DatagramSocket socket;
  private final DenseNetworkVector gradient;

  public GradientPusher(DenseNetworkVector gradient, DatagramSocket socket, InetAddress address,
      int port) {
    this.gradient = gradient;
    this.socket = socket;
    this.address = address;
    this.port = port;
  }

  @Override public void run() {
    // TODO: implement this.
    DatagramPacket packet = new DatagramPacket(gradient.getBytes(), gradient.size(), address, port);
    try {
      socket.send(packet);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
