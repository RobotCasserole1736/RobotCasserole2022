package frc.robot;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.Solenoid;
import frc.Constants;
import frc.lib.Signal.Annotations.Signal;

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

public class Climber {
	// You will want to rename all instances of "Climber" with your actual class name and "climber" with a variable name
	private static Climber climber = null;
    DoubleSolenoid tilt;
    DoubleSolenoid climb1;
    DoubleSolenoid climb2;
    @Signal (units="cmd")
    boolean tiltExtendCmd;
   @Signal (units="cmd")
    boolean climbExtendCmd;
	public static synchronized Climber getInstance() {
		if(climber == null)
			climber = new Climber();
		return climber;
	}

    // This is the private constructor that will be called once by getInstance() and it should instantiate anything that will be required by the class
    // The constructor should set an initial state for each solenoid - straightened for the tilt solenoid and retracted for the climb solenoid.
    private Climber() {
        tilt = new DoubleSolenoid (PneumaticsModuleType.REVPH, Constants.TILT_SOLENOID_EXTEND, Constants.TILT_SOLENOID_RETRACT);
        climb1 = new DoubleSolenoid (PneumaticsModuleType.REVPH, Constants.CLIMBER_SOLENOID1_EXTEND,Constants.CLIMBER_SOLENOID1_RETRACT);
        climb2 = new DoubleSolenoid (PneumaticsModuleType.REVPH, Constants.CLIMBER_SOLENOID2_EXTEND,Constants.CLIMBER_SOLENOID2_RETRACT);
        extendTiltClimber();
        retractClimber();

        //Default state
        tiltExtendCmd = true; // default, extend tilt cylinders
        climbExtendCmd = false; // retract climb cylinders

	}
    public void extendTiltClimber() {
        tiltExtendCmd = true;
    }
    public void retractTiltClimber() {
        tiltExtendCmd = false;
    }
    public void extendClimber() {
        climbExtendCmd = true;
    }
    public void retractClimber() {
        climbExtendCmd = false;
    }
    public boolean getIsTilted() {
        return (tiltExtendCmd == true);

    }
    public boolean getIsExtended() {
        return (climbExtendCmd == true);
        
    }
    public void update () {
        if(climbExtendCmd){
            climb1.set(DoubleSolenoid.Value.kForward);
            climb2.set(DoubleSolenoid.Value.kForward);
        } else {
            climb1.set(DoubleSolenoid.Value.kReverse);
            climb2.set(DoubleSolenoid.Value.kReverse);
        }

        if(tiltExtendCmd){
            tilt.set(DoubleSolenoid.Value.kForward);
        } else {
            tilt.set(DoubleSolenoid.Value.kReverse);
        }
        
    }
	
}