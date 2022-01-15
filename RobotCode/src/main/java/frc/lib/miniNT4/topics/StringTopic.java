package frc.lib.miniNT4.topics;

import frc.lib.miniNT4.NT4Server;
import frc.lib.miniNT4.NT4Types;
import frc.lib.miniNT4.samples.TimestampedString;

public class StringTopic extends Topic{

    public StringTopic(String name, String default_in) {
        super(name, new TimestampedString(default_in, 0));
        id = NT4Server.getInstance().getUniqueTopicID();
    }

    @Override
    public String getTypestring() {
        return NT4Types.STR.dtstr;
    }

    @Override
    public int getTypeInt() {
        return NT4Types.STR.type_idx;
    }

    
}
