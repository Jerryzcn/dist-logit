import org.nd4j.linalg.api.ndarray.INDArray;

import java.nio.ByteBuffer;

/** Read and write vector to bytes with timestamp for passing in network */
public class DenseNetworkVector {

  public final static int SIZE_OF_LONG = 8;
  public final static int SIZE_OF_FLOAT = 4;

  private float[] vector;
  private long timestamp;
  private ByteBuffer buf;

  /** vecLength represents the length of the vector (number of floats) */
  public DenseNetworkVector(int vecLength) {
    vector = new float[vecLength];
    timestamp = System.currentTimeMillis();
    buf = ByteBuffer.allocate(size());
  }

  public void readBytes(byte[] bytes) {
    ByteBuffer buffer = ByteBuffer.wrap(bytes);
    this.timestamp = buffer.getLong();
    for (int i = 0; i < vector.length; i++) {
      vector[i] = buffer.getFloat();
    }
  }

  public float[] getVector() {
    return vector;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setVector(float[] vector) {
    this.vector = vector;
    setTimestamp(System.currentTimeMillis());
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public int size() {
    return SIZE_OF_LONG + SIZE_OF_FLOAT * vector.length;
  }

  public byte[] getBytes() {
    buf.rewind();
    buf.putLong(timestamp);
    for (float f : vector) {
      buf.putFloat(f);
    }
    return buf.array();
  }

  public void setVector(INDArray grad) {
    for (int i = 0; i < grad.length(); i++) {
      vector[i] = grad.getFloat(i);
    }
    setTimestamp(System.currentTimeMillis());
  }
}
