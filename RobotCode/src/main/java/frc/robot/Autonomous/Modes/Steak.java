package frc.robot.Autonomous.Modes;
import edu.wpi.first.math.geometry.Pose2d;
import frc.Constants;
import frc.lib.AutoSequencer.AutoSequencer;
import frc.lib.Autonomous.AutoMode;
import frc.robot.Autonomous.Events.AutoEventIntake;
import frc.robot.Autonomous.Events.AutoEventJSONTrajectory;
import frc.robot.Autonomous.Events.AutoEventShoot;

public class Steak extends AutoMode {

    AutoEventJSONTrajectory driveEvent1 = null;
    AutoEventJSONTrajectory driveEvent2 = null;

    @Override
    public void addStepsToSequencer(AutoSequencer seq) {
        driveEvent1 = new AutoEventJSONTrajectory("many_Pickup1", 0.4);
        driveEvent1.addChildEvent(new AutoEventIntake(3));
        seq.addEvent(driveEvent1);
        seq.addEvent(new AutoEventShoot(Constants.DOUBLE_BALL_SHOT_TIME));
        driveEvent2 = new AutoEventJSONTrajectory("many_Pickup2", 0.6);
        driveEvent2.addChildEvent(new AutoEventIntake(6.0));
        seq.addEvent(driveEvent2);
        seq.addEvent(new AutoEventShoot(Constants.DOUBLE_BALL_SHOT_TIME));
       
    }

    @Override
    public Pose2d getInitialPose(){
        return driveEvent1.getInitialPose();
        
    }
    
}

