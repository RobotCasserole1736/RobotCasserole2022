package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

import edu.wpi.first.math.filter.Debouncer;
import edu.wpi.first.math.filter.Debouncer.DebounceType;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.Encoder;
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

public class Shooter {
	private static Shooter shooter = null;
    private CasseroleCANMotorCtrl shooterMotor;
    private VictorSPX feedMotor; // AKA Upper Elevator Motor

    @Signal (units = "RPM")
    double actualSpeed;
    @Signal (units = "RPM")
    double desiredSpeed;
    @Signal (units = "state")
    shooterLaunchState shooterRunCmd;
    @Signal (units = "state")
    shooterFeedCmdState feedCmdState;

    @Signal (units= "cmd")
    double feedMotorCmd;

    Calibration shooter_P;
    Calibration shooter_I;
    Calibration shooter_D;
    Calibration shooter_F;
    Calibration shooter_high_goal_Launch_Speed;
    Calibration shooter_low_goal_Launch_Speed;
    Calibration allowed_Shooter_Error;
    Calibration feedSpeed;
    Calibration ejectSpeed;
    Calibration intakeSpeed;
    Calibration yeetCargo;

    Encoder feedWheelEncoder;

    @Signal(units = "RPM")
    double feedWheelSpeed;

    @Signal
    boolean isSpooledUp = false;
    Debouncer spooledUpDebounce = new Debouncer(0.5, DebounceType.kRising);

	public static synchronized Shooter getInstance() {
		if(shooter == null)
			shooter = new Shooter();
		return shooter;
	}

	// This is the private constructor that will be called once by getInstance() and it should instantiate anything that will be required by the class
	private Shooter() {

        shooterMotor = new CasseroleCANMotorCtrl("shooter", Constants.SHOOTER_MOTOR_CANID, CANMotorCtrlType.SPARK_MAX);
        feedMotor = new VictorSPX(Constants.SHOOTER_FEED_MOTOR_CANID);
        feedMotor.setNeutralMode(NeutralMode.Brake);

        shooterMotor.setInverted(true);

        shooter_P = new Calibration("shooter P","",0.000015);
        shooter_I = new Calibration("shooter I","",0);
        shooter_D = new Calibration("shooter D","",0);
        shooter_F = new Calibration("shooter F","",0.021);
        
        shooter_high_goal_Launch_Speed = new Calibration("shooter high goal launch speed","RPM",3400);
        shooter_low_goal_Launch_Speed = new Calibration("shooter low goal launch speed","RPM",1650);
        yeetCargo = new Calibration("Yeet Cargo", "RPM", 5200);


        allowed_Shooter_Error = new Calibration("shooter allowed shooter error","RPM",75.0);
        feedSpeed = new Calibration("shooter feed speed","Cmd",1.0);
        ejectSpeed = new Calibration("shooter eject speed","Cmd",0.5);
        intakeSpeed = new Calibration("shooter intake speed","Cmd",0.75);

        feedWheelEncoder = new Encoder(Constants.SHOOTER_FEED_ENC_A, Constants.SHOOTER_FEED_ENC_B);
        feedWheelEncoder.setDistancePerPulse(Constants.SHOOTER_FEED_ENC_REV_PER_PULSE);

        shooterRunCmd = shooterLaunchState.STOP;
        feedCmdState = shooterFeedCmdState.STOP;

        calUpdate(true);
	}

    public enum shooterFeedCmdState{
        STOP(0),
        INTAKE(1),
        EJECT(-1),
        FEED(2);

        public final int value;
        private shooterFeedCmdState(int value) {
            this.value = value;
        }

        public int toInt() {
            return this.value;
        }

    }

    public enum shooterLaunchState{
        STOP(0),
        LOW_GOAL(1),
        HIGH_GOAL(2),
        YEET_CARGO(3);

        public final int value;
        private shooterLaunchState(int value) {
            this.value = value;
        }

        public int toInt() {
            return this.value;
        }

    }

    
    // Call with true to cause the feed wheels to run and feed balls to the shooter. False should stop the feed motors.
    public void setFeed(shooterFeedCmdState ShooterFeedCmdState){
        feedCmdState = ShooterFeedCmdState;
    }
	
    // Call with true to cause the shooter to run, false to stop it.
    public void setRun(shooterLaunchState runCmd){
        shooterRunCmd = runCmd;
    }

    //Call this in a periodic loop to keep the shooter up to date
    public void update(){
        feedWheelSpeed = feedWheelEncoder.getRate() * 60.0; //rev per sec to rev per min conversion
        actualSpeed = Units.radiansPerSecondToRotationsPerMinute(shooterMotor.getVelocity_radpersec());


        if (shooterRunCmd == shooterLaunchState.HIGH_GOAL){
            desiredSpeed = shooter_high_goal_Launch_Speed.get();
        } else if (shooterRunCmd == shooterLaunchState.LOW_GOAL){
            desiredSpeed = shooter_low_goal_Launch_Speed.get();
        } else if(shooterRunCmd == shooterLaunchState.YEET_CARGO){
            desiredSpeed = yeetCargo.get();
        }else {
            desiredSpeed = 0;
        }

        if(desiredSpeed != 0){
            var spd_radPerSec =  Units.rotationsPerMinuteToRadiansPerSecond(desiredSpeed);
            shooterMotor.setClosedLoopCmd(spd_radPerSec, shooter_F.get() * spd_radPerSec);
            isSpooledUp = spooledUpDebounce.calculate(Math.abs(actualSpeed - desiredSpeed) < allowed_Shooter_Error.get());
        } else {
            shooterMotor.setVoltageCmd(0);
            isSpooledUp = false;
        }
        

        if(feedCmdState == shooterFeedCmdState.STOP) {
            feedMotorCmd = 0;
        } else if(feedCmdState == shooterFeedCmdState.INTAKE) {
            feedMotorCmd = -1.0 * intakeSpeed.get();
        } else if(feedCmdState == shooterFeedCmdState.EJECT) {
            feedMotorCmd = -1.0 * ejectSpeed.get();
        } else if(feedCmdState == shooterFeedCmdState.FEED) {
            feedMotorCmd = feedSpeed.get();
        } else {
            feedMotorCmd = 0; // default - stop
        }

        feedMotor.set(ControlMode.PercentOutput, feedMotorCmd);
        shooterMotor.update();

    }

    // Returns whether the shooter is running at its setpoint speed or not.
    public boolean getSpooledUp(){
        return isSpooledUp;
    }
    public boolean isyeeting(){
        return (shooterRunCmd == shooterLaunchState.YEET_CARGO);
    }
    

    public void calUpdate(boolean force){

        // guard these Cal updates with isChanged because they write to motor controllers
        // and that soaks up can bus bandwidth, which we don't want
        //There's probably a better way to do this than this utter horrible block of characters. But meh.
        // Did you know that in vsCode you can edit multiple lines at once by holding alt, shift, and then clicking and dragging?
        if(shooter_P.isChanged() ||
           shooter_I.isChanged() ||
           shooter_D.isChanged() ||
           shooter_F.isChanged() ||
           shooter_high_goal_Launch_Speed.isChanged() ||
           allowed_Shooter_Error.isChanged() ||
           feedSpeed.isChanged() ||
            force){
            shooterMotor.setClosedLoopGains(shooter_P.get(), shooter_I.get(), shooter_D.get());
            shooter_P.acknowledgeValUpdate();
            shooter_I.acknowledgeValUpdate();
            shooter_D.acknowledgeValUpdate();
            shooter_F.acknowledgeValUpdate();
            shooter_high_goal_Launch_Speed.acknowledgeValUpdate();
            allowed_Shooter_Error.acknowledgeValUpdate();
            feedSpeed.acknowledgeValUpdate();
           
        }

    }

    public double getShooterSpeed(){
        return actualSpeed;
    
    }
    
}
