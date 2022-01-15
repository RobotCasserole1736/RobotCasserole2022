package frc.lib.Signal;

import frc.lib.miniNT4.NT4Server;
import frc.lib.miniNT4.NT4TypeStr;
import frc.lib.miniNT4.samples.TimestampedDouble;
import frc.lib.miniNT4.samples.TimestampedString;
import frc.lib.miniNT4.topics.Topic;

public class Signal {

    String name;
    String units;
    Topic nt4ValTopic;
    Topic nt4UnitsTopic;

    /**
     * Class which describes one line on a plot
     * 
     * @param name_in  String of what to call the signal (human readable)
     * @param units_in units the signal is in.
     */
    public Signal(String name_in, String units_in) {
        name = name_in;
        units = units_in;

        nt4ValTopic   = NT4Server.getInstance().publishTopic(this.getNT4ValueTopicName(), NT4TypeStr.FLOAT_64, SignalWrangler.getInstance());
        nt4UnitsTopic = NT4Server.getInstance().publishTopic(this.getNT4UnitsTopicName(), NT4TypeStr.STR,      SignalWrangler.getInstance());

        nt4ValTopic.submitNewValue(new TimestampedDouble(0.0, 0));
        nt4UnitsTopic.submitNewValue(new TimestampedString(units, 0));

        SignalWrangler.getInstance().register(this);
    }

    /**
     * Adds a new sample to the signal queue. It is intended that the controls code
     * would call this once per loop to add a new datapoint to the real-time graph.
     * 
     * The boolean version converts true to 1.0 and false to 0.0.
     * 
     * @param time_in
     * @param value_in
     */
    public void addSample(double time_in_sec, boolean value_in) {
        this.addSample(time_in_sec, value_in ? 1.0 : 0.0);
    }

    /**
     * Adds a new sample to the signal queue. It is intended that the controls code
     * would call this once per loop to add a new datapoint to the real-time graph.
     * 
     * @param time_in
     * @param value_in
     */
    public void addSample(double time_in_sec, double value_in) {
        SignalWrangler.getInstance().logger.addSample(new DataSample(time_in_sec, value_in, this));
        nt4ValTopic.submitNewValue(new TimestampedDouble(value_in, Math.round(time_in_sec*1000000l)));
    }

    /**
     * @return The User-friendly name of the signal
     */
    public String getName() {
        return name;
    }

    /**
     * @return The name of the units the signal is measured in.
     */
    public String getUnits() {
        return units;
    }

    public String getNT4ValueTopicName(){ return SignalUtils.nameToNT4ValueTopic(this.name); }
    public String getNT4UnitsTopicName(){ return SignalUtils.nameToNT4UnitsTopic(this.name); }


}
