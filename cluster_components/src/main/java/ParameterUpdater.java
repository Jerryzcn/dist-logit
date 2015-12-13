import org.apache.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/** Update the parameters in parameter servers */
public class ParameterUpdater implements Runnable, Closeable {
  private static final long TIME_BOUND = 1000L;

  private static Logger logger = Logger.getLogger(ParameterUpdater.class);

  private final DatagramSocket socket;

  private DenseNetworkVector gradient;
  private float[] parameters;
  private boolean isStopped;

  public ParameterUpdater(float[] parameters) throws SocketException {
    this.parameters = parameters;
    this.socket = new DatagramSocket();
    gradient = new DenseNetworkVector(parameters.length);
    this.isStopped = true;
  }

  @Override public void run() {
    isStopped = false;
    byte[] buf = new byte[gradient.size()];
    while (!isStopped()) {
      try {
        socket.receive(new DatagramPacket(buf, buf.length));
        gradient.readBytes(buf);
        if (!isStale(gradient)) {
          float[] update = gradient.getVector();
          for (int i = 0; i < update.length; i++) {
            parameters[i] = update[i];
          }
        }
      } catch (SocketException e) {
        logger.fatal(e);
        e.printStackTrace();
      } catch (IOException e) {
        logger.fatal(e);
        e.printStackTrace();
      }
    }
  }

  public boolean isStale(DenseNetworkVector vector) {
    return System.currentTimeMillis() - vector.getTimestamp() < TIME_BOUND;
  }

  public int getLocalPort() {
    return socket.getLocalPort();
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
}
