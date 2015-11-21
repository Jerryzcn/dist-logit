import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by Jerry on 11/19/2015.
 * <p/>
 * Read the config files and pass the information to master using TCP.
 * <p/>
 * Config file format:
 * training_data = <trainging data path>
 * eval_data = <eval data path>
 * model_output = <model output path>
 * log_path = <logging folder path>
 * reg_constant = <regularization constant>
 * batch_size = <the batch size of mini batch gradient descent>
 */
public class Client {

    public static void main(String args[]) throws IOException {
        if (args.length != 3) {
            System.out.println("usage: <filename> <master_hostname> <port>");
            return;
        }
        int port = Integer.parseInt(args[2]);
        try (Scanner cfgScanner = new Scanner(new File(args[0]));
             Socket masterSocket = new Socket(args[1], port);
             BufferedOutputStream outBuf = new BufferedOutputStream(masterSocket.getOutputStream())) {
            while (cfgScanner.hasNextLine()) {
                outBuf.write((cfgScanner.nextLine() + "\n").getBytes("UTF-8"));
            }
        }
    }
}
