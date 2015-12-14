import java.net.InetSocketAddress;

public class NetworkUtil {

    public final static int FLOAT_SIZE = 4;
    public final static int INT_SIZE = 4;

    public static InetSocketAddress getAddress(String address) {
        String[] strs = address.split(":");
        if (strs.length == 2) {
            return new InetSocketAddress(strs[0], Integer.parseInt(strs[1]));
        } else {
            throw new IllegalStateException();
        }
    }
}
