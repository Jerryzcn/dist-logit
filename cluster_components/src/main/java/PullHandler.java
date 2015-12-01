/** Handles pull requests from workers */
public class PullHandler implements Runnable {

  private DenseNetworkVector weights;
  private float[] parameters;
  private int packetLength;
  private boolean isStopped;

  public PullHandler() {

  }

  @Override public void run() {
    isStopped = false;

  }

  public boolean isStopped() {
    return isStopped;
  }

  public void stop() {
    isStopped = true;
  }
}
