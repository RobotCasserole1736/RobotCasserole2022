package frc.lib.miniNT4.topics;


import frc.lib.miniNT4.NT4Types;
import frc.lib.miniNT4.samples.Timestamp;
import frc.lib.miniNT4.samples.TimestampedValue;

public class TimeTopic extends Topic{

    public TimeTopic() {
        super("/serverTime_us", new Timestamp());
        id = -1; // Hardcode ID for time
        isPersistent = true;
    }

    @Override
    public String getTypestring() {
        return NT4Types.INT.dtstr;
    }

    @Override
    public int getTypeInt() {
        return NT4Types.INT.type_idx;
    }

    @Override
    public TimestampedValue getCurVal() {
        return new Timestamp();
    }
    
}
