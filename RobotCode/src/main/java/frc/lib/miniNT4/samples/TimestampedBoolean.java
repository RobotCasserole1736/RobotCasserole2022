package frc.lib.miniNT4.samples;

import java.io.IOException;

import org.msgpack.core.MessageBufferPacker;

public class TimestampedBoolean extends TimestampedValue {
    boolean value;

    public TimestampedBoolean(boolean value, long time){
        this.value = value;
        this.timestamp_us = time;
    }

    @Override
    public String toNiceString() {
        return "{ Time=" + Long.toString(this.timestamp_us) + "us Value=" + Boolean.toString(this.value) +"}";
    }

    @Override
    public void packValue(MessageBufferPacker packer) throws IOException {
        packer.packBoolean(value);
    }
    
    @Override
    public Boolean getVal() {
        return value;
    }
}
