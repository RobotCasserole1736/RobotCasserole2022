package frc.lib.miniNT4.topics;


import frc.lib.miniNT4.NT4Server;
import frc.lib.miniNT4.NT4Types;
import frc.lib.miniNT4.samples.TimestampedInteger;

public class IntegerTopic extends Topic{

    public IntegerTopic(String name, int default_in) {
        super(name, new TimestampedInteger(default_in, 0));
        id = NT4Server.getInstance().getUniqueTopicID();
    }

    @Override
    public String getTypestring() {
        return NT4Types.INT.dtstr;
    }

    @Override
    public int getTypeInt() {
        return NT4Types.INT.type_idx;
    }

    
}
