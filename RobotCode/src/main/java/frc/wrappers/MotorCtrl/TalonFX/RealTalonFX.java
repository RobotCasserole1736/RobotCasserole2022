package frc.wrappers.MotorCtrl.TalonFX;

import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.TalonFXControlMode;
import com.ctre.phoenix.motorcontrol.TalonFXFeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;

import edu.wpi.first.math.util.Units;
import frc.wrappers.MotorCtrl.AbstractSimmableMotorController;

public class RealTalonFX extends AbstractSimmableMotorController {

    WPI_TalonFX _talon;

    final int TIMEOUT_MS = 30;

    final double MAX_VOLTAGE = 14.0;

    //Falcon-500 specific internal encoder conversion factor
    public final double NATIVE_UNITS_PER_REV = 2048.0;
    //CTRE Uses 1023 to represent the full scale voltage
    public final double CMD_PER_V = 1023.0/12.0;


    public RealTalonFX(int can_id){
        _talon = new WPI_TalonFX(can_id);
        _talon.configFactoryDefault();
        _talon.configNeutralDeadband(0.001);
        _talon.configSelectedFeedbackSensor(TalonFXFeedbackDevice.IntegratedSensor,
                                               0, 
                                               TIMEOUT_MS);
        _talon.configNominalOutputForward(0, TIMEOUT_MS);
        _talon.configNominalOutputReverse(0, TIMEOUT_MS);
        _talon.configPeakOutputForward(1,  TIMEOUT_MS);
        _talon.configPeakOutputReverse(-1, TIMEOUT_MS);
        _talon.enableVoltageCompensation(true);
        _talon.configVoltageCompSaturation(MAX_VOLTAGE, TIMEOUT_MS);
    }


    @Override
    public void setInverted(boolean invert) {
        _talon.setInverted(invert);
    }


    @Override
    public void setClosedLoopGains(double p, double i, double d) {
        //Incoming units:
        // p = volts / (rad/sec error)
        // i = volts / (rad/sec error) * seconds
        // d = volts / (rad/sec error) / seconds

        //Convert to CTRE Units
        p = ( CMD_PER_V ) *  (1/NATIVE_UNITS_PER_REV) * Units.radiansToRotations(p);
        i = ( CMD_PER_V ) *  (1/NATIVE_UNITS_PER_REV) * Units.radiansToRotations(i); //CTRE needs this in * 1000 ms (or * 1 sec)
        d = ( CMD_PER_V ) *  (1/NATIVE_UNITS_PER_REV) * Units.radiansToRotations(d); //CTRE needs this in / 1000 ms (or / 1 sec)

        _talon.config_kP(0, p, TIMEOUT_MS);
        _talon.config_kI(0, i, TIMEOUT_MS);
        _talon.config_kD(0, d, TIMEOUT_MS);
    }


    @Override
    public void setClosedLoopCmd(double velocityCmd_radpersec, double arbFF_V) {
        var arbFF_demand = arbFF_V * 12.0; //TODO - base off of battery voltage rather than assuming 12.0
        var velCmdRPM = Units.radiansPerSecondToRotationsPerMinute(velocityCmd_radpersec);
        _talon.set(TalonFXControlMode.Velocity, RPMtoCTRENativeUnits(velCmdRPM), DemandType.ArbitraryFeedForward, arbFF_demand); 
    }


    @Override
    public void setVoltageCmd(double cmd_v) {
        var pctCmd = cmd_v/MAX_VOLTAGE;

        if(pctCmd > 1.0){
            pctCmd = 1.0;
        }

        if(pctCmd < -1.0){
            pctCmd = -1.0;
        }

        _talon.set(TalonFXControlMode.PercentOutput, pctCmd);
    }


    @Override
    public double getCurrent_A() {
        return _talon.getStatorCurrent();
    }


    @Override
    public double getVelocity_radpersec() {
        var velRPM = CTRENativeUnitstoRPM(_talon.getSelectedSensorVelocity(0));
        return Units.rotationsPerMinuteToRadiansPerSecond(velRPM);
    }


    @Override
    public void follow(Object leader) {
        if(leader.getClass() == RealTalonFX.class){
            _talon.follow(((RealTalonFX)leader)._talon);
        } else {
            throw new IllegalArgumentException(leader.getClass().toString() + " cannot be followed by a " + RealTalonFX.class.toString());
        }
    }

    @Override
    public double getPosition_rad() {
        var posRev = CTRENativeUnitstoRev(_talon.getSelectedSensorPosition(0));
        return posRev * 2 * Math.PI;
    }

    @Override
    public double getAppliedVoltage_V() {
        return _talon.getMotorOutputVoltage();
    }

    double RPMtoCTRENativeUnits(double in_rpm){
        return in_rpm * NATIVE_UNITS_PER_REV / 600.0;
    }

    double CTRENativeUnitstoRPM(double in_native){
        return in_native / NATIVE_UNITS_PER_REV * 600.0;
    }

    double RevtoCTRENativeUnits(double in_rev){
        return in_rev * NATIVE_UNITS_PER_REV ;
    }

    double CTRENativeUnitstoRev(double in_native){
        return in_native / NATIVE_UNITS_PER_REV;
    }

    @Override
    public void resetDistance() {
        _talon.setSelectedSensorPosition(0,0,50);
    }

    
    
}
