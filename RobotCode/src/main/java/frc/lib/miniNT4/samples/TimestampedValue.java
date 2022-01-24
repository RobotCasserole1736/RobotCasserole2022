package frc.lib.miniNT4.samples;

import java.io.IOException;
import org.msgpack.core.MessageBufferPacker;

public abstract class TimestampedValue {
  public long timestamp_us;

  public abstract void packValue(MessageBufferPacker packer) throws IOException;

  public abstract String toNiceString();

  public abstract Object getVal();
}
