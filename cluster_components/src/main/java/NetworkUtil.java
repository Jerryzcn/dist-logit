import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class NetworkUtil {

    public final static int FLOAT_SIZE = 32;

    public static InetSocketAddress getAddress(String address) {
        String[] strs = address.split(":");
        if (strs.length == 2) {
            return new InetSocketAddress(strs[0], Integer.parseInt(strs[1]));
        } else {
            throw new IllegalStateException();
        }
    }
}
