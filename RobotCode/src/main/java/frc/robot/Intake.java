package frc.robot;

import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.motorcontrol.Spark;
import frc.Constants;
import frc.lib.Calibration.Calibration;
import frc.lib.Signal.Annotations.Signal;
import frc.wrappers.MotorCtrl.CasseroleCANMotorCtrl;
import frc.wrappers.MotorCtrl.CasseroleCANMotorCtrl.CANMotorCtrlType;

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

public class Intake {
	private static Intake intake = null;

    private CasseroleCANMotorCtrl horizIntakeMotor;
    private Spark vertIntakeMotor;

    Solenoid intakeSolenoid;

    Calibration horizIntakeSpeed;
    Calibration horizEjectSpeed;
    Calibration vertIntakeSpeed;
    Calibration vertEjectSpeed;
    Calibration horizStartDelay;

    @Signal(units = "cmd")
    intakeCmdState cmdState = intakeCmdState.STOP;
    intakeCmdState cmdStatePrev = intakeCmdState.STOP;

    @Signal(units = "cmd")
    intakeCmdState horizMotorCmd = intakeCmdState.STOP;
    
    @Signal
    boolean intakeSolenoidCmd = false;

    double horizIntakeStartTime = 0;

	public static synchronized Intake getInstance() {
		if(intake == null)
			intake = new Intake();
		return intake;
	}

	private Intake() {
        horizIntakeMotor = new CasseroleCANMotorCtrl("intakeHoriz", Constants.HORIZ_INTAKE_MOTOR_CANID, CANMotorCtrlType.TALON_FX);
        horizIntakeMotor.setInverted(true);
        vertIntakeMotor = new Spark( Constants.VERT_INTAKE_SPARK_MOTOR);
        vertIntakeMotor.setInverted(true);

        intakeSolenoid = new Solenoid(PneumaticsModuleType.REVPH,Constants.INTAKE_SOLENOID);

        horizIntakeSpeed = new Calibration("INT Horizontal Intake Speed", "", 0.5);
        horizEjectSpeed = new Calibration("INT Horizontal Eject Speed", "", -0.5);
        vertIntakeSpeed = new Calibration("INT Vertical Intake Speed", "", -0.8);
        vertEjectSpeed = new Calibration("INT Vertical Eject Speed", "", 0.8);

        horizStartDelay = new Calibration("INT Horizontal Intake Start Delay", "sec", 0.75);
	}

    public enum intakeCmdState{
        STOP(0),
        INTAKE(1),
        EJECT(-1);

        public final int value;

        private intakeCmdState(int value) {
            this.value = value;
        }

        public int toInt() {
            return this.value;
        }
    }

    public void setCmd(intakeCmdState cmd_in){
       cmdState = cmd_in;
    }

    public void update(){

        // Handle intake solenoid
        intakeSolenoidCmd =  (cmdState != intakeCmdState.STOP);
        intakeSolenoid.set(intakeSolenoidCmd);

        // Handle vertical motor
        if(cmdState == intakeCmdState.STOP) {
            vertIntakeMotor.set(0);
        } else if(cmdState == intakeCmdState.INTAKE) {
            vertIntakeMotor.set(vertIntakeSpeed.get());
        } else if(cmdState == intakeCmdState.EJECT) {
            vertIntakeMotor.set(vertEjectSpeed.get());
        }

        // Handle Horizontal motor
        if(cmdState == intakeCmdState.INTAKE && cmdStatePrev != intakeCmdState.INTAKE ){
            //Reset timer on first loop of intake
            horizIntakeStartTime = Timer.getFPGATimestamp() + horizStartDelay.get();
        }

        if(cmdState == intakeCmdState.INTAKE){
            //Keep intake stopped until we're past the start time.
            if(Timer.getFPGATimestamp() > horizIntakeStartTime){
                horizMotorCmd = intakeCmdState.INTAKE;
            } else {
                horizMotorCmd = intakeCmdState.STOP;
            }
        } else {
            horizMotorCmd = cmdState;
        }

        
        if(horizMotorCmd == intakeCmdState.STOP){
            horizIntakeMotor.setVoltageCmd(0.0);
        } else if(horizMotorCmd == intakeCmdState.INTAKE) {
            horizIntakeMotor.setVoltageCmd(horizIntakeSpeed.get()*12);
        } else if(horizMotorCmd == intakeCmdState.EJECT) {
            horizIntakeMotor.setVoltageCmd(horizEjectSpeed.get()*12);
        } 

        cmdStatePrev = cmdState;


    }

}