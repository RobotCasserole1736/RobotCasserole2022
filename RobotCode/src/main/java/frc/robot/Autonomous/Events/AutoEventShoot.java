package frc.robot.Autonomous.Events;

import edu.wpi.first.wpilibj.Timer;
import frc.lib.AutoSequencer.AutoEvent;
import frc.robot.Elevator;
import frc.robot.Shooter;
import frc.robot.Elevator.elevatorCmdState;
import frc.robot.Shooter.ShooterFeedCmd;
import frc.robot.Shooter.ShooterLaunchCmd;

public class AutoEventShoot extends AutoEvent {
	
	double duration_s;
	double endTime;
	final double MIN_SPOOLUP_TIME = 0.25;
	double spoolupEnd;
		
	public AutoEventShoot(double duration_s_in, boolean lowerIntake) {
		duration_s = duration_s_in;
	}
	
	public AutoEventShoot(double duration_s_in) {
		duration_s = duration_s_in;
	}

	@Override
	public void userStart() {
		endTime = Timer.getFPGATimestamp() + duration_s;
		spoolupEnd = Timer.getFPGATimestamp() + MIN_SPOOLUP_TIME;
        Shooter.getInstance().setRun(ShooterLaunchCmd.HIGH_GOAL);
		Shooter.getInstance().setFeed(ShooterFeedCmd.STOP);
		Elevator.getInstance().setCmd(elevatorCmdState.INTAKE);
	}

	@Override
	public void userUpdate() {

		var curTime =Timer.getFPGATimestamp();

		Shooter.getInstance().setRun(ShooterLaunchCmd.HIGH_GOAL);

		if(curTime > spoolupEnd  && Shooter.getInstance().getSpooledUp()){
			Shooter.getInstance().setFeed(ShooterFeedCmd.FEED);
			Elevator.getInstance().setCmd(elevatorCmdState.SHOOT);
		} else {
			Shooter.getInstance().setFeed(ShooterFeedCmd.STOP);
			Elevator.getInstance().setCmd(elevatorCmdState.INTAKE);
		}

		completed = (curTime > endTime) || Elevator.getInstance().isEmpty();
		if (completed){
			Shooter.getInstance().setRun(ShooterLaunchCmd.STOP);
			Shooter.getInstance().setFeed(ShooterFeedCmd.STOP);
			Elevator.getInstance().setCmd(elevatorCmdState.STOP);
		}
	}

	@Override
	public void userForceStop() {
        Shooter.getInstance().setRun(ShooterLaunchCmd.STOP);
		Shooter.getInstance().setFeed(ShooterFeedCmd.STOP);
		Elevator.getInstance().setCmd(elevatorCmdState.STOP);

	}

	@Override
	public boolean isTriggered() {
		return true;
	}

	@Override
	public boolean isDone() {
		return Timer.getFPGATimestamp() >= spoolupEnd && completed;
	}
}
