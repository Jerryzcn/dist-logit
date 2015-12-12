import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class WorkerConnection implements Runnable {

    private Socket workerSocket;
    private WorkerInitInfo info;

    public WorkerConnection(Socket workerSocket, WorkerInitInfo info) {
        this.workerSocket = workerSocket;
        this.info = info;
    }

    @Override
    public void run() {

        try (final BufferedInputStream inBuf = new BufferedInputStream(workerSocket.getInputStream());
             final BufferedOutputStream outBuf = new BufferedOutputStream(workerSocket.getOutputStream())) {

            for (InetAddress address : info.paramServerSettingsMap.keySet()) {
                ParamServerSettings paramSettings = info.paramServerSettingsMap.get(address);
                // paramServer address, worker <-> paramServer port number (upPort, downPort)
                 outBuf.write((address.getAddress() + ":"
                     + paramSettings.upPort + ":"
                     + paramSettings.downPort +"\n")
                     .getBytes());
            }
            outBuf.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
