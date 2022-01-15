package frc.lib.Autonomous;

import edu.wpi.first.math.geometry.Pose2d;
import frc.lib.AutoSequencer.AutoSequencer;
import frc.robot.Autonomous.Events.AutoTrajectoryConstants;

public abstract class AutoMode {

    public String humanReadableName = "";

    public int idx = -1;

    public abstract void addStepsToSequencer(AutoSequencer seq);

    public AutoMode(){
        humanReadableName = this.getClass().getSimpleName();
    }
    
    public Pose2d getInitialPose(){
        return AutoTrajectoryConstants.DEFAULT_START_POSE;
    }
}
