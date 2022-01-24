package frc.robot.Autonomous.Modes;

import edu.wpi.first.math.geometry.Pose2d;
import frc.lib.AutoSequencer.AutoSequencer;
import frc.lib.Autonomous.AutoMode;
import frc.robot.Autonomous.Events.AutoEventJSONTrajectory;
import frc.robot.Autonomous.Events.AutoTrajectoryConstants;

public class DriveFwd extends AutoMode {

  AutoEventJSONTrajectory driveEvent = null;

  @Override
  public void addStepsToSequencer(AutoSequencer seq) {
    driveEvent = new AutoEventJSONTrajectory("driveFwd.wpilib.json");
    seq.addEvent(driveEvent);
  }

  @Override
  public Pose2d getInitialPose() {
    if (driveEvent != null) {
      return driveEvent.trajectory.getInitialPose();
    } else {
      return AutoTrajectoryConstants.DEFAULT_START_POSE;
    }
  }
}
