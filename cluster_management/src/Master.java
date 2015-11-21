/**
 * Created by Jerry on 11/19/2015.
 * <p/>
 * Takes command from client and shard the training data.
 * Sends sharded training data to worker node, and the relevant information in config file using TCP.
 * Sends relevant information associate with the worker to parameter server.
 * Monitor the training loss of all the workers.
 * Periodically send updates to client.
 * Periodically send command to parameter server to write the model to disk, and evaluates the model
 * on eval data.
 */
public class Master {

  public enum Config {
    TRAINING_DATA, EVAL_DATA, MODEL_OUTPUT, LOG_PATH, REG_CONSTANT, BATCH_SIZE
  }

}
