// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.photonvision.PhotonCamera;

import edu.wpi.first.cscore.MjpegServer;
import edu.wpi.first.cscore.UsbCamera;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.util.Units;
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
import frc.robot.Elevator.elevatorCmdState;
import frc.robot.Intake.intakeCmdState;
import frc.robot.Shooter.shooterLaunchState;
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
  BatteryMonitor batMan;
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

  PneumaticsSupplyControl psc;

  SegmentTimeTracker stt;

  @Signal(units = "sec")
  double mainLoopDuration;
  @Signal(units = "sec")
  double mainLoopPeriod;

  final double CAMERA_HEIGHT_METERS = Units.inchesToMeters(24);
  final double TARGET_HEIGHT_METERS = Units.feetToMeters(5);
  // Angle between horizontal and the camera.
  final double CAMERA_PITCH_RADIANS = Units.degreesToRadians(0);

  // How far from the target we want to be
  final double GOAL_RANGE_METERS = Units.feetToMeters(3);

  // Change this to match the name of your camera
  PhotonCamera camera = new PhotonCamera("photonvision");

  final double ANGULAR_P = 0.1;
  final double ANGULAR_D = 0.0;
  PIDController turnController = new PIDController(ANGULAR_P, 0, ANGULAR_D);
  // ... 
  // But before here
  ///////////////////////////////////////////////////////////////////


  ///////////////////////////////////////////////////////////////////
  // Do one-time initilization here
  ///////////////////////////////////////////////////////////////////
  @Override
  public void robotInit() {

    stt = new SegmentTimeTracker("Robot.java", 0.25);

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

    batMan = BatteryMonitor.getInstance();
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

    psc = PneumaticsSupplyControl.getInstance();
    stt.mark("Pneumatics Supply Control");

    SignalWrangler.getInstance().registerSignals(this);
    stt.mark("Signal Registration");

    webserver.startServer();
    stt.mark("Webserver Startup");

    UsbCamera usbCamera = new UsbCamera("USB Camera 0", 0);
    MjpegServer mjpegServer1 = new MjpegServer("serve_USB Camera 0", 1181);
    mjpegServer1.setSource(usbCamera);
    stt.mark("Driver Camera");


    System.out.println("Init Stats:");
    stt.end();

    PhotonCamera.setVersionCheckEnabled(false);
  }


  ///////////////////////////////////////////////////////////////////
  // Autonomous-Specific
  ///////////////////////////////////////////////////////////////////
  @Override
  public void autonomousInit() {
    SignalWrangler.getInstance().logger.startLoggingAuto();

    climb.retractTiltClimber();
    
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

    /////////////////////////////////////
    // Drivetrain Input Mapping, with vision alignment
    double fwdRevSpdCmd_mps = di.getFwdRevCmd_mps();
    double leftRightSpdCmd_mps = di.getSideToSideCmd_mps();
    double rotateCmd_radpersec = 0;

    if (di.getPhotonAlign()) {
      // Vision-alignment mode
      // Query the latest result from PhotonVision
      var result = camera.getLatestResult();

      if (result.hasTargets()) {
          // Calculate angular turn power
          // -1.0 required to ensure positive PID controller effort _increases_ yaw
          rotateCmd_radpersec = turnController.calculate(result.getBestTarget().getYaw(), 0)* Constants.MAX_FWD_REV_SPEED_MPS;
      } else {
          // If we have no targets, stay still.
          rotateCmd_radpersec = 0;
      }
    } else {
        // Manual Driver Mode
        rotateCmd_radpersec = di.getRotateCmd_rps();
    }

    if(!di.getRobotRelative()){ //temp, use robot relative by default
      dt.setCmdRobotRelative(fwdRevSpdCmd_mps, leftRightSpdCmd_mps, rotateCmd_radpersec);
    } else {
      dt.setCmdFieldRelative(fwdRevSpdCmd_mps, leftRightSpdCmd_mps, rotateCmd_radpersec);
    }

    if(di.getOdoResetCmd()){
      //Reset pose estimate to angle 0, but at the same translation we're at
      Pose2d newPose = new Pose2d(dt.getCurEstPose().getTranslation(), new Rotation2d(0.0));
      dt.setKnownPose(newPose);
    }
    

    ////////////////////////////////////////
    // Shooter & Superstructure control
    if(di.getShootHighGoal() || di.getShootLowGoal()){
      // Attempting to Shoot
      if(shooter.getSpooledUp()){
        //At up to speed, allow feed
        shooter.setFeed(Shooter.shooterFeedCmdState.FEED);
        elevator.setCmd(elevatorCmdState.SHOOT);
      } else {
        // Not at speed yet, hold off on feeding
        shooter.setFeed(Shooter.shooterFeedCmdState.STOP);
        elevator.setCmd(elevatorCmdState.INTAKE);
      }

      shooter.setRun(di.getShootHighGoal()?shooterLaunchState.HIGH_GOAL:shooterLaunchState.LOW_GOAL);
    
      in.setCmd(intakeCmdState.STOP); 

    } else {
      // No shoot desired, just collect/store balls

      if(di.getEject() || oi.getEject()){
        // eject everything
        elevator.setCmd(elevatorCmdState.EJECT);
        shooter.setFeed(Shooter.shooterFeedCmdState.EJECT);
        in.setCmd(intakeCmdState.EJECT);
      } else if( (di.getIntakeLowerAndRun() || oi.getIntakeLowerAndRun()) && !elevator.isFull()){
        // Intake
        elevator.setCmd(elevatorCmdState.INTAKE);
        shooter.setFeed(Shooter.shooterFeedCmdState.INTAKE);
        in.setCmd(intakeCmdState.INTAKE);
      } else {
        // Stop
        elevator.setCmd(elevatorCmdState.STOP);
        shooter.setFeed(Shooter.shooterFeedCmdState.STOP);
        in.setCmd(intakeCmdState.STOP);     
      }

      shooter.setRun(shooterLaunchState.STOP);

    }

    
    ////////////////////////////////////////
    // Climber Control
    if(di.getClimbExtend() || oi.getClimbExtend()){
      climb.extendClimber();
    } else if (di.getClimbRetract() || oi.getClimbRetract()) {
      climb.retractClimber();
    } else {
      //maintain state
    }

    if(di.getClimbTilt() || oi.getClimbTilt()){
      climb.extendTiltClimber();
    } else if (di.getClimbStraighten() || oi.getClimbStraighten()) {
      climb.retractTiltClimber();
    } else {
      //maintain state
    }

    psc.setCompressorEnabledCmd(di.getCompressorEnabledCmd());

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

    stt.end();
  }

  private void telemetryUpdate(){
    double time = Timer.getFPGATimestamp();

    dt.updateTelemetry();

    pt.setDesiredPose(dt.getCurDesiredPose());
    pt.setEstimatedPose(dt.getCurEstPose());
    
    pt.update(time);

    mainLoopDuration = stt.loopDurationSec;
    mainLoopPeriod = stt.loopPeriodSec;

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
