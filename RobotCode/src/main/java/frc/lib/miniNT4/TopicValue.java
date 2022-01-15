package frc.lib.miniNT4;

import frc.lib.miniNT4.samples.TimestampedValue;
import frc.lib.miniNT4.topics.Topic;

class TopicValue
{
    public Topic topic;
    public TimestampedValue val;
    public static TopicValue of(Topic topic, TimestampedValue val) {
        TopicValue retVal = new TopicValue();
        retVal.topic = topic;
        retVal.val = val;
        return retVal;
    }
}