package frc.lib.miniNT4.samples;

import java.io.IOException;

import org.msgpack.core.MessageBufferPacker;

abstract public class TimestampedValue{
    public long timestamp_us;

    abstract public void packValue(MessageBufferPacker packer) throws IOException;

    abstract public String toNiceString();

    abstract public Object getVal();

}