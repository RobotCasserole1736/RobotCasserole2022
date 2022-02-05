package frc.robot;

import com.ctre.phoenix.motorcontrol.can.VictorSPX;

import frc.Constants;
import frc.lib.Calibration.Calibration;
import frc.wrappers.MotorCtrl.CasseroleCANMotorCtrl;
import frc.wrappers.MotorCtrl.CasseroleCANMotorCtrl.CANMotorCtrlType;


/*
 *******************************************************************************************
 * Copyright (C) 2020 FRC Team 1736 Robot Casserole - www.robotcasserole.org
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

public class Shooter {
	private static Shooter shooter = null;
    private CasseroleCANMotorCtrl shooterMotor;
    private VictorSPX feedWheelOne;
    private VictorSPX feedWheelTwo;

    Calibration shooter_P;
    Calibration shooter_I;
    Calibration shooter_D;
    Calibration shooter_F;
    Calibration shooter_Launch_Speed;
    Calibration allowed_Shooter_Error;

	public static synchronized Shooter getInstance() {
		if(shooter == null)
			shooter = new Shooter();
		return shooter;
	}

	// This is the private constructor that will be called once by getInstance() and it should instantiate anything that will be required by the class
	private Shooter() {

        shooterMotor = new CasseroleCANMotorCtrl("shooter", Constants.SHOOTER_MOTOR_CANID, CANMotorCtrlType.SPARK_MAX);
        feedWheelOne = new VictorSPX(Constants.SHOOTER_FEED_MOTOR_1_CANID);
        feedWheelTwo = new VictorSPX(Constants.SHOOTER_FEED_MOTOR_2_CANID);

        shooter_P = new Calibration("shooter P","",0);
        shooter_I = new Calibration("shooter I","",0);
        shooter_D = new Calibration("shooter D","",0);
        shooter_F = new Calibration("shooter F","",0.006);
        shooter_Launch_Speed = new Calibration("shooter launch speed","RPM",2000);
        allowed_Shooter_Error = new Calibration("allowed shooter error", "RPM",100);


	}

	
    // Call with true to cause the shooter to run, false to stop it.
    public void setRun(boolean runCmd){

    }


    // Call with true to cause the feed wheels to run and feed balls to the shooter. False should stop the feed motors.
    public void setFeed(boolean feedCmd){

    }

    //Call this in a periodic loop to keep the shooter up to date
    public void update(){

    }

    // Returns whether the shooter is running at its setpoint speed or not.
    public boolean getSpooleldUp(){

    }


},