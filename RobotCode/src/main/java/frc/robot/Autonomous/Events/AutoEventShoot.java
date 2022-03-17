package frc.robot.Autonomous.Events;

import edu.wpi.first.wpilibj.Timer;
import frc.lib.AutoSequencer.AutoEvent;
import frc.robot.Elevator;
import frc.robot.Intake;
import frc.robot.Shooter;
import frc.robot.Elevator.elevatorCmdState;
import frc.robot.Intake.intakeCmdState;
import frc.robot.Shooter.shooterFeedCmdState;
import frc.robot.Shooter.shooterLaunchState;

public class AutoEventShoot extends AutoEvent {
	
	double duration_s;
	double endTime;
	
	public AutoEventShoot(double duration_s_in) {
		duration_s = duration_s_in;
	}

	@Override
	public void userStart() {
		endTime = Timer.getFPGATimestamp() + duration_s;
        Shooter.getInstance().setRun(shooterLaunchState.HIGH_GOAL);
		Shooter.getInstance().setFeed(shooterFeedCmdState.FEED);
		Elevator.getInstance().setCmd(elevatorCmdState.SHOOT);
	}

	@Override
	public void userUpdate() {
		completed = (Timer.getFPGATimestamp() > endTime);
		if (completed){
			Shooter.getInstance().setRun(shooterLaunchState.STOP);
			Shooter.getInstance().setFeed(shooterFeedCmdState.STOP);
			Elevator.getInstance().setCmd(elevatorCmdState.STOP);

		}
	}

	@Override
	public void userForceStop() {
        Shooter.getInstance().setRun(shooterLaunchState.STOP);
		Shooter.getInstance().setFeed(shooterFeedCmdState.STOP);
		Elevator.getInstance().setCmd(elevatorCmdState.STOP);

	}

	@Override
	public boolean isTriggered() {
		return true;
	}

	@Override
	public boolean isDone() {
		return Timer.getFPGATimestamp() >= endTime;
	}
}
