// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import frc.Constants;
import frc.lib.Calibration.CalWrangler;
import frc.lib.LoadMon.CasseroleRIOLoadMonitor;
import frc.lib.Signal.SignalWrangler;
import frc.lib.Webserver2.Webserver2;
import frc.lib.miniNT4.NT4Server;
import frc.robot.Autonomous.Autonomous;
import frc.robot.Drivetrain.DrivetrainControl;
import frc.sim.RobotModel;
import frc.wrappers.MotorCtrl.CasseroleCANMotorCtrl;
import frc.wrappers.SwerveAzmthEncoder.CasseroleSwerveAzmthEncoder;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {

  ///////////////////////////////////////////////////////////////////
  // Instatntiate new classes after here
  // ...

  // Website utilities
  Webserver2 webserver;
  Dashboard db;

  // Things
  CasseroleRIOLoadMonitor loadMon;

  // DriverInput
  DriverInput di;

  // Drivetrain and drivetrain accessories
  DrivetrainControl dt;

  // Autonomous Control Utilities
  Autonomous auto;
  PoseTelemetry pt;

  // TEMPORARY OBJECTS
  // These are just here to keep the sim happy while we test
  // They should be deleted/moved/modified/whatever as the drivetrain or whateverclasses are
  // actually developed
  CasseroleCANMotorCtrl fl_wheel =
      new CasseroleCANMotorCtrl(
          "FL_Wheel",
          Constants.FL_WHEEL_MOTOR_CANID,
          CasseroleCANMotorCtrl.CANMotorCtrlType.TALON_FX);
  CasseroleCANMotorCtrl fl_azmth =
      new CasseroleCANMotorCtrl(
          "FL_Azmth",
          Constants.FL_AZMTH_MOTOR_CANID,
          CasseroleCANMotorCtrl.CANMotorCtrlType.SPARK_MAX);
  CasseroleCANMotorCtrl fr_wheel =
      new CasseroleCANMotorCtrl(
          "FR_Wheel",
          Constants.FR_WHEEL_MOTOR_CANID,
          CasseroleCANMotorCtrl.CANMotorCtrlType.TALON_FX);
  CasseroleCANMotorCtrl fr_azmth =
      new CasseroleCANMotorCtrl(
          "FR_Azmth",
          Constants.FR_AZMTH_MOTOR_CANID,
          CasseroleCANMotorCtrl.CANMotorCtrlType.SPARK_MAX);
  CasseroleCANMotorCtrl bl_wheel =
      new CasseroleCANMotorCtrl(
          "BL_Wheel",
          Constants.BL_WHEEL_MOTOR_CANID,
          CasseroleCANMotorCtrl.CANMotorCtrlType.TALON_FX);
  CasseroleCANMotorCtrl bl_azmth =
      new CasseroleCANMotorCtrl(
          "BL_Azmth",
          Constants.BL_AZMTH_MOTOR_CANID,
          CasseroleCANMotorCtrl.CANMotorCtrlType.SPARK_MAX);
  CasseroleCANMotorCtrl br_wheel =
      new CasseroleCANMotorCtrl(
          "BR_Wheel",
          Constants.BR_WHEEL_MOTOR_CANID,
          CasseroleCANMotorCtrl.CANMotorCtrlType.TALON_FX);
  CasseroleCANMotorCtrl br_azmth =
      new CasseroleCANMotorCtrl(
          "BR_Azmth",
          Constants.BR_AZMTH_MOTOR_CANID,
          CasseroleCANMotorCtrl.CANMotorCtrlType.SPARK_MAX);
  CasseroleSwerveAzmthEncoder fl_azmth_enc =
      new CasseroleSwerveAzmthEncoder("FL", Constants.FL_AZMTH_ENC_IDX, 0);
  CasseroleSwerveAzmthEncoder fr_azmth_enc =
      new CasseroleSwerveAzmthEncoder("FR", Constants.FR_AZMTH_ENC_IDX, 0);
  CasseroleSwerveAzmthEncoder bl_azmth_enc =
      new CasseroleSwerveAzmthEncoder("BL", Constants.BL_AZMTH_ENC_IDX, 0);
  CasseroleSwerveAzmthEncoder br_azmth_enc =
      new CasseroleSwerveAzmthEncoder("BR", Constants.BR_AZMTH_ENC_IDX, 0);

  CasseroleCANMotorCtrl intakeMotor =
      new CasseroleCANMotorCtrl(
          "Intake", Constants.INTAKE_MOTOR_CANID, CasseroleCANMotorCtrl.CANMotorCtrlType.SPARK_MAX);
  CasseroleCANMotorCtrl shooterMotor =
      new CasseroleCANMotorCtrl(
          "Shooter",
          Constants.SHOOTER_MOTOR_CANID,
          CasseroleCANMotorCtrl.CANMotorCtrlType.SPARK_MAX);

  // ...
  // But before here
  ///////////////////////////////////////////////////////////////////

  ///////////////////////////////////////////////////////////////////
  // Do one-time initilization here
  ///////////////////////////////////////////////////////////////////
  @Override
  public void robotInit() {

    NT4Server.getInstance(); // Ensure it starts

    /* Init website utilties */
    webserver = new Webserver2();
    CalWrangler.getInstance();
    pt = PoseTelemetry.getInstance();
    db = new Dashboard(webserver);

    auto = Autonomous.getInstance();

    loadMon = new CasseroleRIOLoadMonitor();

    di = new DriverInput();

    dt = DrivetrainControl.getInstance();

    if (Robot.isSimulation()) {
      simulationSetup();
    }

    // Autonomous might overwrite this, but pick a default starting pose for now?
    // Maybe this is unnecessary?
    dt.pe.setKnownPose(Constants.DFLT_START_POSE);

    SignalWrangler.getInstance().registerSignals(this);
    webserver.startServer();
  }

  ///////////////////////////////////////////////////////////////////
  // Autonomous-Specific
  ///////////////////////////////////////////////////////////////////
  @Override
  public void autonomousInit() {
    SignalWrangler.getInstance().logger.startLoggingAuto();

    // Reset sequencer
    auto.reset();
    auto.startSequencer();

    // Ensure simulation resets to correct pose at the start of autonomous
    syncSimPoseToEstimate();
  }

  @Override
  public void autonomousPeriodic() {
    // Step the sequencer forward
    auto.update();
  }

  ///////////////////////////////////////////////////////////////////
  // Teleop-Specific
  ///////////////////////////////////////////////////////////////////
  @Override
  public void teleopInit() {
    SignalWrangler.getInstance().logger.startLoggingTeleop();
  }

  @Override
  public void teleopPeriodic() {

    di.update();

    // TEMPORARY LOGIC to send some voltages to motors
    // This isn't correct and should be done inside the proper classes
    // But, just for demonstration, for now.... here it be.
    fl_wheel.setVoltageCmd(di.getFwdRevCmd() * 12.0);
    fr_wheel.setVoltageCmd(di.getFwdRevCmd() * 12.0);
    bl_wheel.setVoltageCmd(di.getFwdRevCmd() * 12.0);
    br_wheel.setVoltageCmd(di.getFwdRevCmd() * 12.0);

    fl_azmth.setVoltageCmd(di.getRotateCmd() * 12.0);
    fr_azmth.setVoltageCmd(di.getRotateCmd() * 12.0);
    bl_azmth.setVoltageCmd(di.getRotateCmd() * 12.0);
    br_azmth.setVoltageCmd(di.getRotateCmd() * 12.0);

    intakeMotor.setVoltageCmd(di.getSideToSideCmd() * 12.0);
    shooterMotor.setVoltageCmd(di.getSideToSideCmd() * 12.0);
  }

  ///////////////////////////////////////////////////////////////////
  // Disabled-Specific
  ///////////////////////////////////////////////////////////////////
  @Override
  public void disabledInit() {
    SignalWrangler.getInstance().logger.stopLogging();
  }

  @Override
  public void disabledPeriodic() {}

  ///////////////////////////////////////////////////////////////////
  // Common Periodic updates
  ///////////////////////////////////////////////////////////////////
  @Override
  public void robotPeriodic() {

    dt.update();

    db.updateDriverView();
    telemetryUpdate();
  }

  private void telemetryUpdate() {
    double time = Timer.getFPGATimestamp();

    dt.updateTelemetry();

    pt.setDesiredPose(dt.getCurDesiredPose());

    // TODO - send drivetrain pose estimate to the pt (pose telemetry) object

    pt.update(time);
    SignalWrangler.getInstance().sampleAllSignals(time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  // Simulation Support
  //////////////////////////////////////////////////////////////////////////////////////////

  RobotModel plant;

  public void simulationSetup() {
    plant = new RobotModel();
  }

  public void syncSimPoseToEstimate() {
    if (Robot.isSimulation()) {
      // TODO update plant pose to match current estimate
      // This needs the drivetrain pose estimator class functional first before it works
      // plant.reset(dt.getCurPoseEst());

    }
  }

  @Override
  public void simulationPeriodic() {
    plant.update(this.isDisabled());
    pt.setActualPose(plant.getCurActPose());
  }
}
