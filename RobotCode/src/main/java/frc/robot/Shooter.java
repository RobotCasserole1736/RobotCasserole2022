package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.filter.Debouncer;
import edu.wpi.first.math.filter.LinearFilter;
import edu.wpi.first.math.filter.Debouncer.DebounceType;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Timer;
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

    LinearFilter shooterAccelFilter = LinearFilter.movingAverage(10);

    PIDController shooterPID;

    double prevSampleTime = 0;

    @Signal (units = "RPM")
    double actualSpeed;
    double actualSpeedPrev;
    @Signal (units = "RPMperSec")
    double actualAccel;
    @Signal (units = "RPM")
    double desiredSpeed;
    @Signal (units = "state")
    ShooterLaunchCmd launchCmd;
    @Signal (units = "state")
    ShooterFeedCmd feedCmd;
    @Signal(units = "state")
    ShooterState curState;

    @Signal (units= "cmd")
    double feedMotorCmd;

    Calibration shooter_P;
    Calibration shooter_I;
    Calibration shooter_D;
    Calibration shooter_F;
    Calibration shooter_high_goal_Launch_Speed;
    Calibration shooter_low_goal_Launch_Speed;
    Calibration shooterStableError;
    Calibration shooterAccelerateError;
    Calibration feedSpeed;
    Calibration ejectSpeed;
    Calibration intakeSpeed;
    Calibration yeetCargo;

    Encoder feedWheelEncoder;
    Encoder launchWheelEncoder;

    @Signal(units = "RPM")
    double feedWheelSpeed;

    @Signal
    boolean isStable = false;
    Debouncer stableDebounce = new Debouncer(0.25, DebounceType.kRising);

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

        shooter_P = new Calibration("shooter P","",0.000);
        shooter_I = new Calibration("shooter I","",0);
        shooter_D = new Calibration("shooter D","",0);
        shooter_F = new Calibration("shooter F","",0.021);

        shooterPID = new PIDController(shooter_P.get(), shooter_I.get(), shooter_D.get());
        
        shooter_high_goal_Launch_Speed = new Calibration("shooter high goal launch speed","RPM",3600);
        shooter_low_goal_Launch_Speed = new Calibration("shooter low goal launch speed","RPM",1650);
        yeetCargo = new Calibration("Yeet Cargo", "RPM", 5200);


        shooterStableError = new Calibration("shooter stable error","RPM", 300.0);
        shooterAccelerateError = new Calibration("shooter accelerate error","RPM", 1500.0);
        feedSpeed = new Calibration("shooter feed speed","Cmd",0.75);
        ejectSpeed = new Calibration("shooter eject speed","Cmd",0.5);
        intakeSpeed = new Calibration("shooter intake speed","Cmd",0.75);

        feedWheelEncoder = new Encoder(Constants.SHOOTER_FEED_ENC_A, Constants.SHOOTER_FEED_ENC_B);
        feedWheelEncoder.setDistancePerPulse(Constants.SHOOTER_FEED_ENC_REV_PER_PULSE);

        launchWheelEncoder = new Encoder(Constants.SHOOTER_LAUNCH_ENC_A, Constants.SHOOTER_LAUNCH_ENC_B);
        launchWheelEncoder.setDistancePerPulse(Constants.SHOOTER_FEED_ENC_REV_PER_PULSE);

        launchCmd = ShooterLaunchCmd.STOP;
        feedCmd = ShooterFeedCmd.STOP;
        curState = ShooterState.STOP;

        calUpdate(true);
	}

    public enum ShooterFeedCmd{
        STOP(0),
        INTAKE(1),
        EJECT(-1),
        FEED(2);

        public final int value;
        private ShooterFeedCmd(int value) {
            this.value = value;
        }

        public int toInt() {
            return this.value;
        }

    }

    public enum ShooterLaunchCmd{
        STOP(0),
        LOW_GOAL(1),
        HIGH_GOAL(2),
        YEET_CARGO(3);

        public final int value;
        private ShooterLaunchCmd(int value) {
            this.value = value;
        }

        public int toInt() {
            return this.value;
        }

    }

    public enum ShooterState{
        STOP(0),
        ACCELERATE(1),
        STABILIZE(2),
        HOLD(3);

        public final int value;
        private ShooterState(int value) {
            this.value = value;
        }

        public int toInt() {
            return this.value;
        }

    }
    
    // Call with true to cause the feed wheels to run and feed balls to the shooter. False should stop the feed motors.
    public void setFeed(ShooterFeedCmd ShooterFeedCmdState){
        feedCmd = ShooterFeedCmdState;
    }
	
    // Call with true to cause the shooter to run, false to stop it.
    public void setRun(ShooterLaunchCmd runCmd){
        launchCmd = runCmd;
    }

    //Call this in a periodic loop to keep the shooter up to date
    public void update(){

        /////////////////////////////////////////////////
        // Read sensor inputs
        var curSampleTime = Timer.getFPGATimestamp();
        feedWheelSpeed = feedWheelEncoder.getRate() * 60.0; //rev per sec to rev per min conversion
        actualSpeed = -1.0 * launchWheelEncoder.getRate() * 60.0 * Constants.SHOOTER_GEAR_RATIO; //rev per sec to rev per min conversion, back to the motor speed
        actualAccel = shooterAccelFilter.calculate( (actualSpeed - actualSpeedPrev) / (curSampleTime - prevSampleTime) );
        var speedErr =  desiredSpeed - actualSpeed; //Positive for too-slow, negative for too-fast

        prevSampleTime = curSampleTime;
        actualSpeedPrev = actualSpeed;


        /////////////////////////////////////////////////
        // Translate launch command to desired RPM
        if (launchCmd == ShooterLaunchCmd.HIGH_GOAL){
            desiredSpeed = shooter_high_goal_Launch_Speed.get();
        } else if (launchCmd == ShooterLaunchCmd.LOW_GOAL){
            desiredSpeed = shooter_low_goal_Launch_Speed.get();
        } else if(launchCmd == ShooterLaunchCmd.YEET_CARGO){
            desiredSpeed = yeetCargo.get();
        }else {
            desiredSpeed = 0;
        }

        /////////////////////////////////////////////////
        // Perform state machine transitions

        // Default - keep same state as previous
        ShooterState nextState = curState;

        if(desiredSpeed == 0.0){
            nextState = ShooterState.STOP;
        } else {
            switch(curState){
                case ACCELERATE:
                    if( speedErr < shooterAccelerateError.get()){
                        nextState = ShooterState.STABILIZE; //we're close enough to go to closed loop control
                    }
                break;
                case STABILIZE:
                    if( speedErr >= shooterAccelerateError.get()){
                        nextState = ShooterState.ACCELERATE; //shooter speed got too slow, go back to full battery
                    } else if(isStable){
                        nextState = ShooterState.HOLD;
                    }
                break;
                case HOLD:
                    if( speedErr >= shooterAccelerateError.get()){
                        nextState = ShooterState.ACCELERATE; //shooter speed way too slow, go right to full battery
                    } else if(speedErr >= shooterStableError.get() && actualAccel > 0){
                        nextState = ShooterState.ACCELERATE; //shooter speed slower that setpoint and we have started to turn around (ball has left)
                    }
                break;
                case STOP:
                    nextState = ShooterState.ACCELERATE;
                break;

            }
        }

        curState = nextState;

        /////////////////////////////////////////////////
        // Perform during-state updates
        switch(curState){
            case STOP:
                shooterMotor.setVoltageCmd(0);
                isStable = false;
                stableDebounce.calculate(false);
            break;
            case ACCELERATE:
                shooterMotor.setVoltageCmd(14.0); //max volts
                isStable = false;
                stableDebounce.calculate(false);
            break;
            case STABILIZE:
            case HOLD:
                shooterPID.setSetpoint(desiredSpeed);
                var closedLoopVoltage = shooterPID.calculate(actualSpeed);
                var spd_radPerSec =  Units.rotationsPerMinuteToRadiansPerSecond(desiredSpeed);
                shooterMotor.setVoltageCmd(shooter_F.get() * spd_radPerSec + closedLoopVoltage);
                isStable = stableDebounce.calculate(Math.abs(speedErr) < shooterStableError.get());
            break;
        }
        

        /////////////////////////////////////////////////
        // Assign feed motor based on shooter state and command
        if(feedCmd == ShooterFeedCmd.STOP) {
            feedMotorCmd = 0;
        } else if(feedCmd == ShooterFeedCmd.INTAKE) {
            feedMotorCmd = -1.0 * intakeSpeed.get();
        } else if(feedCmd == ShooterFeedCmd.EJECT) {
            feedMotorCmd = -1.0 * ejectSpeed.get();
        } else if(feedCmd == ShooterFeedCmd.FEED && curState == ShooterState.HOLD) {
            feedMotorCmd = feedSpeed.get();
        } else {
            feedMotorCmd = 0; // default - stop
        }

        feedMotor.set(ControlMode.PercentOutput, feedMotorCmd);
        shooterMotor.update();

    }

    // Returns whether the shooter is running at its setpoint speed or not.
    public boolean getSpooledUp(){
        return curState == ShooterState.HOLD;
    }

    // Returns whether the shooter is running at its setpoint speed or not.
    public boolean isSpoolingUp(){
        return (curState == ShooterState.ACCELERATE || curState == ShooterState.STABILIZE);
    }

    public boolean isyeeting(){
        return (launchCmd == ShooterLaunchCmd.YEET_CARGO && feedCmd == ShooterFeedCmd.FEED);
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
           shooterStableError.isChanged() ||
           feedSpeed.isChanged() ||
            force){
            shooterPID.setPID(shooter_P.get(), shooter_I.get(), shooter_D.get());
            shooter_P.acknowledgeValUpdate();
            shooter_I.acknowledgeValUpdate();
            shooter_D.acknowledgeValUpdate();
            shooter_F.acknowledgeValUpdate();
            shooter_high_goal_Launch_Speed.acknowledgeValUpdate();
            shooterStableError.acknowledgeValUpdate();
            feedSpeed.acknowledgeValUpdate();
           
        }

    }

    public double getShooterSpeed(){
        return actualSpeed;
    
    }
    
}
