package frc.lib.miniNT4.topics;

import frc.lib.miniNT4.NT4Server;
import frc.lib.miniNT4.NT4Types;
import frc.lib.miniNT4.samples.TimestampedBoolean;

public class BooleanTopic extends Topic{

    public BooleanTopic(String name, boolean default_in) {
        super(name, new TimestampedBoolean(default_in, 0));
        id = NT4Server.getInstance().getUniqueTopicID();
    }

    @Override
    public String getTypestring() {
        return NT4Types.BOOL.dtstr;
    }

    @Override
    public int getTypeInt() {
        return NT4Types.BOOL.type_idx;
    }
    
}
