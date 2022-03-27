package frc.robot.Drivetrain;

import edu.wpi.first.math.controller.HolonomicDriveController;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import frc.Constants;
import frc.lib.Calibration.Calibration;
import frc.lib.Util.FunctionGenerator;

public class DrivetrainControl {
    
    /* Singleton infrastructure */
    private static DrivetrainControl instance;
    public static DrivetrainControl getInstance() {
        if (instance == null) {
            instance = new DrivetrainControl();
        }
        return instance;
    }

    // The four swerve modules under our control
    // Naming assumes you are looking down on the top of the robot
    // Two letter abreviations are used to ensure variable names
    // stay the same number of characters, which helps keep things 
    // lined up and makes editing easier.
    // Don't use "Rear", since both "Rear" and "Right" start with the same letter
    // FL = Front Left
    // FR = Front Right
    // BL = Back Right
    // BR = Back Right.
    SwerveModuleControl moduleFL;
    SwerveModuleControl moduleFR;
    SwerveModuleControl moduleBL;
    SwerveModuleControl moduleBR;

    // Common PID Calibrations for modules
    Calibration moduleWheel_kP;
    Calibration moduleWheel_kI;
    Calibration moduleWheel_kD;
    Calibration moduleWheel_kV; // Feed-forward Voltage Constat - IE, how many volts to get a certain amount of motor speed in radians per second?
    Calibration moduleWheel_kS; // Feed-forward Static Constnat - IE, how many volts to overcome static friction and get any motion at all?
    Calibration moduleAzmth_kP;
    Calibration moduleAzmth_kI;
    Calibration moduleAzmth_kD;

    // Common PID calibrations for the Holonomic controller
    //  which follows paths in autonomous
    Calibration hdc_translate_kP;
    Calibration hdc_translate_kI;
    Calibration hdc_translate_kD;
    Calibration hdc_rotation_kP;
    Calibration hdc_rotation_kI;
    Calibration hdc_rotation_kD;

    //Gets set to true when we're attempting to servo the module angles prior to actually moving.
    boolean initAngleOnly;

    // Pose Estimator Class
    // This class is designed to combine encoder measurments and gyro readings
    // and maybe vision processing (in the future), and produce a best-guess
    // as to where the robot is at on the field at any particular moment.
    DrivetrainPoseEstimator pe;

    // Holonomic drive controller and its components
    PIDController hdc_fwdrev;
    PIDController hdc_leftright;
    ProfiledPIDController hdc_rotate;
    CasseroleHolonomicDriveController hdc;

    // Current chassis speed commands, based on desired pose or driver command inputs
    ChassisSpeeds desChSpd = new ChassisSpeeds(0, 0, 0);

    // Current module desired states, translated out of chassis speeds or test inputs or whatever.
    SwerveModuleState[] desModState;

    // Autonmous-commanded desired pose
    Pose2d curDesPose = new Pose2d();

    // Test mode tools
    // These help us inject specific waveforms into swerve modules to calibrate and test them.
    FunctionGenerator azmthFG;
    FunctionGenerator wheelFG;

    private DrivetrainControl(){

        moduleWheel_kP = new Calibration("Drivetrain Module Wheel kP", "", 0.0029); 
        moduleWheel_kI = new Calibration("Drivetrain Module Wheel kI", "", 0.0);
        moduleWheel_kD = new Calibration("Drivetrain Module Wheel kD", "", 0.0);
        moduleWheel_kV = new Calibration("Drivetrain Module Wheel kV", "volts/radPerSec", 0.018);
        moduleWheel_kS = new Calibration("Drivetrain Module Wheel kS", "volts", 0.172);
        moduleAzmth_kP = new Calibration("Drivetrain Module Azmth kP", "", 0.008);
        moduleAzmth_kI = new Calibration("Drivetrain Module Azmth kI", "", 0.0);
        moduleAzmth_kD = new Calibration("Drivetrain Module Azmth kD", "", 0.00001);

        hdc_translate_kP = new Calibration("Drivetrain HDC Translation kP", "", 4.0); 
        hdc_translate_kI = new Calibration("Drivetrain HDC Translation kI", "", 2.0); 
        hdc_translate_kD = new Calibration("Drivetrain HDC Translation kD", "", 0.0);
        hdc_rotation_kP  = new Calibration("Drivetrain HDC Rotation kP", "", 6.5); 
        hdc_rotation_kI  = new Calibration("Drivetrain HDC Rotation kI", "", 0.0); 
        hdc_rotation_kD  = new Calibration("Drivetrain HDC Rotation kD", "", 0.0);

        //Component PID controllers of the autonomous holonomic drive controller 
        hdc_fwdrev = new PIDController(hdc_translate_kP.get(), hdc_translate_kI.get(), hdc_translate_kD.get());
        hdc_leftright = new PIDController(hdc_rotation_kP.get(), hdc_translate_kI.get(), hdc_translate_kD.get());
        hdc_rotate = new ProfiledPIDController(hdc_rotation_kP.get(), hdc_rotation_kI.get(), hdc_rotation_kD.get(),
                    new TrapezoidProfile.Constraints(Constants.MAX_ROTATE_SPEED_RAD_PER_SEC * 0.8, 
                                                    Constants.MAX_ROTATE_ACCEL_RAD_PER_SEC_2 * 0.8));
        hdc_rotate.enableContinuousInput(-1.0 * Math.PI, Math.PI);

        hdc = new CasseroleHolonomicDriveController(hdc_fwdrev, hdc_leftright, hdc_rotate);

        hdc.setEnabled(true);

        moduleFL = new SwerveModuleControl("FL", Constants.FL_WHEEL_MOTOR_CANID,Constants.FL_AZMTH_MOTOR_CANID,Constants.FL_AZMTH_ENC_IDX, Constants.FL_ENCODER_MOUNT_OFFSET_RAD, true);
        moduleFR = new SwerveModuleControl("FR", Constants.FR_WHEEL_MOTOR_CANID,Constants.FR_AZMTH_MOTOR_CANID,Constants.FR_AZMTH_ENC_IDX, Constants.FR_ENCODER_MOUNT_OFFSET_RAD, false);
        moduleBL = new SwerveModuleControl("BL", Constants.BL_WHEEL_MOTOR_CANID,Constants.BL_AZMTH_MOTOR_CANID,Constants.BL_AZMTH_ENC_IDX, Constants.BL_ENCODER_MOUNT_OFFSET_RAD, true);
        moduleBR = new SwerveModuleControl("BR", Constants.BR_WHEEL_MOTOR_CANID,Constants.BR_AZMTH_MOTOR_CANID,Constants.BR_AZMTH_ENC_IDX, Constants.BR_ENCODER_MOUNT_OFFSET_RAD, false);          

        pe = DrivetrainPoseEstimator.getInstance();

        azmthFG = new FunctionGenerator("dt_azmth", "deg");
        wheelFG = new FunctionGenerator("dt_wheel", "m/s");

        calUpdate(true);

    }

    // Commands the robot to travel at a certain speed relative to the field.
    // The pose estsimator is used to "rotate" the commands into the robot's reference frame.
    // FwdRev commands along the fields's X axis (toward-opposite-alliance positive), in meters per second
    // strafeCmd commands along the field's Y axis (toward-your-alliance-driver-station-1 positive), in meters per second
    // rotateCmd commands rotation about the field's Z axis (when viewed top-down, counterclockwise positive), in radians per second.
    public void setCmdFieldRelative(double fwdRevCmd, double strafeCmd, double rotateCmd){
        desChSpd = ChassisSpeeds.fromFieldRelativeSpeeds(fwdRevCmd, strafeCmd, rotateCmd, pe.getGyroHeading());
        curDesPose = pe.getEstPose();
        initAngleOnly = false;
    }

    // Commands the robot to travel at a certain speed relative to itself.
    // FwdRev commands along the robot's X axis (forward positive), in meters per second
    // strafeCmd commands along the robot's Y axis (left positive), in meters per second
    // rotateCmd commands rotation about the robot's Z axis (when viewed top-down, counterclockwise positive), in radians per second.
    public void setCmdRobotRelative(double fwdRevCmd, double strafeCmd, double rotateCmd){
        desChSpd = new ChassisSpeeds(fwdRevCmd, strafeCmd, rotateCmd);
        curDesPose = pe.getEstPose();
        initAngleOnly = false;
    }

    // Autonomous-centric way to command the drivetrain via a Trajectory.
    // The autonomous routine must still step through the trajectory over time.
    // At before each drivetrain update() call, auto should call this with the current 
    // state along the trajectory.
    public void setCmdTrajectory(Trajectory.State desTrajState, Rotation2d desAngle,  Rotation2d desAngVel){
        setCmdTrajectory(desTrajState, desAngle, desAngVel, false);
    }

    // Autonomous-centric way to command the drivetrain via a Trajectory.
    // The autonomous routine must still step through the trajectory over time.
    // At before each drivetrain update() call, auto should call this with the current 
    // state along the trajectory.
    public void setCmdTrajectory(Trajectory.State desTrajState, Rotation2d desAngle, Rotation2d desAngVel, boolean initAngleOnly){
        desChSpd = hdc.calculate(pe.getEstPose(), desTrajState, desAngle, desAngVel);
        curDesPose = new Pose2d(desTrajState.poseMeters.getTranslation(), desAngle);
        this.initAngleOnly = initAngleOnly;
    }


    // Helper function to command the drivetrain to stop in place. Azimuths will still servo to the "stopped" cross position
    // Helpful especially for auto routines or disabled to ensure the drivetrain is commanded to stop.
    public void stop(){
        setCmdRobotRelative(0,0,0);
    }

    // Main periodic step function for Teleop, Autonomous, and Disabled
    public void update(){

        boolean motionCommanded = Math.abs(desChSpd.vxMetersPerSecond) > 0.01 | Math.abs(desChSpd.vyMetersPerSecond) > 0.01 | Math.abs(desChSpd.omegaRadiansPerSecond) > 0.01;

        if(motionCommanded | initAngleOnly){
            //In motion
            desModState = Constants.m_kinematics.toSwerveModuleStates(desChSpd);
        } else {
            //Home Position
            desModState = new SwerveModuleState[4];
            desModState[0] = new SwerveModuleState(0, Rotation2d.fromDegrees(-45));
            desModState[1] = new SwerveModuleState(0, Rotation2d.fromDegrees(45));
            desModState[2] = new SwerveModuleState(0, Rotation2d.fromDegrees(45));
            desModState[3] = new SwerveModuleState(0, Rotation2d.fromDegrees(-45));
        }

        if(initAngleOnly){
            //Force module speeds to zero
            desModState[0].speedMetersPerSecond = 0;
            desModState[1].speedMetersPerSecond = 0;
            desModState[2].speedMetersPerSecond = 0;
            desModState[3].speedMetersPerSecond = 0;
        }

        updateCommon();
    }

    // Special Test periodic step function which injects function-generator inputs into the modules
    public void testUpdate(){

        double azmthCmd = azmthFG.getValue();
        double wheelCmd = wheelFG.getValue();
        desModState = new SwerveModuleState[4];
        desModState[0] = new SwerveModuleState(wheelCmd, Rotation2d.fromDegrees(azmthCmd));
        desModState[1] = new SwerveModuleState(wheelCmd, Rotation2d.fromDegrees(azmthCmd));
        desModState[2] = new SwerveModuleState(wheelCmd, Rotation2d.fromDegrees(azmthCmd));
        desModState[3] = new SwerveModuleState(wheelCmd, Rotation2d.fromDegrees(azmthCmd));

        updateCommon();
    }

    public void testInit(){
        azmthFG.reset();
        wheelFG.reset();
    }

    private void updateCommon(){
        
        moduleFL.setDesiredState(desModState[0]);
        moduleFR.setDesiredState(desModState[1]);
        moduleBL.setDesiredState(desModState[2]);
        moduleBR.setDesiredState(desModState[3]);

        var curActualSpeed_ftpersec = pe.getSpeedFtpSec();
        moduleFL.update(curActualSpeed_ftpersec);
        moduleFR.update(curActualSpeed_ftpersec);
        moduleBL.update(curActualSpeed_ftpersec);
        moduleBR.update(curActualSpeed_ftpersec);


        pe.update();
 
    }


    // Utility telemetry reporting functions
    public SwerveModuleState [] getModuleActualStates(){
        SwerveModuleState retArr[] =  { moduleFL.getActualState(),
                                        moduleFR.getActualState(),
                                        moduleBL.getActualState(),
                                        moduleFR.getActualState()};
        return retArr;
    }

    public SwerveModuleState [] getModuleDesiredStates(){
        SwerveModuleState retArr[] =  { moduleFL.getDesiredState(),
                                        moduleFR.getDesiredState(),
                                        moduleBL.getDesiredState(),
                                        moduleFR.getDesiredState()};
        return retArr;
    }

    // Pass the current calibration values downard into child classes.
    // Should generally only be called during disabled, since we don't usually
    // want to change PID gains while running.
    public void calUpdate(boolean force){

        // guard these Cal updates with isChanged because they write to motor controlelrs
        // and that soaks up can bus bandwidth, which we don't want
        //There's probably a better way to do this than this utter horrible block of characters. But meh.
        // Did you know that in vsCode you can edit multiple lines at once by holding alt, shift, and then clicking and dragging?
        if(moduleWheel_kP.isChanged() ||
           moduleWheel_kI.isChanged() ||
           moduleWheel_kD.isChanged() ||
           moduleWheel_kV.isChanged() ||
           moduleWheel_kS.isChanged() ||
           moduleAzmth_kP.isChanged() ||
           moduleAzmth_kI.isChanged() ||
           moduleAzmth_kD.isChanged() || force){
            moduleFL.setClosedLoopGains(moduleWheel_kP.get(), moduleWheel_kI.get(), moduleWheel_kD.get(), moduleWheel_kV.get(), moduleWheel_kS.get(), moduleAzmth_kP.get(), moduleAzmth_kI.get(), moduleAzmth_kD.get());
            moduleFR.setClosedLoopGains(moduleWheel_kP.get(), moduleWheel_kI.get(), moduleWheel_kD.get(), moduleWheel_kV.get(), moduleWheel_kS.get(), moduleAzmth_kP.get(), moduleAzmth_kI.get(), moduleAzmth_kD.get());
            moduleBL.setClosedLoopGains(moduleWheel_kP.get(), moduleWheel_kI.get(), moduleWheel_kD.get(), moduleWheel_kV.get(), moduleWheel_kS.get(), moduleAzmth_kP.get(), moduleAzmth_kI.get(), moduleAzmth_kD.get());
            moduleBR.setClosedLoopGains(moduleWheel_kP.get(), moduleWheel_kI.get(), moduleWheel_kD.get(), moduleWheel_kV.get(), moduleWheel_kS.get(), moduleAzmth_kP.get(), moduleAzmth_kI.get(), moduleAzmth_kD.get());
            moduleWheel_kP.acknowledgeValUpdate();
            moduleWheel_kI.acknowledgeValUpdate();
            moduleWheel_kD.acknowledgeValUpdate();
            moduleWheel_kV.acknowledgeValUpdate();
            moduleWheel_kS.acknowledgeValUpdate();
            moduleAzmth_kP.acknowledgeValUpdate();
            moduleAzmth_kI.acknowledgeValUpdate();
            moduleAzmth_kD.acknowledgeValUpdate();
        }

        // these cal updates can just be done whenever, no harm to doing that

        hdc_fwdrev.setP(hdc_translate_kP.get());
        hdc_fwdrev.setI(hdc_translate_kI.get());
        hdc_fwdrev.setD(hdc_translate_kD.get());
        hdc_leftright.setP(hdc_translate_kP.get());
        hdc_leftright.setI(hdc_translate_kI.get());
        hdc_leftright.setD(hdc_translate_kD.get());
        hdc_rotate.setP(hdc_rotation_kP.get());
        hdc_rotate.setI(hdc_rotation_kI.get());
        hdc_rotate.setD(hdc_rotation_kD.get());

    }

    public Pose2d getCurDesiredPose(){
        return curDesPose;
    }

    public Pose2d getCurEstPose(){
        return pe.getEstPose();
    }

    // Pose Estimation relies on total accumulated distance for some calculations
    // When the pose estimator "warped" from one location to another instantaneously, 
    // we also need to reset its sources of data. 
    // This method handles that action for the wheel encoders.
    public void resetWheelEncoders() {
        moduleFL.resetWheelEncoder();
        moduleFR.resetWheelEncoder();
        moduleBL.resetWheelEncoder();
        moduleBR.resetWheelEncoder();
    }

    // Cause all non-annotated signals to broadcast a new value for the loop.
    public void updateTelemetry(){
        moduleFL.updateTelemetry();
        moduleFR.updateTelemetry();
        moduleBL.updateTelemetry();
        moduleBR.updateTelemetry();
    }

    public void setKnownPose(Pose2d poseIn){
        curDesPose = poseIn;
        pe.setKnownPose(poseIn);
        hdc_rotate.reset(poseIn.getRotation().getRadians());
    }

}
