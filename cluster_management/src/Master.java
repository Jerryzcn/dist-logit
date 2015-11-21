/**
 * Created by Jerry on 11/19/2015.
 * <p/>
 * Takes command from client and shard the tarining data.
 * Sends sharded training data to worker node, and the relavent information in config file using TCP.
 * Sends relavent information of the parameter server associate with the worker.
 * Monitor the training loss of all the workers.
 * Periodically send command to parameter server to write the model to disk, and evaluates the model
 * on eval data.
 */
public class Master {

  public enum Config {
    TRAINING_DATA, EVAL_DATA, MODEL_OUTPUT, LOG_PATH, REG_CONSTANT, BATCH_SIZE
  }

}
