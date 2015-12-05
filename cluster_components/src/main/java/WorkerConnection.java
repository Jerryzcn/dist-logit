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

    }
}
