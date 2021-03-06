import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Reads the config files and pass the information to master using TCP.
 * <p>
 * Config file format:
 * training_data = <training data path>
 * eval_data = <eval data path>
 * model_output = <model output path>
 * log_path = <logging folder path>
 * reg_constant = <regularization constant>
 * batch_size = <the batch size of mini batch gradient descent>
 * learning_rate = <learning rate for stochastic gradient descent>
 * worker = <worker address>:<worker tcp port>
 * worker = <worker address>:<worker tcp port>
 * ...
 * parameter_server = <parameter server address>:<parameter server tcp port>
 * ...
 */
public class Client {
  // Connections needed: tcp from client to master

  private final static int BUFFER_SIZE = 1024;

  public static void main(String args[]) throws IOException {
    if (args.length != 3) {
      printUsage();
      return;
    }
    int port = Integer.parseInt(args[2]);
    try (final Scanner cfgScanner = new Scanner(new File(args[0]));
        final Scanner consoleScanner = new Scanner(System.in);
        final Socket masterSocket = new Socket(args[1], port);
        final BufferedOutputStream outBuf = new BufferedOutputStream(
            masterSocket.getOutputStream())) {
      while (cfgScanner.hasNextLine()) {
        outBuf.write((cfgScanner.nextLine() + "\n").getBytes("UTF-8"));
      }

      outBuf.write("header_end\n".getBytes());
      outBuf.flush();
      // listener should be terminated by master signaling it by closing the socket.
      Thread listener = new Thread(new Runnable() {
        @Override public void run() {
          byte[] buf = new byte[BUFFER_SIZE];
          try (final BufferedInputStream inBuf = new BufferedInputStream(
              masterSocket.getInputStream())) {
            int res = inBuf.read(buf);
            while (res != -1) {
              System.out.write(buf, 0, res);
              System.out.flush();
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      });
      listener.start();
      while (consoleScanner.hasNext()) {
        outBuf.write((consoleScanner.nextLine() + "\n").getBytes("UTF-8"));
        outBuf.flush();
      }
    } catch (FileNotFoundException e1) {
      System.err.println("Cannot find the config file");
      e1.printStackTrace();
    } catch (IOException e2) {
      System.err.println("Cannot connect to master");
      e2.printStackTrace();
    }
  }

  private static void printUsage() {
    System.out.println(
        "usage: <filename> <master_hostname> <port>\n" + "terminate the program by enter stop");
  }
}
