import org.nd4j.linalg.api.ndarray.INDArray;

import java.net.DatagramSocket;
import java.net.InetAddress;

/** Pushes the newly calculated gradient to parameter servers. */
public class GradientPusher implements Runnable {

  private final InetAddress address;
  private final int port;
  private final DatagramSocket socket;
  private final INDArray gradient;

  public GradientPusher(INDArray gradient, DatagramSocket socket, InetAddress address, int port) {
    this.gradient = gradient;
    this.socket = socket;
    this.address = address;
    this.port = port;
  }

  @Override public void run() {
    // TODO: implement this.
  }
}
