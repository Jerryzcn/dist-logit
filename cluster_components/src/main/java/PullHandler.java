import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/** Handles pull requests from workers */
public class PullHandler implements Runnable, Closeable {

  private static final int BUFFER_SIZE = 1;
  public static final byte VALID_REQUEST = 7;

  private DenseNetworkVector weights;
  private float[] parameters;
  private DatagramSocket socket;
  private boolean isStopped;

  public PullHandler(float[] parameters) throws SocketException {
    this.socket = new DatagramSocket();
    this.parameters = parameters;
  }

  @Override public void run() {
    isStopped = false;
    try {
      byte[] buf = new byte[BUFFER_SIZE];
      DenseNetworkVector vector = new DenseNetworkVector(parameters.length);
      while (!isStopped()) {
        DatagramPacket request = new DatagramPacket(buf, buf.length);
        socket.receive(request);
        if (buf[0] == VALID_REQUEST) {
          //TODO: Handle Request
          vector.setVector(parameters);
          socket.send(new DatagramPacket(vector.getBytes(), vector.size(), request.getAddress(),
              request.getPort()));

        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public boolean isStopped() {
    return isStopped;
  }


  @Override public void close() throws IOException {
    if (!isStopped()) {
      isStopped = true;
    }
    socket.close();
  }

  public int getLocalPort() {
    return socket.getLocalPort();
  }
}
