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

public class DrivetrainControl {
    
    /* Singleton infrastructure */
    private static DrivetrainControl instance;
    public static DrivetrainControl getInstance() {
        if (instance == null) {
            instance = new DrivetrainControl();
        }
        return instance;
    }

    SwerveModuleControl moduleFL;
    SwerveModuleControl moduleFR;
    SwerveModuleControl moduleBL;
    SwerveModuleControl moduleBR;

    Calibration moduleWheel_kP;
    Calibration moduleWheel_kI;
    Calibration moduleWheel_kD;
    Calibration moduleWheel_kV;
    Calibration moduleWheel_kS;
    Calibration moduleAzmth_kP;
    Calibration moduleAzmth_kI;
    Calibration moduleAzmth_kD;

    Calibration hdc_translate_kP;
    Calibration hdc_translate_kI;
    Calibration hdc_translate_kD;

    Calibration hdc_rotation_kP;
    Calibration hdc_rotation_kI;
    Calibration hdc_rotation_kD;


    public DrivetrainPoseEstimator pe;

    PIDController hdc_fwdrev;
    PIDController hdc_leftright;
    ProfiledPIDController hdc_rotate;

    HolonomicDriveController hdc;

    ChassisSpeeds desChSpd = new ChassisSpeeds(0, 0, 0);

    Pose2d curDesPose = new Pose2d();

    private DrivetrainControl(){

        moduleWheel_kP = new Calibration("Drivetrain Module Wheel kP", "", 8.0);
        moduleWheel_kI = new Calibration("Drivetrain Module Wheel kI", "", 0.0);
        moduleWheel_kD = new Calibration("Drivetrain Module Wheel kD", "", 0.0);
        moduleWheel_kV = new Calibration("Drivetrain Module Wheel kV", "volts/radPerSec", 12.0/668.112);
        moduleWheel_kS = new Calibration("Drivetrain Module Wheel kS", "volts", 0.2);
        moduleAzmth_kP = new Calibration("Drivetrain Module Azmth kP", "", 8.0);
        moduleAzmth_kI = new Calibration("Drivetrain Module Azmth kI", "", 0.0);
        moduleAzmth_kD = new Calibration("Drivetrain Module Azmth kD", "", 0.0);

        hdc_translate_kP = new Calibration("Drivetrain HDC Translation kP", "", 8.0);
        hdc_translate_kI = new Calibration("Drivetrain HDC Translation kI", "", 0.0);
        hdc_translate_kD = new Calibration("Drivetrain HDC Translation kD", "", 0.0);
        hdc_rotation_kP  = new Calibration("Drivetrain HDC Rotation kP", "", 8.0);
        hdc_rotation_kI  = new Calibration("Drivetrain HDC Rotation kI", "", 0.0);
        hdc_rotation_kD  = new Calibration("Drivetrain HDC Rotation kD", "", 0.0);

        //Component PID controllers of the autonomous holonomic drive controller 
        hdc_fwdrev = new PIDController(hdc_translate_kP.get(), hdc_translate_kI.get(), hdc_translate_kD.get());
        hdc_leftright = new PIDController(hdc_rotation_kP.get(), hdc_translate_kI.get(), hdc_translate_kD.get());
        hdc_rotate = new ProfiledPIDController(hdc_rotation_kP.get(), hdc_rotation_kI.get(), hdc_rotation_kD.get(),
                    new TrapezoidProfile.Constraints(Constants.MAX_ROTATE_SPEED_RAD_PER_SEC * 0.8, 
                                                    Constants.MAX_ROTATE_ACCEL_RAD_PER_SEC_2 * 0.8));

        hdc = new HolonomicDriveController(hdc_fwdrev, hdc_leftright, hdc_rotate);

        hdc.setEnabled(true);

        moduleFL = new SwerveModuleControl("FL", Constants.FL_WHEEL_MOTOR_CANID,Constants.FL_AZMTH_MOTOR_CANID,Constants.FL_AZMTH_ENC_IDX);
        moduleFR = new SwerveModuleControl("FR", Constants.FR_WHEEL_MOTOR_CANID,Constants.FR_AZMTH_MOTOR_CANID,Constants.FR_AZMTH_ENC_IDX);
        moduleBL = new SwerveModuleControl("BL", Constants.BL_WHEEL_MOTOR_CANID,Constants.BL_AZMTH_MOTOR_CANID,Constants.BL_AZMTH_ENC_IDX);
        moduleBR = new SwerveModuleControl("BR", Constants.BR_WHEEL_MOTOR_CANID,Constants.BR_AZMTH_MOTOR_CANID,Constants.BR_AZMTH_ENC_IDX);          

        pe = DrivetrainPoseEstimator.getInstance();

        calUpdate(true);

    }

    public void setCmdFieldRelative(double fwdRevCmd, double strafeCmd, double rotateCmd){
        desChSpd = ChassisSpeeds.fromFieldRelativeSpeeds(fwdRevCmd, strafeCmd, rotateCmd, pe.getGyroHeading());
        curDesPose = pe.getEstPose();
    }

    public void setCmdRobotRelative(double fwdRevCmd, double strafeCmd, double rotateCmd){
        desChSpd = new ChassisSpeeds(fwdRevCmd, strafeCmd, rotateCmd);
        curDesPose = pe.getEstPose();
    }

    public void setCmdTrajectory(Trajectory.State desTrajState, Rotation2d desAngle){
        desChSpd = hdc.calculate(pe.getEstPose(), desTrajState, desAngle);
        curDesPose = new Pose2d(desTrajState.poseMeters.getTranslation(), desAngle);
    }

    public void stop(){
        setCmdRobotRelative(0,0,0);
    }

    public void update(){

        SwerveModuleState[] desModState;

        if(Math.abs(desChSpd.vxMetersPerSecond) > 0.01 | Math.abs(desChSpd.vyMetersPerSecond) > 0.01 | Math.abs(desChSpd.omegaRadiansPerSecond) > 0.01){
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

        moduleFL.setDesiredState(desModState[0]);
        moduleFR.setDesiredState(desModState[1]);
        moduleBL.setDesiredState(desModState[2]);
        moduleBR.setDesiredState(desModState[3]);

        double worstError = getMaxErrorMag();

        var curActualSpeed_ftpersec = pe.getSpeedFtpSec();

        moduleFL.update(curActualSpeed_ftpersec, worstError);
        moduleFR.update(curActualSpeed_ftpersec, worstError);
        moduleBL.update(curActualSpeed_ftpersec, worstError);
        moduleBR.update(curActualSpeed_ftpersec, worstError);

        pe.update();
    }


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

    public double getMaxErrorMag(){
        double maxErr = 0;
        maxErr = Math.max(maxErr, moduleFL.azmthCtrl.getErrMag_deg());
        maxErr = Math.max(maxErr, moduleFR.azmthCtrl.getErrMag_deg());
        maxErr = Math.max(maxErr, moduleBL.azmthCtrl.getErrMag_deg());
        maxErr = Math.max(maxErr, moduleFR.azmthCtrl.getErrMag_deg());
        return maxErr;
    }

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

        // these cal updates can just be done every loop, no harm to doing that

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

    public void resetWheelEncoders() {
        moduleFL.resetWheelEncoder();
        moduleFR.resetWheelEncoder();
        moduleBL.resetWheelEncoder();
        moduleBR.resetWheelEncoder();
    }

    public void updateTelemetry(){
        moduleFL.updateTelemetry();
        moduleFR.updateTelemetry();
        moduleBL.updateTelemetry();
        moduleBR.updateTelemetry();
    }

}
