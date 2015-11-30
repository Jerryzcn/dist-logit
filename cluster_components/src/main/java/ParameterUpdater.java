import java.net.InetAddress;

/** Update the parameters in parameter servers. */
public class ParameterUpdater implements Runnable {
  private final InetAddress workerAddress;
  private final int port;

  private float[] parameters;
  private boolean isStopped;

  public ParameterUpdater(float[] parameters, InetAddress workerAddress, int port) {
    this.parameters = parameters;
    this.workerAddress = workerAddress;
    this.port = port;
    this.isStopped = true;
  }

  @Override public void run() {
    isStopped = false;
    while (!isStopped()) {

    }
  }

  public boolean isStopped() {
    return isStopped;
  }

  public void stop() {
    isStopped = true;
  }
}
