package frc.robot.Autonomous.Events;

import edu.wpi.first.wpilibj.Timer;
import frc.lib.AutoSequencer.AutoEvent;
import frc.robot.Shooter;

public class AutoEventShoot extends AutoEvent {
	
	double duration_s;
	double endTime;
	
	public AutoEventShoot(double duration_s_in) {
		duration_s = duration_s_in;
	}

	@Override
	public void userStart() {
		endTime = Timer.getFPGATimestamp() + duration_s;
        Shooter.getInstance().setRun(true);
	}

	@Override
	public void userUpdate() {
		completed = (Timer.getFPGATimestamp() > endTime);
		if (completed){
			Shooter.getInstance().setRun(false);
			}
	}

	@Override
	public void userForceStop() {
        Shooter.getInstance().setRun(false);
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
