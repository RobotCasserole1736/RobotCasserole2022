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

  public DrivetrainPoseEstimator pe;

  HolonomicDriveController hdc =
      new HolonomicDriveController(
          new PIDController(8.0, 0, 0), // Fwd/Rev Trajectory Tracking PID Controller
          new PIDController(8.0, 0, 0), // Left/Right Trajectory Tracking PID Controller
          new ProfiledPIDController(
              8.0,
              0,
              0, // Rotation Trajectory Tracking PID Controller
              new TrapezoidProfile.Constraints(
                  Constants.MAX_ROTATE_SPEED_RAD_PER_SEC * 0.8,
                  Constants.MAX_ROTATE_ACCEL_RAD_PER_SEC_2 * 0.8)));

  ChassisSpeeds desChSpd = new ChassisSpeeds(0, 0, 0);

  Pose2d curDesPose = new Pose2d();

  private DrivetrainControl() {

    hdc.setEnabled(true);

    moduleFL =
        new SwerveModuleControl(
            "FL",
            Constants.FL_WHEEL_MOTOR_CANID,
            Constants.FL_AZMTH_MOTOR_CANID,
            Constants.FL_AZMTH_ENC_IDX);
    moduleFR =
        new SwerveModuleControl(
            "FR",
            Constants.FR_WHEEL_MOTOR_CANID,
            Constants.FR_AZMTH_MOTOR_CANID,
            Constants.FR_AZMTH_ENC_IDX);
    moduleBL =
        new SwerveModuleControl(
            "BL",
            Constants.BL_WHEEL_MOTOR_CANID,
            Constants.BL_AZMTH_MOTOR_CANID,
            Constants.BL_AZMTH_ENC_IDX);
    moduleBR =
        new SwerveModuleControl(
            "BR",
            Constants.BR_WHEEL_MOTOR_CANID,
            Constants.BR_AZMTH_MOTOR_CANID,
            Constants.BR_AZMTH_ENC_IDX);

    pe = DrivetrainPoseEstimator.getInstance();
  }

  public void setInputs(double fwdRevCmd, double strafeCmd, double rotateCmd) {
    desChSpd = new ChassisSpeeds(fwdRevCmd, strafeCmd, rotateCmd);
    curDesPose = pe.getEstPose();
  }

  public void setInputs(Trajectory.State desTrajState, Rotation2d desAngle) {
    desChSpd = hdc.calculate(pe.getEstPose(), desTrajState, desAngle);
    curDesPose = new Pose2d(desTrajState.poseMeters.getTranslation(), desAngle);
  }

  public void stop() {
    setInputs(0, 0, 0);
  }

  public void update() {

    SwerveModuleState[] desModState;

    if (Math.abs(desChSpd.vxMetersPerSecond) > 0.01
        | Math.abs(desChSpd.vyMetersPerSecond) > 0.01
        | Math.abs(desChSpd.omegaRadiansPerSecond) > 0.01) {
      // In motion
      desModState = Constants.m_kinematics.toSwerveModuleStates(desChSpd);
    } else {
      // Home Position
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
  }

  public SwerveModuleState[] getModuleActualStates() {
    SwerveModuleState retArr[] = {
      moduleFL.getActualState(),
      moduleFR.getActualState(),
      moduleBL.getActualState(),
      moduleFR.getActualState()
    };
    return retArr;
  }

  public SwerveModuleState[] getModuleDesiredStates() {
    SwerveModuleState retArr[] = {
      moduleFL.getDesiredState(),
      moduleFR.getDesiredState(),
      moduleBL.getDesiredState(),
      moduleFR.getDesiredState()
    };
    return retArr;
  }

  public double getMaxErrorMag() {
    double maxErr = 0;
    // TODO after swerve modules have an azimuth controller, read each one's absolute value to
    // figure out which one is most "misalighned"
    // maxErr = Math.max(maxErr, moduleFL.azmthCtrl.getErrMag_deg());
    // maxErr = Math.max(maxErr, moduleFR.azmthCtrl.getErrMag_deg());
    // maxErr = Math.max(maxErr, moduleBL.azmthCtrl.getErrMag_deg());
    // maxErr = Math.max(maxErr, moduleFR.azmthCtrl.getErrMag_deg());
    return maxErr;
  }

  public Pose2d getCurDesiredPose() {
    return curDesPose;
  }

  public void resetWheelEncoders() {
    // TODO - do we actually need to do anything here? It's not clear to me :(
    // moduleFL.wheelEnc.reset();
    // moduleFR.wheelEnc.reset();
    // moduleBL.wheelEnc.reset();
    // moduleBR.wheelEnc.reset();
  }

  public void updateTelemetry() {
    moduleFL.updateTelemetry();
    moduleFR.updateTelemetry();
    moduleBL.updateTelemetry();
    moduleBR.updateTelemetry();
  }
}
