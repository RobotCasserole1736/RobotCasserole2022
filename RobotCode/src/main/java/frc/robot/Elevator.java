package frc.robot;
/*
 *******************************************************************************************
 * Copyright (C) 2022 FRC Team 1736 Robot Casserole - www.robotcasserole.org
 *******************************************************************************************
 *
 * This software is released under the MIT Licence - see the license.txt
 *  file in the root of this repo.
 *
 * Non-legally-binding statement from Team 1736:
 *  Thank you for taking the time to read through our software! We hope you
 *   find it educational and informative! 
 *  Please feel free to snag our software for your own use in whatever project
 *   you have going on right now! We'd love to be able to help out! Shoot us 
 *   any questions you may have, all our contact info should be on our website
 *   (listed above).
 *  If you happen to end up using our software to make money, that is wonderful!
 *   Robot Casserole is always looking for more sponsors, so we'd be very appreciative
 *   if you would consider donating to our club to help further STEM education.
 */

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

import edu.wpi.first.math.filter.Debouncer;
import edu.wpi.first.math.filter.Debouncer.DebounceType;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.motorcontrol.Spark;
import frc.Constants;
import frc.lib.Calibration.Calibration;
import frc.lib.Signal.Annotations.Signal;

public class Elevator {
    VictorSPX lowerElevatorMotor;
	Spark vertIntakeMotor;

	DigitalInput lowerSensor;
	DigitalInput upperSensor;

	@Signal
	boolean upperBallPresent;

	@Signal
	boolean lowerBallPresent;

	@Signal ( units = "cmd")
	double lowerElevatorMotorCmd;

	@Signal(units="cmd")
	double vertIntakeMotorCmd;

	Calibration advance;
    Calibration eject;
	Calibration vertIntakeSpeed;
    Calibration vertEjectSpeed;
	@Signal(units = "cmd")
	elevatorCmdState cmdState = elevatorCmdState.STOP;

	
    @Signal
    boolean isEmpty = false;
    Debouncer emptyDebounce = new Debouncer(0.25, DebounceType.kRising);


	private static Elevator elevator = null;
	public static synchronized Elevator getInstance() {
		if(elevator == null)
			elevator = new Elevator();
		return elevator;
	}

	// This is the private constructor that will be called once by getInstance() and it should instantiate anything that will be required by the class
	private Elevator() {
        lowerElevatorMotor = new VictorSPX(Constants.ELEVATOR_LOWER_CANID);
		lowerElevatorMotor.setInverted(true);

        vertIntakeMotor = new Spark( Constants.VERT_INTAKE_SPARK_MOTOR);
        vertIntakeMotor.setInverted(true);

		advance = new Calibration("elevator advance speed", "cmd", 0.75);
		eject = new Calibration("elevator eject speed", "cmd", 0.75);
		vertIntakeSpeed = new Calibration("elevator Vertical Intake Speed", "", -0.8);
        vertEjectSpeed = new Calibration("elevator Vertical Eject Speed", "", 0.8);

		lowerSensor = new DigitalInput(Constants.ELEVATOR_LOWER_BALL_SENSOR);
		upperSensor = new DigitalInput(Constants.ELEVATOR_UPPER_BALL_SENSOR);

	}


		
	// Possible states we could want the elevator to be running in
	// STOP: don't move
	// INTAKE: move cargo into and through the singulator into the elevator
	// EJECT: move cargo toward the intake 
	public enum elevatorCmdState{
		STOP(0),
		INTAKE(1),
		SHOOT(2),
		EJECT(-1);

		public final int value;

		private elevatorCmdState(int value) {
			this.value = value;
		}

		public int toInt() {
			return this.value;
		}
	}

	public void setCmd(elevatorCmdState cmd_in){
		cmdState = cmd_in;

	}

	public void update(){
		upperBallPresent = !upperSensor.get(); //invert due to sensor technology
		lowerBallPresent = !lowerSensor.get();
		
		if(cmdState == elevatorCmdState.STOP) {
			lowerElevatorMotorCmd = 0;
			vertIntakeMotorCmd = 0;
		} else if(cmdState == elevatorCmdState.INTAKE && !isFull()) {
			lowerElevatorMotorCmd = advance.get();
			vertIntakeMotorCmd = vertIntakeSpeed.get();
		} else if(cmdState == elevatorCmdState.SHOOT) {
			lowerElevatorMotorCmd = advance.get();
			vertIntakeMotorCmd = vertIntakeSpeed.get();
		} else if(cmdState == elevatorCmdState.EJECT) {
			lowerElevatorMotorCmd = -1.0 * eject.get();
			vertIntakeMotorCmd = vertEjectSpeed.get();
		} else {
			lowerElevatorMotorCmd = 0;
			vertIntakeMotorCmd = 0;
		}

		lowerElevatorMotor.set(ControlMode.PercentOutput,lowerElevatorMotorCmd);
		vertIntakeMotor.set(vertIntakeMotorCmd);

		isEmpty = emptyDebounce.calculate(upperBallPresent == false && lowerBallPresent == false);


	}

	public boolean isFull(){
		return upperBallPresent && lowerBallPresent;
	}

	public boolean hasSomething(){
		return upperBallPresent;
	}


	public boolean isEmpty(){
		return isEmpty;
	}

}


    

