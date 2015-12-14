import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class WorkerConnection implements Runnable {

  private Socket workerSocket;
  private WorkerInitInfo info;
  private final float[] dividedData;
  private boolean isStopped;

  public WorkerConnection(Socket workerSocket, WorkerInitInfo info, float[] dividedData) {
    this.workerSocket = workerSocket;
    this.info = info;
    this.dividedData = dividedData;
    isStopped = true;
  }

  @Override public void run() {
    isStopped = false;
    try (final BufferedInputStream inBuf = new BufferedInputStream(workerSocket.getInputStream());
        final BufferedOutputStream outBuf = new BufferedOutputStream(
            workerSocket.getOutputStream());
        final ObjectOutputStream out = new ObjectOutputStream(workerSocket.getOutputStream())) {

      out.writeObject(info);
      out.writeObject(dividedData);
      out.flush();
      while (!isStopped()) {

      }
      outBuf.write(Message.STOP.getBytes("UTF-8"));
      outBuf.flush();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void stop() {
    isStopped = true;
  }

  public boolean isStopped() {
    return isStopped;
  }
}
