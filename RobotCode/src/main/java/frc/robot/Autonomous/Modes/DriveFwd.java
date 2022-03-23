package frc.robot.Autonomous.Modes;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import frc.lib.AutoSequencer.AutoSequencer;
import frc.lib.Autonomous.AutoMode;
import frc.robot.Autonomous.Events.AutoEventDriveTime;

public class DriveFwd extends AutoMode {

    AutoEventDriveTime driveEvent = null;

    @Override
    public void addStepsToSequencer(AutoSequencer seq) {
        driveEvent = new AutoEventDriveTime(1.8, 1.0, this.getInitialPose().getRotation().getRadians()); //about a robot length and a half
        seq.addEvent(driveEvent); 
    }

    @Override
    public Pose2d getInitialPose(){
        return new Pose2d(10.109, 5.717,
                          Rotation2d.fromDegrees(45.0)
                          );
    }
    
}

