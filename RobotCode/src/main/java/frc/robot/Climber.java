package frc.robot;

import edu.wpi.first.math.filter.Debouncer;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
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
	private static Climber climber = null;
    DoubleSolenoid climb1;
    DoubleSolenoid climb2;

    @Signal
    boolean isExtended;
    boolean isExtendedRaw;
    Debouncer extendDbnc = new Debouncer(0.75);

    @Signal
    climbState climbCmd = climbState.STOP;

	public static synchronized Climber getInstance() {
		if(climber == null)
			climber = new Climber();
		return climber;
	}


    public enum climbState{
        STOP(0),
        EXTEND(1),
        RETRACT(-1);

        public final int value;
        private climbState(int value) {
            this.value = value;
        }

        public int toInt() {
            return this.value;
        }

    }

    // This is the private constructor that will be called once by getInstance() and it should instantiate anything that will be required by the class
    // The constructor should set an initial state for each solenoid - straightened for the tilt solenoid and retracted for the climb solenoid.
    private Climber() {
        climb1 = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, Constants.CLIMBER_SOLENOID1_EXTEND,Constants.CLIMBER_SOLENOID1_RETRACT);
        climb2 = new DoubleSolenoid(PneumaticsModuleType.CTREPCM,Constants.CLIMBER_SOLENOID2_EXTEND,Constants.CLIMBER_SOLENOID2_RETRACT);
        isExtended = false;
	}

    public void setClimbCmd(climbState input){
        climbCmd = input;
    }

    public boolean getIsExtended() {
        return isExtended;
    }

    public void update () {
        if(climbCmd == climbState.EXTEND){
            climb1.set(Value.kForward);
            climb2.set(Value.kForward);
            isExtendedRaw = true;
        } else if (climbCmd == climbState.RETRACT){
            climb1.set(Value.kReverse);
            climb2.set(Value.kReverse); 
            isExtendedRaw = false;
        } else {
            climb1.set(Value.kOff);
            climb2.set(Value.kOff);     
        }

        isExtended = extendDbnc.calculate(isExtendedRaw);
        
    }
	
}