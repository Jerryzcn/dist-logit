import java.net.InetAddress;

/**
 * TODO:
 */
public class ParameterPuller implements Runnable {
  private final InetAddress address;
  private final int port;
  private final int low;
  private final int high;

  public ParameterPuller(InetAddress address, int downPort, int lowIndex, int highIndex) {
    this.address = address;
    this.port = downPort;
    this.low = lowIndex;
    this.high = highIndex;
  }

  @Override public void run() {

  }
}
