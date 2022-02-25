package frc.robot;

import edu.wpi.first.wpilibj.BuiltInAccelerometer;
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

public class RobotAngle {
	// You will want to rename all instances of "RobotAngle" with your actual class name and "ra" with a variable name
	private static RobotAngle ra = null;
    BuiltInAccelerometer accel;
    @Signal (units = "deg")
    double tiltAngle;

	public static synchronized RobotAngle getInstance() {
		if(ra == null)
			ra = new RobotAngle();
		return ra;
	}

	// This is the private constructor that will be called once by getInstance() and it should instantiate anything that will be required by the class
	private RobotAngle() {
        accel = new BuiltInAccelerometer();
        tiltAngle = 0;

	}

    public void update(){
        tiltAngle = Math.asin(accel.getZ());

    }
	
    public double getTilt() {
        return tiltAngle;
        
    }
}