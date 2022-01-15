package frc.lib.miniNT4.samples;

import java.io.IOException;

import org.msgpack.core.MessageBufferPacker;

public class TimestampedString extends TimestampedValue {
    String value;

    public TimestampedString(String value, long time){
        this.value = value;
        this.timestamp_us = time;
    }

    @Override
    public String toNiceString() {
        return "{ Time=" + Long.toString(this.timestamp_us) + "us Value=\"" + this.value +"\"}";
    }

    @Override
    public void packValue(MessageBufferPacker packer) throws IOException {
        packer.packString(value);
    }

    @Override
    public String getVal() {
        return value;
    }
    
}
