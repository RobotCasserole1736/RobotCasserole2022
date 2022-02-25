package frc.robot.Autonomous.Events;

import edu.wpi.first.wpilibj.Timer;
import frc.lib.AutoSequencer.AutoEvent;
import frc.robot.Intake;
import frc.robot.Intake.intakeCmdState;

public class AutoEventIntake extends AutoEvent {
	
	double duration_s;
	double endTime;
    boolean completed = false;
	
	public AutoEventIntake(double duration_s_in) {
		duration_s = duration_s_in;
	}

	@Override
	public void userStart() {
		endTime = Timer.getFPGATimestamp() + duration_s;
        Intake.getInstance().setCmd(intakeCmdState.INTAKE);
	}

	@Override
	public void userUpdate() {
    completed = (Timer.getFPGATimestamp() > endTime);
    if (completed){
        Intake.getInstance().setCmd(intakeCmdState.STOP);
        }
	}

	@Override
	public void userForceStop() {
        Intake.getInstance().setCmd(intakeCmdState.STOP);
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