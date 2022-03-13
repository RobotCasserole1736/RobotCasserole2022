package frc.robot.Autonomous.Modes;

import edu.wpi.first.math.geometry.Pose2d;
import frc.Constants;
import frc.lib.AutoSequencer.AutoSequencer;
import frc.lib.Autonomous.AutoMode;
import frc.robot.Autonomous.Events.AutoEventDriveBackwardTime;
import frc.robot.Autonomous.Events.AutoEventDriveForwardTime;
import frc.robot.Autonomous.Events.AutoEventIntake;
import frc.robot.Autonomous.Events.AutoEventShoot;

public class twoBallAuto extends AutoMode {

    AutoEventDriveForwardTime driveEvent = null;
    AutoEventDriveBackwardTime driveEvent2 = null;


    @Override
    public void addStepsToSequencer(AutoSequencer seq) {
        seq.addEvent(new AutoEventShoot(Constants.DOUBLE_BALL_SHOT_TIME));
        driveEvent = new AutoEventDriveForwardTime(2.0);
        driveEvent.addChildEvent(new AutoEventIntake(3));
        seq.addEvent(driveEvent); 
        driveEvent2 = new AutoEventDriveBackwardTime(2.0);
        seq.addEvent(driveEvent2); 
        seq.addEvent(new AutoEventShoot(Constants.DOUBLE_BALL_SHOT_TIME));
    }

    @Override
    public Pose2d getInitialPose(){
        return Constants.DFLT_START_POSE;
    }
    
}

