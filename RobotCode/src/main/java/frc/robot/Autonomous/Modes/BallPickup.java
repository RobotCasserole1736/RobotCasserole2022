package frc.robot.Autonomous.Modes;

import edu.wpi.first.math.geometry.Pose2d;
import frc.lib.AutoSequencer.AutoSequencer;
import frc.lib.Autonomous.AutoMode;
import frc.robot.Autonomous.Events.AutoEventJSONTrajectory;

public class BallPickup extends AutoMode {

    AutoEventJSONTrajectory driveEvent = null;

    @Override
    public void addStepsToSequencer(AutoSequencer seq) {
        driveEvent = new AutoEventJSONTrajectory("pickup_loop", 0.25);
        seq.addEvent(driveEvent); 
    }

    @Override
    public Pose2d getInitialPose(){
        return driveEvent.getInitialPose();
    }
    
}

