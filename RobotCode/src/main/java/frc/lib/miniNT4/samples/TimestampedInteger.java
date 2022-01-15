package frc.lib.miniNT4.samples;

import java.io.IOException;

import org.msgpack.core.MessageBufferPacker;

import edu.wpi.first.wpilibj.Timer;

public class TimestampedInteger extends TimestampedValue {
    long value;

    public TimestampedInteger(long value){
        this.value = value;
        this.timestamp_us = Math.round(Timer.getFPGATimestamp() * 1000000);
    }

    public TimestampedInteger(long value, long time){
        this.value = value;
        this.timestamp_us = time;
    }

    @Override
    public String toNiceString() {
        return "{ Time=" + Long.toString(this.timestamp_us) + "us Value=" + Long.toString(this.value) +"}";
    }

    @Override
    public void packValue(MessageBufferPacker packer) throws IOException {
        packer.packLong(value);
    }

    @Override
    public Long getVal() {
        return value;
    }
    
}
