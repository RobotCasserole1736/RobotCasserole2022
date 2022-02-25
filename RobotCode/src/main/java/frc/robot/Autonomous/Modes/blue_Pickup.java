package frc.robot.Autonomous.Modes;

import edu.wpi.first.math.geometry.Pose2d;
import frc.Constants;
import frc.lib.AutoSequencer.AutoSequencer;
import frc.lib.Autonomous.AutoMode;
import frc.robot.Autonomous.Events.AutoEventIntake;
import frc.robot.Autonomous.Events.AutoEventJSONTrajectory;
import frc.robot.Autonomous.Events.AutoEventShoot;

public class blue_Pickup extends AutoMode {

    AutoEventJSONTrajectory driveEvent = null;

    @Override
    public void addStepsToSequencer(AutoSequencer seq) {
        seq.addEvent(new AutoEventShoot(Constants.SINGLE_BALL_SHOT_TIME));
        driveEvent = new AutoEventJSONTrajectory("blue_Pickup", 0.80);
        driveEvent.addChildEvent(new AutoEventIntake(5));
        seq.addEvent(driveEvent); 
        seq.addEvent(new AutoEventShoot(Constants.DOUBLE_BALL_SHOT_TIME));
    }

    @Override
    public Pose2d getInitialPose(){
        return driveEvent.getInitialPose();
    }
    
}

