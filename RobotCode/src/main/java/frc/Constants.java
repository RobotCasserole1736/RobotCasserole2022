package frc;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.util.Units;
import java.util.Arrays;
import java.util.List;

public class Constants {
  //////////////////////////////////////////////////////////////////
  // Drivetrain Physical
  //////////////////////////////////////////////////////////////////
  public static final double WHEEL_BASE_HALF_WIDTH_M = Units.feetToMeters(1.0);
  public static final double WHEEL_RADIUS_IN = 6.0 / 2.0; // six inch diameter wheels
  public static final double ROBOT_MASS_kg = UnitUtils.lbsToKg(140);
  public static final double ROBOT_MOI_KGM2 =
      1.0
          / 12.0
          * ROBOT_MASS_kg
          * Math.pow((WHEEL_BASE_HALF_WIDTH_M * 2.2), 2)
          * 2; // Model moment of intertia as a square slab slightly bigger than wheelbase with axis
  // through center
  // Drivetrain Performance Mechanical limits
  public static final double MAX_FWD_REV_SPEED_MPS = Units.feetToMeters(12.0);
  public static final double MAX_STRAFE_SPEED_MPS = Units.feetToMeters(8.0);
  public static final double MAX_ROTATE_SPEED_RAD_PER_SEC = Units.degreesToRadians(360.0);
  public static final double MAX_TRANSLATE_ACCEL_MPS2 =
      MAX_FWD_REV_SPEED_MPS / 0.25; // 0-full time of 0.25 second
  public static final double MAX_ROTATE_ACCEL_RAD_PER_SEC_2 =
      MAX_ROTATE_SPEED_RAD_PER_SEC / 0.25; // 0-full time of 0.25 second

  //////////////////////////////////////////////////////////////////
  // Electrical
  //////////////////////////////////////////////////////////////////

  // PWM Bank
  // static public final int UNUSED = 0;
  // static public final int UNUSED = 1;
  // static public final int UNUSED = 2;
  // static public final int UNUSED = 3;
  // static public final int UNUSED = 4;
  // static public final int UNUSED = 5;
  // static public final int UNUSED = 6;
  // static public final int UNUSED = 7;
  // static public final int UNUSED = 8;
  // static public final int UNUSED = 9;

  // DIO Bank
  public static final int FL_AZMTH_ENC_IDX = 0; // TODO - are these actually going to be here?
  public static final int FR_AZMTH_ENC_IDX = 1;
  public static final int BL_AZMTH_ENC_IDX = 2;
  public static final int BR_AZMTH_ENC_IDX = 3;
  // static public final int UNUSED = 4;
  // static public final int UNUSED = 5;
  // static public final int UNUSED = 6;
  // static public final int UNUSED = 7;
  // static public final int UNUSED = 8;
  // static public final int UNUSED = 9;

  // Analog Bank
  // static public final int UNUSED = 0;
  // static public final int UNUSED = 1;
  // static public final int UNUSED = 2;
  // static public final int UNUSED = 3;

  // CAN Bus Addresses - Motors
  // static public final int RESERVED_DO_NOT_USE = 0; // default for most stuff
  // static public final int RESERVED_DO_NOT_USE = 1; // Rev Power Distribution Hub
  public static final int FL_WHEEL_MOTOR_CANID = 2;
  public static final int FL_AZMTH_MOTOR_CANID = 3;
  public static final int FR_WHEEL_MOTOR_CANID = 4;
  public static final int FR_AZMTH_MOTOR_CANID = 5;
  public static final int BL_WHEEL_MOTOR_CANID = 6;
  public static final int BL_AZMTH_MOTOR_CANID = 7;
  public static final int BR_WHEEL_MOTOR_CANID = 8;
  public static final int BR_AZMTH_MOTOR_CANID = 9;
  public static final int SHOOTER_MOTOR_CANID = 10;
  public static final int INTAKE_MOTOR_CANID = 11;
  // static public final int UNUSED = 12;
  // static public final int UNUSED = 13;
  // static public final int UNUSED = 14;
  // static public final int UNUSED = 15;
  // static public final int UNUSED = 16;
  // static public final int UNUSED = 17;

  // Pneumatics Hub
  public static final int INTAKE_SOLENOID = 0;
  // static public final int UNUSED = 1;
  // static public final int UNUSED = 2;
  // static public final int UNUSED = 3;
  // static public final int UNUSED = 4;
  // static public final int UNUSED = 5;
  // static public final int UNUSED = 6;
  // static public final int UNUSED = 7;
  // static public final int UNUSED = 8;
  // static public final int UNUSED = 9;

  //////////////////////////////////////////////////////////////////
  // Nominal Sample Times
  //////////////////////////////////////////////////////////////////
  public static final double Ts = 0.02;
  public static final double SIM_SAMPLE_RATE_SEC = 0.001;

  //////////////////////////////////////////////////////////////////
  // Field Dimensions
  //////////////////////////////////////////////////////////////////
  public static final double FIELD_WIDTH_M = Units.feetToMeters(27.0);
  public static final double FIELD_LENGTH_M = Units.feetToMeters(54.0);
  public static final Translation2d MAX_ROBOT_TRANSLATION =
      new Translation2d(FIELD_LENGTH_M, FIELD_WIDTH_M);
  public static final Translation2d MIN_ROBOT_TRANSLATION = new Translation2d(0.0, 0.0);
  // Assumed starting location of the robot. Auto routines will pick their own location and update
  // this.
  public static final Pose2d DFLT_START_POSE = new Pose2d(3, 3, new Rotation2d(0));

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  ////  Derived Constants
  //// - You can reference how these are calculated, but shouldn't
  ////   have to change them
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // HELPER ORGANIZATION CONSTANTS
  public static final int FL = 0; // Front Left Module Index
  public static final int FR = 1; // Front Right Module Index
  public static final int BL = 2; // Back Left Module Index
  public static final int BR = 3; // Back Right Module Index
  public static final int NUM_MODULES = 4;

  // Internal objects used to track where the modules are at relative to
  // the center of the robot, and all the implications that spacing has.
  public static final List<Translation2d> robotToModuleTL =
      Arrays.asList(
          new Translation2d(Constants.WHEEL_BASE_HALF_WIDTH_M, Constants.WHEEL_BASE_HALF_WIDTH_M),
          new Translation2d(Constants.WHEEL_BASE_HALF_WIDTH_M, -Constants.WHEEL_BASE_HALF_WIDTH_M),
          new Translation2d(-Constants.WHEEL_BASE_HALF_WIDTH_M, Constants.WHEEL_BASE_HALF_WIDTH_M),
          new Translation2d(
              -Constants.WHEEL_BASE_HALF_WIDTH_M, -Constants.WHEEL_BASE_HALF_WIDTH_M));

  public static final List<Transform2d> robotToModuleTF =
      Arrays.asList(
          new Transform2d(robotToModuleTL.get(FL), new Rotation2d(0.0)),
          new Transform2d(robotToModuleTL.get(FR), new Rotation2d(0.0)),
          new Transform2d(robotToModuleTL.get(BL), new Rotation2d(0.0)),
          new Transform2d(robotToModuleTL.get(BR), new Rotation2d(0.0)));

  public static final SwerveDriveKinematics m_kinematics =
      new SwerveDriveKinematics(
          robotToModuleTL.get(FL),
          robotToModuleTL.get(FR),
          robotToModuleTL.get(BL),
          robotToModuleTL.get(BR));
}
