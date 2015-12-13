import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

public class ParamServerConnection implements Runnable {

  private static Logger logger = Logger.getLogger(ParamServerConnection.class);

  private ParamServerSettings.Builder builder;
  private Socket socket;
  private int paramLength;

  public ParamServerConnection(ParamServerSettings.Builder builder, Socket socket,
      int paramLength) {
    this.builder = builder;
    this.socket = socket;
    this.paramLength = paramLength;
  }

  @Override public void run() {

    try (final BufferedInputStream inBuf = new BufferedInputStream(socket.getInputStream());
        final BufferedOutputStream outBuf = new BufferedOutputStream(socket.getOutputStream())) {
      outBuf.write(("" + paramLength + "\n").getBytes("UTF-8"));
      outBuf.flush();
      logger.info("parameter vector length: " + paramLength);
      byte[] buf = new byte[NetworkUtil.INT_SIZE];
      inBuf.read(buf);
      ByteBuffer buffer = ByteBuffer.wrap(buf);
      int upPort = buffer.getInt();
      int downPort = buffer.getInt();
      builder.setUpPort(upPort).setDownPort(downPort);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
