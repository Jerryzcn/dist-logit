/**
 * The parameter server settings class that records
 *  upload port number
 *  download port number
 *  lowIndex of manipulate data set
 *  highIndex of manipulate data set
 */
public class ParamServerSettings {
  private final int upPort;
  private final int downPort;
  private final int lowIndex;
  private final int highIndex;

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

    public Builder() {

    }

    public Builder setUpPort(int upPort) {
      this.upPort = upPort;
      return this;
    }

    public Builder setDownPort(int downPort) {
      this.upPort = downPort;
      return this;
    }

    public Builder setLowIndex(int lowIndex) {
      this.upPort = lowIndex;
      return this;
    }

    public Builder sethighIndex(int highIndex) {
      this.upPort = highIndex;
      return this;
    }

    public ParamServerSettings build() {
      return new ParamServerSettings(this);
    }
  }

}
