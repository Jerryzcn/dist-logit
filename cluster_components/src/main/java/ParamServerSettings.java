import java.io.Serializable;

/**
 * The parameter server settings class that records
 *  upload port number
 *  download port number
 *  lowIndex of manipulate data set
 *  highIndex of manipulate data set
 */
public class ParamServerSettings implements Serializable{
  public final int upPort;
  public final int downPort;
  public final int lowIndex;
  public final int highIndex;

  private ParamServerSettings(Builder builder) {
    upPort = builder.upPort;
    downPort = builder.downPort;
    lowIndex = builder.lowIndex;
    highIndex = builder.highIndex;
  }

  public static class Builder {

    private int upPort;
    private int downPort;
    private int lowIndex;
    private int highIndex;

    public boolean isReadyToBuild() {
      return upPort != 0 && downPort != 0;
    }

    public Builder setUpPort(int upPort) {
      this.upPort = upPort;
      return this;
    }

    public Builder setDownPort(int downPort) {
      this.downPort = downPort;
      return this;
    }

    public Builder setLowIndex(int lowIndex) {
      this.lowIndex = lowIndex;
      return this;
    }

    public Builder setHighIndex(int highIndex) {
      this.highIndex = highIndex;
      return this;
    }

    public ParamServerSettings build() {
      return new ParamServerSettings(this);
    }
  }

}
