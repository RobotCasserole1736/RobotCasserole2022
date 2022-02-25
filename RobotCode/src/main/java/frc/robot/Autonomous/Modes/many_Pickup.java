package frc.robot.Autonomous.Modes;
import edu.wpi.first.math.geometry.Pose2d;
import frc.Constants;
import frc.lib.AutoSequencer.AutoSequencer;
import frc.lib.Autonomous.AutoMode;
import frc.robot.Autonomous.Events.AutoEventIntake;
import frc.robot.Autonomous.Events.AutoEventJSONTrajectory;
import frc.robot.Autonomous.Events.AutoEventShoot;

public class many_Pickup extends AutoMode {

    AutoEventJSONTrajectory driveEvent = null;

    @Override
    public void addStepsToSequencer(AutoSequencer seq) {
        driveEvent = new AutoEventJSONTrajectory("many_Pickup1", 0.80);
        driveEvent.addChildEvent(new AutoEventIntake(3));
        seq.addEvent(new AutoEventShoot(Constants.DOUBLE_BALL_SHOT_TIME));
        driveEvent = new AutoEventJSONTrajectory("many_Pickup2", 0.80);
        driveEvent.addChildEvent(new AutoEventIntake(5));
        seq.addEvent(new AutoEventShoot(Constants.DOUBLE_BALL_SHOT_TIME));
       
    }

    @Override
    public Pose2d getInitialPose(){
        return driveEvent.getInitialPose();
    }
    
}

