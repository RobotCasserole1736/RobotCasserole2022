// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.TimedRobot;
import frc.Constants;
import frc.lib.Calibration.CalWrangler;
import frc.lib.LoadMon.CasseroleRIOLoadMonitor;
import frc.lib.Signal.SignalWrangler;
import frc.lib.Webserver2.Webserver2;
import frc.lib.miniNT4.NT4Server;
import frc.robot.Autonomous.Autonomous;
import frc.sim.RobotModel;
import frc.wrappers.ADXRS453.CasseroleADXRS453;
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

  // Autonomous Control Utilities
  Autonomous auto;
  PoseTelemetry pt;

  //TEMPORARY DRIVETRAIN OBJECTS
  // These are just here to keep the sim happy while we test
  // They should be deleted/moved/modified/whatever as the drivetrain classes are actually developed
  CasseroleADXRS453 gyro = new CasseroleADXRS453();
  CasseroleCANMotorCtrl fl_wheel = new CasseroleCANMotorCtrl("FL_Wheel", Constants.FL_WHEEL_MOTOR_CANID, CasseroleCANMotorCtrl.CANMotorCtrlType.TALON_FX);
  CasseroleCANMotorCtrl fl_azmth = new CasseroleCANMotorCtrl("FL_Azmth", Constants.FL_AZMTH_MOTOR_CANID, CasseroleCANMotorCtrl.CANMotorCtrlType.SPARK_MAX);
  CasseroleCANMotorCtrl fr_wheel = new CasseroleCANMotorCtrl("FR_Wheel", Constants.FR_WHEEL_MOTOR_CANID, CasseroleCANMotorCtrl.CANMotorCtrlType.TALON_FX);
  CasseroleCANMotorCtrl fr_azmth = new CasseroleCANMotorCtrl("FR_Azmth", Constants.FR_AZMTH_MOTOR_CANID, CasseroleCANMotorCtrl.CANMotorCtrlType.SPARK_MAX);
  CasseroleCANMotorCtrl bl_wheel = new CasseroleCANMotorCtrl("BL_Wheel", Constants.BL_WHEEL_MOTOR_CANID, CasseroleCANMotorCtrl.CANMotorCtrlType.TALON_FX);
  CasseroleCANMotorCtrl bl_azmth = new CasseroleCANMotorCtrl("BL_Azmth", Constants.BL_AZMTH_MOTOR_CANID, CasseroleCANMotorCtrl.CANMotorCtrlType.SPARK_MAX);
  CasseroleCANMotorCtrl br_wheel = new CasseroleCANMotorCtrl("BR_Wheel", Constants.BR_WHEEL_MOTOR_CANID, CasseroleCANMotorCtrl.CANMotorCtrlType.TALON_FX);
  CasseroleCANMotorCtrl br_azmth = new CasseroleCANMotorCtrl("BR_Azmth", Constants.BR_AZMTH_MOTOR_CANID, CasseroleCANMotorCtrl.CANMotorCtrlType.SPARK_MAX);
  CasseroleSwerveAzmthEncoder fl_azmth_enc = new CasseroleSwerveAzmthEncoder("FL", Constants.FL_AZMTH_ENC_IDX, 0);
  CasseroleSwerveAzmthEncoder fr_azmth_enc = new CasseroleSwerveAzmthEncoder("FR", Constants.FR_AZMTH_ENC_IDX, 0);
  CasseroleSwerveAzmthEncoder bl_azmth_enc = new CasseroleSwerveAzmthEncoder("BL", Constants.BL_AZMTH_ENC_IDX, 0);
  CasseroleSwerveAzmthEncoder br_azmth_enc = new CasseroleSwerveAzmthEncoder("BR", Constants.BR_AZMTH_ENC_IDX, 0);

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

  RobotModel plant;

  public void simulationSetup(){
    plant = new RobotModel();
  }

  public void syncSimPoseToEstimate(){
    if(Robot.isSimulation()){
      //TODO update plant pose to match current estimator
      //plant.reset(dt.getCurPoseEst());
      
    }
  }

  @Override
  public void simulationPeriodic(){
    plant.update(this.isDisabled());
  }


}
