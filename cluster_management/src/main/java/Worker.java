/**
 * Created by Jerry on 11/19/2015.
 * <p>
 * Calculates the gradient vector to update the parameters.
 * Launches parameter servers.
 */
public class Worker {
  // Connections needed: tcp from master to workers
  //                     udp from workers to parameter servers

  private int workerId;
  private boolean isStopped;

  public static void main(String args[]) {

  }

  public int getWorkerId() {
    return workerId;
  }

  public void setWorkerId(int workerId) {
    this.workerId = workerId;
  }

  public void stop(){

  }
}
