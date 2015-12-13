import java.io.Serializable;
import java.net.InetAddress;
import java.util.Map;

public class WorkerInitInfo implements Serializable{
  public final Map<InetAddress, ParamServerSettings> paramServerSettingsMap;
  public final float[] hyperParameters;
//  public final float[] trainingData;
  public final int trainingDataWidth;

  WorkerInitInfo(Map<InetAddress, ParamServerSettings> paramServerSettingsMap,
      float[] hyperParameters, int trainingDataWidth) {
    this.paramServerSettingsMap = paramServerSettingsMap;
    this.hyperParameters = hyperParameters;
//    this.trainingData = trainingData;
    this.trainingDataWidth = trainingDataWidth;

  }
}
