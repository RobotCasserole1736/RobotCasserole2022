// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.TimedRobot;
import frc.lib.Calibration.CalWrangler;
import frc.lib.LoadMon.CasseroleRIOLoadMonitor;
import frc.lib.Signal.SignalWrangler;
import frc.lib.Webserver2.Webserver2;
import frc.lib.miniNT4.NT4Server;
import frc.robot.Autonomous.Autonomous;


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

  // Autonomous Control Utilities
  Autonomous auto;
  PoseTelemetry pt;


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

    if(Robot.isSimulation()){
      simulationSetup();
    }

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

  }



  
  ///////////////////////////////////////////////////////////////////
  // Common Periodic updates
  ///////////////////////////////////////////////////////////////////
  @Override
  public void robotPeriodic() {

    db.updateDriverView();
    telemetryUpdate();
  }

  private void telemetryUpdate(){
    double time = Timer.getFPGATimestamp();
    pt.update(time);
    SignalWrangler.getInstance().sampleAllSignals(time);
  }


  //////////////////////////////////////////////////////////////////////////////////////////
  // Simulation Support
  //////////////////////////////////////////////////////////////////////////////////////////


  public void simulationSetup(){
    // TODO - add simulation plant
  }

  public void syncSimPoseToEstimate(){
    if(Robot.isSimulation()){
      //TODO update plant pose to match current estimator
      //m_plant.m_dt.resetPose(dt.getCurPoseEst());
    }
  }

  @Override
  public void simulationPeriodic(){

  }


}
