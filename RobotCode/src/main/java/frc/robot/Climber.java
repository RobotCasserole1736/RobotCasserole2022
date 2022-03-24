package frc.robot;

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
    DoubleSolenoid liftCyl1;
    DoubleSolenoid liftCyl2;

    DoubleSolenoid tiltCyl;

    @Signal
    CylCmd liftCmd = CylCmd.NONE;
    
    @Signal
    CylCmd tiltCmd = CylCmd.NONE;

	public static synchronized Climber getInstance() {
		if(climber == null)
			climber = new Climber();
		return climber;
	}


    public enum CylCmd{
        NONE(0),
        EXTEND(1),
        RETRACT(-1);

        public final int value;
        private CylCmd(int value) {
            this.value = value;
        }

        public int toInt() {
            return this.value;
        }

    }

    // This is the private constructor that will be called once by getInstance() and it should instantiate anything that will be required by the class
    // The constructor should set an initial state for each solenoid - straightened for the tilt solenoid and retracted for the climb solenoid.
    private Climber() {
        liftCyl1 = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, Constants.CLIMBER_LIFT_SOL_1_EXTEND,Constants.CLIMBER_LIFT_SOL_1_RETRACT);
        liftCyl2 = new DoubleSolenoid(PneumaticsModuleType.CTREPCM,Constants.CLIMBER_LIFT_SOL_2_EXTEND,Constants.CLIMBER_LIFT_SOL_2_RETRACT);
        tiltCyl = new DoubleSolenoid(PneumaticsModuleType.CTREPCM,Constants.CLIMBER_TILT_SOL_EXTEND,Constants.CLIMBER_TILT_SOL_RETRACT);
	}

    public void setLiftCmd(CylCmd input){
        liftCmd = input;
    }

    public void setTiltCmd(CylCmd input){
        tiltCmd = input;
    }

    public boolean isClimbing(){
        return tiltCmd == CylCmd.EXTEND;
    }

    public void update () {
        if(liftCmd == CylCmd.EXTEND){
            liftCyl1.set(Value.kForward);
            liftCyl2.set(Value.kForward);
        } else if (liftCmd == CylCmd.RETRACT){
            liftCyl1.set(Value.kReverse);
            liftCyl2.set(Value.kReverse); 
        } else {
            liftCyl1.set(Value.kOff);
            liftCyl2.set(Value.kOff);     
        }

        if(tiltCmd == CylCmd.EXTEND){
            tiltCyl.set(Value.kForward);
        }else if (tiltCmd == CylCmd.RETRACT){
            tiltCyl.set(Value.kReverse);
        } else {
            tiltCyl.set(Value.kOff);
        }

        
    }
	
}