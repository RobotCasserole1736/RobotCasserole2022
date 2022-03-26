package frc.robot.Autonomous.Events;

import frc.Constants;

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

import com.pathplanner.lib.PathPlanner;
import com.pathplanner.lib.PathPlannerTrajectory;
import com.pathplanner.lib.PathPlannerTrajectory.PathPlannerState;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.Timer;

/**
 * Interface into the Casserole autonomous sequencer for a path-planned traversal. Simply wraps
 * path-planner functionality into the AutoEvent abstract class.
 */

public class AutoEventJSONTrajectory extends AutoEvent {

    final double MODULE_ANGLE_INIT_TIME_SEC = 0.25;

    double trajStartTime = 0;
    double trajEndTime = 0;

    boolean done = false;

    PathPlannerTrajectory path;

    DrivetrainControl dt_inst;

    public AutoEventJSONTrajectory(String jsonFileName, double speedScalar) {

        dt_inst = DrivetrainControl.getInstance();

        path = PathPlanner.loadPath(jsonFileName, 
                                    Constants.MAX_FWD_REV_SPEED_MPS * speedScalar, 
                                    Constants.MAX_TRANSLATE_ACCEL_MPS2 * speedScalar * speedScalar);       
                                    
        trajStartTime = MODULE_ANGLE_INIT_TIME_SEC;
        trajEndTime = MODULE_ANGLE_INIT_TIME_SEC + path.getTotalTimeSeconds();
    }

    /**
     * On the first loop, calculates velocities needed to take the path specified. Later loops will
     * assign these velocities to the drivetrain at the proper time.
     */
    private double startTime = 0;

    public void userUpdate() {
        double curTime = (Timer.getFPGATimestamp()-startTime);

        if(curTime >= trajEndTime) {
            //Trajectory finished, stop, we're done.
            done = true;
            dt_inst.stop();

        } else if( curTime >= trajStartTime){
            // Normal  trajectory

            // Extract current step
            PathPlannerState curState = (PathPlannerState)  path.sample(curTime - trajStartTime);
            PathPlannerState nextState = (PathPlannerState)  path.sample(curTime - trajStartTime + Constants.Ts);
            Rotation2d curHeading = curState.holonomicRotation;
            Rotation2d nextHeading = nextState.holonomicRotation;
            Rotation2d curHeadingVel = nextHeading.minus(curHeading).times(1.0 / (Constants.Ts));

            dt_inst.setCmdTrajectory(curState, curHeading, curHeadingVel, false);

            //Populate desired pose from path plan.
            PoseTelemetry.getInstance().setDesiredPose(curState.poseMeters);

        } else {
            //Trajectory Init - just servo the swerve modules to the right positions without driving them.

            // Extract current step
            PathPlannerState curState = (PathPlannerState)  path.sample(0.0);
            Rotation2d curHeading = curState.holonomicRotation;

            dt_inst.setCmdTrajectory(curState, curHeading, new Rotation2d(), true);

            //Populate desired pose from path plan.
            PoseTelemetry.getInstance().setDesiredPose(curState.poseMeters);

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


    public Pose2d getInitialPose(){
        return path.getInitialPose();
    }

}