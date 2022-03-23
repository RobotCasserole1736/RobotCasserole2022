package frc.robot.Autonomous.Modes;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import frc.lib.AutoSequencer.AutoSequencer;
import frc.lib.Autonomous.AutoMode;
import frc.robot.Autonomous.Events.AutoEventShoot;

public class JustShoot extends AutoMode {

    @Override
    public void addStepsToSequencer(AutoSequencer seq) {
        seq.addEvent(new AutoEventShoot(10.0));
    }

    @Override
    public Pose2d getInitialPose(){
        return new Pose2d(7.261, 4.741, Rotation2d.fromDegrees(155));
    }
    
}

