package frc.lib.miniNT4.samples;

import java.io.IOException;

import org.msgpack.core.MessageUnpacker;

import frc.lib.miniNT4.NT4Types;

public class TimestampedValueFactory {
    
    public static TimestampedValue fromMsgPack(MessageUnpacker unpacker) throws IOException{
        long timestamp_us = unpacker.unpackLong();
        int typeIdx = unpacker.unpackInt();

        if(typeIdx == NT4Types.FLOAT_64.type_idx){
            double value = unpacker.unpackDouble();
            return new TimestampedDouble(value, timestamp_us);
        } else if(typeIdx == NT4Types.BOOL.type_idx){
            boolean value = unpacker.unpackBoolean();
            return new TimestampedBoolean(value, timestamp_us);
        } else if(typeIdx == NT4Types.FLOAT_32.type_idx){
            float value = unpacker.unpackFloat();
            return new TimestampedDouble(value, timestamp_us);
        } else if(typeIdx == NT4Types.INT.type_idx){
            int value = unpacker.unpackInt();
            return new TimestampedInteger(value, timestamp_us);
        } else if(typeIdx == NT4Types.STR.type_idx){
            String value = unpacker.unpackString();
            return new TimestampedString(value, timestamp_us);
        }  else {
            throw new IllegalArgumentException("Unsupported topic type idx " + typeIdx);
        }

    }

}
