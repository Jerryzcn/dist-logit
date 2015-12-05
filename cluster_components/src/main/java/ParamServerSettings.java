public class ParamServerSettings {
  public final int upPort;
  public final int downPort;
  public final int lowIndex;
  public final int highIndex;

  public ParamServerSettings(int upPort, int downPort, int lowIndex, int highIndex) {
    this.upPort = upPort;
    this.downPort = downPort;
    this.lowIndex = lowIndex;
    this.highIndex = highIndex;
  }

}
