import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

public class ParamServerConnection implements Runnable {

    private ParamServerSettings.Builder builder;
    private Socket socket;

    public ParamServerConnection(ParamServerSettings.Builder builder, Socket socket) {
        this.builder = builder;
        this.socket = socket;
    }

    @Override
    public void run() {

        try (final BufferedInputStream inBuf = new BufferedInputStream(socket.getInputStream());
             final BufferedOutputStream outBuf = new BufferedOutputStream(socket.getOutputStream())) {
            byte[] buf = new byte[NetworkUtil.INT_SIZE];
            inBuf.read(buf);
            ByteBuffer buffer = ByteBuffer.wrap(buf);
            int upPort = buffer.getInt();
            int downPort = buffer.getInt();
            builder.setUpPort(upPort)
                .setDownPort(downPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
