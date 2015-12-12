import java.net.InetAddress;
import java.util.Map;

public class WorkerInitInfo {
  public final Map<InetAddress, ParamServerSettings> paramServerSettingsMap;
  public final float[] hyperParameters;
  public final float[] trainingData;
  public final int trainingDataWidth;

  WorkerInitInfo(Map<InetAddress, ParamServerSettings> paramServerSettingsMap,
      float[] hyperParameters,float[] trainingData,int trainingDataWidth) {
    this.paramServerSettingsMap = paramServerSettingsMap;
    this.hyperParameters = hyperParameters;
    this.trainingData = trainingData;
    this.trainingDataWidth = trainingDataWidth;

  }
}
