package frc.robot;

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

    //private CasseroleCANMotorCtrl horizIntakeMotor;
    private Spark vertIntakeMotor;

    Calibration horizIntakeSpeed;
    Calibration horizEjectSpeed;
    Calibration vertIntakeSpeed;
    Calibration vertEjectSpeed;

    @Signal(units = "cmd")
    intakeCmdState cmdState = intakeCmdState.STOP;

	public static synchronized Intake getInstance() {
		if(intake == null)
			intake = new Intake();
		return intake;
	}

	private Intake() {
        //horizIntakeMotor = new CasseroleCANMotorCtrl("intakeHoriz", Constants.HORIZ_INTAKE_MOTOR_CANID, CANMotorCtrlType.TALON_FX);
        vertIntakeMotor = new Spark( Constants.VERT_INTAKE_SPARK_MOTOR);
        vertIntakeMotor.setInverted(true);

        horizIntakeSpeed = new Calibration("INT Horizontal Intake Speed", "", 0.8);
        horizEjectSpeed = new Calibration("INT Horizontal Eject Speed", "", -0.8);
        vertIntakeSpeed = new Calibration("INT Vertical Intake Speed", "", -0.8);
        vertEjectSpeed = new Calibration("INT Vertical Eject Speed", "", 0.8);
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
         //if(cmd_in == intakeCmdState.STOP) {
        //    horizIntakeMotor.setVoltageCmd(0);
        //} else if(cmd_in == intakeCmdState.INTAKE) {
        //    horizIntakeMotor.setVoltageCmd(horizIntakeSpeed.get());
        //} else if(cmd_in == intakeCmdState.EJECT) {
        //    horizIntakeMotor.setVoltageCmd(horizEjectSpeed.get());
        //} 

        if(cmdState == intakeCmdState.STOP) {
           vertIntakeMotor.set(0);
        } else if(cmdState == intakeCmdState.INTAKE) {
           vertIntakeMotor.set(vertIntakeSpeed.get());
        } else if(cmdState == intakeCmdState.EJECT) {
           vertIntakeMotor.set(vertEjectSpeed.get());
        }
//
        //if(cmd_in == intakeCmdState.STOP) {
        //    vertIntakeMotorR.setVoltageCmd(0);
        //} else if(cmd_in == intakeCmdState.INTAKE) {
        //    vertIntakeMotorR.setVoltageCmd(vertIntakeSpeedR.get());
        //} else if(cmd_in == intakeCmdState.EJECT) {
        //    vertIntakeMotorR.setVoltageCmd(vertIntakeSpeedR.get());
        //}
    }

}