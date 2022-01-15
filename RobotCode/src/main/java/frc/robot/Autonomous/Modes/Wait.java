package frc.robot.Autonomous.Modes;

import frc.lib.AutoSequencer.AutoSequencer;
import frc.lib.Autonomous.AutoMode;
import frc.robot.Autonomous.Events.AutoEventWait;

public class Wait extends AutoMode {

    private double duration = 0;

    public Wait(double duration){
        super();
        this.duration = duration;
        this.humanReadableName = Double.toString(duration) + "s";
    }

    @Override
    public void addStepsToSequencer(AutoSequencer seq) {
        seq.addEvent(new AutoEventWait(duration));
    }
    
}

