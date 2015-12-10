import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class WorkerConnection implements Runnable {

    private Socket socket;
    private Master master;

    public WorkerConnection(Master master, Socket socket) {
        this.socket = socket;
        this.master = master;
    }

    @Override
    public void run() {

        try (final BufferedInputStream inBuf = new BufferedInputStream(socket.getInputStream());
             final BufferedOutputStream outBuf = new BufferedOutputStream(socket.getOutputStream())) {

            for (InetSocketAddress address : master.parameterServerAddresses)
                outBuf.write((address.getAddress().getHostAddress() + ":" + address.getPort() + "\n").getBytes());
            outBuf.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
