package frc.robot.Autonomous.Modes;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.util.Units;
import frc.lib.AutoSequencer.AutoSequencer;
import frc.lib.Autonomous.AutoMode;
import frc.robot.Autonomous.Events.AutoEventDriveTime;

public class DriveFwd extends AutoMode {

    AutoEventDriveTime driveEvent = null;

    @Override
    public void addStepsToSequencer(AutoSequencer seq) {
        driveEvent = new AutoEventDriveTime(3.0, 1.0);
        seq.addEvent(driveEvent); 
    }

    @Override
    public Pose2d getInitialPose(){
        return new Pose2d(Units.feetToMeters(30.0),
                          Units.feetToMeters(27.0/2+2),
                          Rotation2d.fromDegrees(45.0)
                          );
    }
    
}

