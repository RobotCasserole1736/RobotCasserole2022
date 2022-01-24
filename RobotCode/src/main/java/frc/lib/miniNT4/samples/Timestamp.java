package frc.lib.miniNT4.samples;

import frc.lib.miniNT4.NT4Server;
import java.io.IOException;
import org.msgpack.core.MessageBufferPacker;

public class Timestamp extends TimestampedValue {

  public Timestamp() {
    this.timestamp_us = NT4Server.getInstance().getCurServerTime();
  }

  public Timestamp(long time) {
    this.timestamp_us = time;
  }

  @Override
  public String toNiceString() {
    return "{ Time=" + Long.toString(this.timestamp_us) + "us}";
  }

  @Override
  public void packValue(MessageBufferPacker packer) throws IOException {
    packer.packLong(this.timestamp_us);
  }

  @Override
  public Long getVal() {
    return this.timestamp_us;
  }
}
