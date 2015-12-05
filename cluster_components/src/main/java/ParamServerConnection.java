import java.net.Socket;

public class ParamServerConnection implements Runnable {

    private Socket socket;
    private Master master;

    public ParamServerConnection(Master master, Socket socket) {
        this.socket = socket;
        this.master = master;
    }

    @Override
    public void run() {

    }
}
