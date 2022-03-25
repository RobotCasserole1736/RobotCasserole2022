package frc.wrappers.MotorCtrl.TalonFX;

import com.ctre.phoenix.ErrorCode;
import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.ctre.phoenix.motorcontrol.TalonFXControlMode;
import com.ctre.phoenix.motorcontrol.TalonFXFeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;
import com.ctre.phoenix.sensors.SensorVelocityMeasPeriod;

import edu.wpi.first.math.util.Units;
import frc.wrappers.MotorCtrl.AbstractSimmableMotorController;

public class RealTalonFX extends AbstractSimmableMotorController {

    WPI_TalonFX _talon;

    final int TIMEOUT_MS = 1000;

    final double MAX_VOLTAGE = 14.0;

    //Falcon-500 specific internal encoder conversion factor
    public final double NATIVE_UNITS_PER_REV = 2048.0;
    //CTRE Uses 1023 to represent the full scale voltage
    public final double CMD_PER_V = 1023.0/12.0;


    public RealTalonFX(int can_id){
        _talon = new WPI_TalonFX(can_id);

        boolean success = false;

        _talon.enableVoltageCompensation(true);
        _talon.setNeutralMode(NeutralMode.Coast);

        while(!success){
            var err0 = _talon.configFactoryDefault();
            var err1 = _talon.configNeutralDeadband(0.001);
            var err2 = _talon.configSelectedFeedbackSensor(TalonFXFeedbackDevice.IntegratedSensor,
                                                0, 
                                                TIMEOUT_MS);
            var err3 = _talon.configNominalOutputForward(0, TIMEOUT_MS);
            var err4 = _talon.configNominalOutputReverse(0, TIMEOUT_MS);
            var err5 = _talon.configPeakOutputForward(1,  TIMEOUT_MS);
            var err6 = _talon.configPeakOutputReverse(-1, TIMEOUT_MS);   
            var err7 = _talon.configVelocityMeasurementPeriod(SensorVelocityMeasPeriod.Period_20Ms, TIMEOUT_MS);
            var err8 = _talon.configVelocityMeasurementWindow(16, TIMEOUT_MS);
            var err9 = _talon.configVoltageCompSaturation(MAX_VOLTAGE, TIMEOUT_MS);

            //Reduce CAN bus rates on things we don't quite carea bout
            var err10 = _talon.setStatusFramePeriod(StatusFrameEnhanced.Status_1_General, 21, TIMEOUT_MS); //Applied motor output, faults
            var err11 = _talon.setStatusFramePeriod(StatusFrameEnhanced.Status_2_Feedback0, 61, TIMEOUT_MS); //Position/Velocity
            var err12 = _talon.setStatusFramePeriod(StatusFrameEnhanced.Status_3_Quadrature, 9999, TIMEOUT_MS); // Quadrature - unused
            var err13 = _talon.setStatusFramePeriod(StatusFrameEnhanced.Status_4_AinTempVbat, 200, TIMEOUT_MS); // Includes input supply voltage, which we might care about
            var err14 = _talon.setStatusFramePeriod(StatusFrameEnhanced.Status_8_PulseWidth, 9999, TIMEOUT_MS); // No idea, not used
            var err15 = _talon.setStatusFramePeriod(StatusFrameEnhanced.Status_10_MotionMagic, 9999, TIMEOUT_MS); // Not using motion magic
            var err16 = _talon.setStatusFramePeriod(StatusFrameEnhanced.Status_12_Feedback1, 9999, TIMEOUT_MS); // No external feedback sensors connected
            var err17 = _talon.setStatusFramePeriod(StatusFrameEnhanced.Status_14_Turn_PIDF1, 9999, TIMEOUT_MS); // No no auxilary PID used

            success = (
                err0 == ErrorCode.OK &&
                err1 == ErrorCode.OK &&
                err2 == ErrorCode.OK &&
                err3 == ErrorCode.OK &&
                err4 == ErrorCode.OK &&
                err5 == ErrorCode.OK &&
                err6 == ErrorCode.OK &&
                err7 == ErrorCode.OK &&
                err8 == ErrorCode.OK &&
                err9 == ErrorCode.OK &&
                err10 == ErrorCode.OK &&
                err11 == ErrorCode.OK &&
                err12 == ErrorCode.OK &&
                err13 == ErrorCode.OK &&
                err14 == ErrorCode.OK &&
                err15 == ErrorCode.OK &&
                err16 == ErrorCode.OK &&
                err17 == ErrorCode.OK
            );

            if(!success){
                System.out.println("Configuration Failed, retrying....");
            }

        }
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

        // I don't know why we need to do this, but it makes sim line up with real life.
        p /= 1000;

        //Convert to CTRE Units
        p = ( CMD_PER_V ) *  RevtoCTRENativeUnits(Units.radiansToRotations(p));
        i = ( CMD_PER_V ) *  RevtoCTRENativeUnits(Units.radiansToRotations(i)); //CTRE needs this in * 1000 ms (or * 1 sec)
        d = ( CMD_PER_V ) *  RevtoCTRENativeUnits(Units.radiansToRotations(d)); //CTRE needs this in / 1000 ms (or / 1 sec)

        _talon.config_kP(0, p, TIMEOUT_MS);
        _talon.config_kI(0, i, TIMEOUT_MS);
        _talon.config_kD(0, d, TIMEOUT_MS);
    }


    @Override
    public void setClosedLoopCmd(double velocityCmd_radpersec, double arbFF_V) {
        var arbFF_demand = arbFF_V / 12.0; // CTRE wants this in the range [-1, 1].  TODO use bbattery voltage?
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
        return RevtoCTRENativeUnits(in_rpm) / 600;
    }

    double CTRENativeUnitstoRPM(double in_native){
        return CTRENativeUnitstoRev(in_native) * 600;
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
