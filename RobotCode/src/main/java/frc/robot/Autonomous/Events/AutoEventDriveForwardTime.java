package frc.robot.Autonomous.Events;

/*
 *******************************************************************************************
 * Copyright (C) FRC Team 1736 Robot Casserole - www.robotcasserole.org
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

import frc.lib.AutoSequencer.AutoEvent;
import frc.robot.PoseTelemetry;
import frc.robot.Drivetrain.DrivetrainControl;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.math.util.Units;

/**
 * Interface into the Casserole autonomous sequencer for a path-planned traversal. Simply wraps
 * path-planner functionality into the AutoEvent abstract class.
 */

public class AutoEventDriveForwardTime extends AutoEvent {

    boolean done = false;
    double duration = 0;

    private final double FWD_SPEED_MPS = Units.feetToMeters(3.0);

    DrivetrainControl dt_inst;

    public AutoEventDriveForwardTime(double duration_in) {

        duration = duration_in;
        dt_inst = DrivetrainControl.getInstance();    
    }

    /**
     * On the first loop, calculates velocities needed to take the path specified. Later loops will
     * assign these velocities to the drivetrain at the proper time.
     */
    private double startTime = 0;

    public void userUpdate() {
        double curTime = (Timer.getFPGATimestamp()-startTime);

        //Check for finish
        if(curTime >= duration) {
            done = true;
            dt_inst.stop();
            return;
        } else {
            dt_inst.setCmdRobotRelative(FWD_SPEED_MPS, 0.0, 0.0);
            //Populate desired pose from drivetrain - meh
            PoseTelemetry.getInstance().setDesiredPose(dt_inst.getCurEstPose());
        }

    }

    /**
     * Force both sides of the drivetrain to zero
     */
    public void userForceStop() {
       dt_inst.stop();
    }

    /**
     * Always returns true, since the routine should run as soon as it comes up in the list.
     */
    public boolean isTriggered() {
        return true; // we're always ready to go
    }

    /**
     * Returns true once we've run the whole path
     */
    public boolean isDone() {
        return done;
    }

    @Override
    public void userStart() {
        done = false;
        startTime = Timer.getFPGATimestamp();
    }


}