// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.TimedRobot;
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
  BatteryMonitor batMan;

  // DriverInput
  DriverInput di;

  //OperatorInput
  OperatorInput oi;

  // Intake
  Intake in;

  //Shooter
  Shooter shooter;
  //Elevator
  Elevator elevator;

  //Climber
  Climber climb;

  //Drivetrain and drivetrain accessories
  DrivetrainControl dt;

  // Autonomous Control Utilities
  Autonomous auto;
  PoseTelemetry pt;

  LEDController ledCont;
  //TEMPORARY OBJECTS
  // These are just here to keep the sim happy while we test
  // They should be deleted/moved/modified/whatever as the drivetrain or whateverclasses are actually developed

  //CasseroleCANMotorCtrl intakeMotor  = new CasseroleCANMotorCtrl("Intake", Constants.INTAKE_MOTOR_CANID, CasseroleCANMotorCtrl.CANMotorCtrlType.SPARK_MAX);
  //CasseroleCANMotorCtrl shooterMotor = new CasseroleCANMotorCtrl("Shooter", Constants.SHOOTER_MOTOR_CANID, CasseroleCANMotorCtrl.CANMotorCtrlType.SPARK_MAX);


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

    loadMon = new CasseroleRIOLoadMonitor();
    batMan = BatteryMonitor.getInstance();
    climb = Climber.getInstance();

    di = DriverInput.getInstance();

    oi = OperatorInput.getInstance();

    dt = DrivetrainControl.getInstance();

    in = Intake.getInstance();

    shooter = Shooter.getInstance();
    elevator = Elevator.getInstance();

    auto = Autonomous.getInstance();
    auto.loadSequencer();

    if(Robot.isSimulation()){
      simulationSetup();
    }

    ledCont = LEDController.getInstance();
    syncSimPoseToEstimate();

    SignalWrangler.getInstance().registerSignals(this);
    webserver.startServer();
  }


  ///////////////////////////////////////////////////////////////////
  // Autonomous-Specific
  ///////////////////////////////////////////////////////////////////
  @Override
  public void autonomousInit() {
    SignalWrangler.getInstance().logger.startLoggingAuto();

    //Reset sequencer
    auto.reset();
    auto.startSequencer();


    // Ensure simulation resets to correct pose at the start of autonomous
    syncSimPoseToEstimate();

  }

  @Override
  public void autonomousPeriodic() {
    //Step the sequencer forward
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

    oi.update();

    in.update();

    elevator.update();

    climb.update();

    double fwdRevSpdCmd_mps = di.getFwdRevCmd() * Constants.MAX_FWD_REV_SPEED_MPS;
    double leftRightSpdCmd_mps = di.getSideToSideCmd() * Constants.MAX_FWD_REV_SPEED_MPS;
    double rotateCmd_radpersec = di.getRotateCmd() * Constants.MAX_FWD_REV_SPEED_MPS;

    if(di.getRobotRelative()){
      dt.setCmdRobotRelative(fwdRevSpdCmd_mps, leftRightSpdCmd_mps, rotateCmd_radpersec);
    } else {
      dt.setCmdFieldRelative(fwdRevSpdCmd_mps, leftRightSpdCmd_mps, rotateCmd_radpersec);
    }

    shooter.setFeed(oi.getFeedShooter());
    shooter.setRun(oi.getRunShooter());

    //intakeMotor.setVoltageCmd(di.getSideToSideCmd() * 12.0);
    //shooterMotor.setVoltageCmd(di.getSideToSideCmd() * 12.0);

  }



  ///////////////////////////////////////////////////////////////////
  // Disabled-Specific
  ///////////////////////////////////////////////////////////////////
  @Override
  public void disabledInit() {
    SignalWrangler.getInstance().logger.stopLogging();
  }

  @Override
  public void disabledPeriodic() {
    dt.calUpdate(false);

    auto.sampleDashboardSelector();

  }



  
  ///////////////////////////////////////////////////////////////////
  // Common Periodic updates
  ///////////////////////////////////////////////////////////////////
  @Override
  public void robotPeriodic() {

    shooter.update();

    if(DriverStation.isTest() && !DriverStation.isDisabled()){
      dt.testUpdate();
    } else {
      dt.update();
    }

    db.updateDriverView();
    telemetryUpdate();
  }

  private void telemetryUpdate(){
    double time = Timer.getFPGATimestamp();

    dt.updateTelemetry();

    pt.setDesiredPose(dt.getCurDesiredPose());
    pt.setEstimatedPose(dt.getCurEstPose());
    
    pt.update(time);
    batMan.update();
    SignalWrangler.getInstance().sampleAllSignals(time);
  }

  ///////////////////////////////////////////////////////////////////
  // Test-Mode-Specific
  ///////////////////////////////////////////////////////////////////

  @Override
  public void testInit(){
    // Disable default behavior of the live-window output manipulation logic
    // We've got our own and never use this anyway.
    LiveWindow.setEnabled(false);

    // Tell the subsystems that care that we're entering test mode.
    dt.testInit();
  }

  @Override
  public void testPeriodic(){
    // Nothing special here, yet
  }

  ///////////////////////////////////////////////////////////////////
  // Simulation Support
  ///////////////////////////////////////////////////////////////////

  RobotModel plant;

  public void simulationSetup(){
    plant = new RobotModel();
    syncSimPoseToEstimate();
  }

  public void syncSimPoseToEstimate(){
    if(Robot.isSimulation()){
      plant.reset(dt.getCurEstPose());
    }
  }

  @Override
  public void simulationPeriodic(){
    plant.update(this.isDisabled());
    pt.setActualPose(plant.getCurActPose());
  }


}
