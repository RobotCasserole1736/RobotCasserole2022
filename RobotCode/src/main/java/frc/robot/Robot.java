// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Tracer;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.TimedRobot;
import frc.Constants;
import frc.lib.Calibration.CalWrangler;
import frc.lib.LoadMon.CasseroleRIOLoadMonitor;
import frc.lib.LoadMon.SegmentTimeTracker;
import frc.lib.Signal.SignalWrangler;
import frc.lib.Signal.Annotations.Signal;
import frc.lib.Webserver2.Webserver2;
import frc.lib.miniNT4.NT4Server;
import frc.robot.Autonomous.Autonomous;
import frc.robot.Drivetrain.DrivetrainControl;
import frc.sim.RobotModel;


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
  //BatteryMonitor batMan;
  //Ballcolordetector bcd;

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

  RobotAngle angle;

  // Autonomous Control Utilities
  Autonomous auto;
  PoseTelemetry pt;

  LEDController ledCont;

  PneumaticsSupplyControl PSC;

  @Signal
  double loopDurationSec;
  double startTimeSec;

  @Signal
  double loopPeriodSec;

  @Signal (units="sec")
  double elapsedTime;

  SegmentTimeTracker stt = new SegmentTimeTracker("Robot.java", 0.03);


  // ... 
  // But before here
  ///////////////////////////////////////////////////////////////////


  ///////////////////////////////////////////////////////////////////
  // Do one-time initilization here
  ///////////////////////////////////////////////////////////////////
  @Override
  public void robotInit() {

    stt.start();

    // Disable default behavior of the live-window output manipulation logic
    // We've got our own and never use this anyway.
    LiveWindow.setEnabled(false);
    LiveWindow.disableAllTelemetry();
    stt.mark("LW Disable");

    NT4Server.getInstance(); // Ensure it starts
    stt.mark("NT4");


    /* Init website utilties */
    webserver = new Webserver2();
    stt.mark("Webserver2");

    CalWrangler.getInstance();
    stt.mark("Cal Wrangler");

    pt = PoseTelemetry.getInstance();
    stt.mark("Pose Telemetry");

    db = new Dashboard(webserver);
    stt.mark("Dashboard");

    loadMon = new CasseroleRIOLoadMonitor();
    stt.mark("RIO Load Monitor");

    //batMan = BatteryMonitor.getInstance();
    stt.mark("Battery Monitor");

    climb = Climber.getInstance();
    stt.mark("Climber");

    //bcd = new Ballcolordetector();
    stt.mark("Ball Color Detector");

    di = DriverInput.getInstance();
    oi = OperatorInput.getInstance();
    stt.mark("Driver IO");

    dt = DrivetrainControl.getInstance();
    stt.mark("Drivetrain Control");

    angle = RobotAngle.getInstance();
    stt.mark("Robot Angle");

    in = Intake.getInstance();
    stt.mark("Intake");


    shooter = Shooter.getInstance();
    stt.mark("Shooter");

    elevator = Elevator.getInstance();
    stt.mark("Elevator");


    auto = Autonomous.getInstance();
    auto.loadSequencer();
    stt.mark("Autonomous");


    if(Robot.isSimulation()){
      simulationSetup();
    }
    syncSimPoseToEstimate();
    stt.mark("Simulation");


    ledCont = LEDController.getInstance();
    stt.mark("LED Control");

    PSC = new PneumaticsSupplyControl();
    stt.mark("Pneumatics Supply Control");

    SignalWrangler.getInstance().registerSignals(this);
    stt.mark("Signal Registration");

    webserver.startServer();
    stt.mark("Webserver Startup");

    System.out.println("Init Stats:");
    stt.end();

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

    stt.start();

    //Step the sequencer forward
    auto.update();
    stt.mark("Auto Update");

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
    stt.start();

    di.update();
    stt.mark("Driver Input");

    oi.update();
    stt.mark("Operator Input");

    PSC.setCompressorEnabledCmd(di.getCompressorEnabledCmd());

    double fwdRevSpdCmd_mps = di.getFwdRevCmd_mps();
    double leftRightSpdCmd_mps = di.getSideToSideCmd_mps();
    double rotateCmd_radpersec = di.getRotateCmd_rps();

    if(!di.getRobotRelative()){ //temp, use robot relative by default
      dt.setCmdRobotRelative(fwdRevSpdCmd_mps, leftRightSpdCmd_mps, rotateCmd_radpersec);
    } else {
      dt.setCmdFieldRelative(fwdRevSpdCmd_mps, leftRightSpdCmd_mps, rotateCmd_radpersec);
    }

    if(di.getFeedShooter())
      shooter.setFeed(Shooter.shooterFeedCmdState.FEED);
    else
      shooter.setFeed(Shooter.shooterFeedCmdState.STOP);
    shooter.setRun(di.getRunShooter());

    stt.mark("Human Input Mapping");


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
    stt.start();

    dt.calUpdate(false);
    shooter.calUpdate(false);
    stt.mark("Cal Updates");

    auto.sampleDashboardSelector();
    stt.mark("Auto Mode Update");

  }



  
  ///////////////////////////////////////////////////////////////////
  // Common Periodic updates
  ///////////////////////////////////////////////////////////////////
  @Override
  public void robotPeriodic() {
    angle.update();
    stt.mark("AngleMeasurement");

    //bcd.update();
    stt.mark("BallColorDetector");
    
    in.update();
    stt.mark("Intake");

    shooter.update();
    stt.mark("Shooter");

    elevator.update();
    stt.mark("Elevator");

    
    climb.update();
    stt.mark("Climber");


    if(DriverStation.isTest() && !DriverStation.isDisabled()){
      dt.testUpdate();
    } else {
      dt.update();
    }
    stt.mark("Drivetrain");



    db.updateDriverView();
    stt.mark("Dashboard");
    telemetryUpdate();
    stt.mark("Telemetry");

    elapsedTime = Timer.getFPGATimestamp();

    stt.end();
  }

  private void telemetryUpdate(){
    double time = Timer.getFPGATimestamp();

    dt.updateTelemetry();

    pt.setDesiredPose(dt.getCurDesiredPose());
    pt.setEstimatedPose(dt.getCurEstPose());
    
    pt.update(time);
    //batMan.update();
    SignalWrangler.getInstance().sampleAllSignals(time);
  }

  ///////////////////////////////////////////////////////////////////
  // Test-Mode-Specific
  ///////////////////////////////////////////////////////////////////

  @Override
  public void testInit(){
    // Tell the subsystems that care that we're entering test mode.
    dt.testInit();
  }

  @Override
  public void testPeriodic(){
    stt.start();

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
