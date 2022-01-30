package frc.robot.Drivetrain;

import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.Timer;
import frc.Constants;
import frc.UnitUtils;
import frc.lib.Signal.Annotations.Signal;
import frc.lib.Webserver2.DashboardConfig.SwerveStateTopicSet;
import frc.wrappers.MotorCtrl.CasseroleCANMotorCtrl;
import frc.wrappers.SwerveAzmthEncoder.CasseroleSwerveAzmthEncoder;

class SwerveModuleControl {

    CasseroleCANMotorCtrl wheelMotorCtrl;
    CasseroleCANMotorCtrl azmthMotorCtrl;
    CasseroleSwerveAzmthEncoder azmth_enc;

    SwerveModuleState desState = new SwerveModuleState();
    SwerveModuleState actState = new SwerveModuleState();

    double motorDesSpd_radpersec;

    frc.lib.Signal.Signal azmthPosDesSig;
    frc.lib.Signal.Signal azmthPosActSig;
    frc.lib.Signal.Signal wheelSpdDesSig;
    frc.lib.Signal.Signal wheelSpdActSig;

    public AzimuthAngleController azmthCtrl;

    SimpleMotorFeedforward wheelMotorFF;

    @Signal(units = "cmd")
    double wheelMotorCmd;

    public SwerveModuleControl(String modName, int wheelMotorIdx, int azmthMotorIdx, int azmthEncoderIdx){

        wheelMotorCtrl = new CasseroleCANMotorCtrl("wheel"+modName, wheelMotorIdx, CasseroleCANMotorCtrl.CANMotorCtrlType.TALON_FX);
        azmthMotorCtrl = new CasseroleCANMotorCtrl("azmth"+modName, azmthMotorIdx, CasseroleCANMotorCtrl.CANMotorCtrlType.SPARK_MAX);
        azmth_enc = new CasseroleSwerveAzmthEncoder("encoder"+modName, azmthEncoderIdx, 0);
      
        azmthPosDesSig = new frc.lib.Signal.Signal(SwerveStateTopicSet.PREFIX + modName + SwerveStateTopicSet.SUFFIX_AZMTH_DES, "deg");
        azmthPosActSig = new frc.lib.Signal.Signal(SwerveStateTopicSet.PREFIX + modName + SwerveStateTopicSet.SUFFIX_AZMTH_ACT, "deg");
        wheelSpdDesSig = new frc.lib.Signal.Signal(SwerveStateTopicSet.PREFIX + modName + SwerveStateTopicSet.SUFFIX_WHEEL_DES, "RPM");
        wheelSpdActSig = new frc.lib.Signal.Signal(SwerveStateTopicSet.PREFIX + modName + SwerveStateTopicSet.SUFFIX_WHEEL_ACT, "RPM");

        azmthCtrl = new AzimuthAngleController();

        wheelMotorFF = new SimpleMotorFeedforward(0, // kS - minimum voltage to see any movement. AKA "overcome stiction"
                                                  0); // kV - Volts required to get one (radian per second) of velocity in steady state

    }

    public void update(double curSpeedFtPerSec, double maxAzmthErr_deg){

        azmth_enc.update();

        // Update the azimuth PID controller
        azmthCtrl.setInputs(desState.angle.getDegrees(), 
                            Units.radiansToDegrees(azmth_enc.getAngle_rad()), 
                            curSpeedFtPerSec);
        azmthCtrl.update();

        //Send the output of the controller to the motor, converting from "motor-cmd" to volts as we go
        azmthMotorCtrl.setVoltageCmd(azmthCtrl.getMotorCmd() * 12.0);

        // Calculate the motor speed given the current wheel speed command and whether 
        //  the azimuth is in an "inverting" state.
        motorDesSpd_radpersec = UnitUtils.dtLinearSpeedToMotorSpeed_radpersec(desState.speedMetersPerSecond);
        if(azmthCtrl.getInvertWheelCmd()){
            motorDesSpd_radpersec *= -1.0;
        }

        // Send the speed command to the motor controller
        wheelMotorCtrl.setClosedLoopCmd(motorDesSpd_radpersec, wheelMotorFF.calculate(motorDesSpd_radpersec));

        // Update the actual state with measurements from the sensors
        actState.angle = new Rotation2d(azmth_enc.getAngle_rad());
        actState.speedMetersPerSecond = UnitUtils.dtMotorSpeedToLinearSpeed_mps(wheelMotorCtrl.getVelocity_radpersec());

        wheelMotorCtrl.update();
        azmthMotorCtrl.update();

    }

    /**
     * Broadcast signals specific to the visualiation
     */
    public void updateTelemetry(){
        double sampleTime = Timer.getFPGATimestamp(); //TODO - this should actually be coming from the loop timing utility, whenever Lucas finishes it up.

        azmthPosDesSig.addSample(sampleTime, azmthCtrl.getSetpoint_deg());
        azmthPosActSig.addSample(sampleTime, Units.radiansToDegrees(azmth_enc.getAngle_rad()));
        wheelSpdDesSig.addSample(sampleTime, UnitUtils.dtMotorSpeedToLinearSpeed_mps(motorDesSpd_radpersec)/Constants.MAX_FWD_REV_SPEED_MPS);
        wheelSpdActSig.addSample(sampleTime, UnitUtils.dtMotorSpeedToLinearSpeed_mps(wheelMotorCtrl.getVelocity_radpersec())/Constants.MAX_FWD_REV_SPEED_MPS);
    }

    public void setDesiredState(SwerveModuleState des){
        desState = des;
    }

    public SwerveModuleState getActualState(){
        return actState;
    }

    public SwerveModuleState getDesiredState(){
        return desState;
    }

    public void setClosedLoopGains(double wheel_kP, double wheel_kI, double wheel_kD, double wheel_kV, double wheel_kS, double azmth_kP, double azmth_kI, double azmth_kD){
        wheelMotorCtrl.setClosedLoopGains(wheel_kP, wheel_kI, wheel_kD);
        wheelMotorFF = new SimpleMotorFeedforward(wheel_kS, wheel_kV);
        azmthCtrl.setGains(azmth_kP, azmth_kI, azmth_kD);
    }

    public void resetWheelEncoder(){
        wheelMotorCtrl.resetDistance();
    }

}
