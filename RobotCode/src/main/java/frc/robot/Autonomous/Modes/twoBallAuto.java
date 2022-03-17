package frc.robot.Autonomous.Modes;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import frc.Constants;
import frc.lib.AutoSequencer.AutoSequencer;
import frc.lib.Autonomous.AutoMode;
import frc.robot.Autonomous.Events.AutoEventDriveTime;
import frc.robot.Autonomous.Events.AutoEventIntake;
import frc.robot.Autonomous.Events.AutoEventShoot;

public class twoBallAuto extends AutoMode {

    AutoEventDriveTime driveFwd = null;
    AutoEventDriveTime driveRev = null;

    final double DRIVE_TIME_S = 2.0;
    final double DRIVE_SPEED_MPS = 1.5;


    @Override
    public void addStepsToSequencer(AutoSequencer seq) {
        seq.addEvent(new AutoEventShoot(Constants.SINGLE_BALL_SHOT_TIME));
        driveFwd = new AutoEventDriveTime(DRIVE_TIME_S, DRIVE_SPEED_MPS);
        driveFwd.addChildEvent(new AutoEventIntake(2.5));
        seq.addEvent(driveFwd); 
        driveRev = new AutoEventDriveTime(DRIVE_TIME_S, -1.0 * DRIVE_SPEED_MPS);
        seq.addEvent(driveRev); 
        seq.addEvent(new AutoEventShoot(Constants.SINGLE_BALL_SHOT_TIME));
    }

    @Override
    public Pose2d getInitialPose(){
        return new Pose2d(7.261, 4.741, Rotation2d.fromDegrees(155));
    }
    
}

