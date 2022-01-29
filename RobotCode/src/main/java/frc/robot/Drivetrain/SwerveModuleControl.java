package frc.robot.Drivetrain;

import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.Timer;
import frc.UnitUtils;
import frc.lib.Signal.Annotations.Signal;
import frc.wrappers.MotorCtrl.CasseroleCANMotorCtrl;
import frc.wrappers.SwerveAzmthEncoder.CasseroleSwerveAzmthEncoder;

class SwerveModuleControl {

    CasseroleCANMotorCtrl wheelMotorCtrl;
    CasseroleCANMotorCtrl azmthMotorCtrl;
    CasseroleSwerveAzmthEncoder azmth_enc;

    SwerveModuleState desState = new SwerveModuleState();
    SwerveModuleState actState = new SwerveModuleState();

    frc.lib.Signal.Signal azmthPosDesSig;
    frc.lib.Signal.Signal azmthPosActSig;

    public AzimuthAngleController azmthCtrl;

    SimpleMotorFeedforward wheelMotorFF;

    @Signal(units = "cmd")
    double wheelMotorCmd;

    public SwerveModuleControl(String posId, int wheelMotorIdx, int azmthMotorIdx, int azmthEncoderIdx){

        wheelMotorCtrl = new CasseroleCANMotorCtrl("wheel"+posId, wheelMotorIdx, CasseroleCANMotorCtrl.CANMotorCtrlType.TALON_FX);
        azmthMotorCtrl = new CasseroleCANMotorCtrl("azmth"+posId, azmthMotorIdx, CasseroleCANMotorCtrl.CANMotorCtrlType.SPARK_MAX);
        azmth_enc = new CasseroleSwerveAzmthEncoder("encoder"+posId, azmthEncoderIdx, 0);
      
        azmthPosDesSig = new frc.lib.Signal.Signal("DtModule_" + posId + "_azmthDes", "deg");
        azmthPosActSig = new frc.lib.Signal.Signal("DtModule_" + posId + "_azmthDes", "deg");

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
        double motorDesSpd_radpersec = UnitUtils.dtLinearSpeedToMotorSpeed_radpersec(desState.speedMetersPerSecond);
        if(azmthCtrl.getInvertWheelCmd()){
            motorDesSpd_radpersec *= -1.0;
        }

        // Send the speed command to the motor controller
        wheelMotorCtrl.setClosedLoopCmd(motorDesSpd_radpersec, wheelMotorFF.calculate(motorDesSpd_radpersec));

        // Update the actual state with measurements from the sensors
        actState.angle = new Rotation2d(azmth_enc.getAngle_rad());
        actState.speedMetersPerSecond = UnitUtils.dtMotorSpeedToLinearSpeed_mps(wheelMotorCtrl.getVelocity_radpersec());

    }

    /**
     * Broadcast signals specific to the visualiation
     */
    public void updateTelemetry(){
        double sampleTime = Timer.getFPGATimestamp(); //TODO - this should actually be coming from the loop timing utility, whenever Lucas finishes it up.

        azmthPosDesSig.addSample(sampleTime, azmthCtrl.getSetpoint_deg());
        azmthPosActSig.addSample(sampleTime, Units.radiansToDegrees(azmth_enc.getAngle_rad()));
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
