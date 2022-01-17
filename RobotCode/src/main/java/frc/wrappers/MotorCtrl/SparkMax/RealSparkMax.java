package frc.wrappers.MotorCtrl.SparkMax;


import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.SparkMaxPIDController;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.math.util.Units;
import frc.UnitUtils;
import frc.wrappers.MotorCtrl.AbstractSimmableMotorController;


public class RealSparkMax extends AbstractSimmableMotorController {

    private CANSparkMax m_motor;
    private SparkMaxPIDController m_pidController;
    private RelativeEncoder m_encoder;


    public RealSparkMax(int can_id){
        m_motor = new CANSparkMax(can_id, MotorType.kBrushless);
        m_motor.restoreFactoryDefaults();
        m_pidController = m_motor.getPIDController();
        m_encoder = m_motor.getEncoder();
    }


    @Override
    public void setInverted(boolean invert) {
        m_motor.setInverted(invert);
    }


    @Override
    public void setClosedLoopGains(double p, double i, double d) {

        //Convert to Rev units of RPM
        p = Units.radiansPerSecondToRotationsPerMinute(p);
        i = Units.radiansPerSecondToRotationsPerMinute(i);
        d = Units.radiansPerSecondToRotationsPerMinute(d);

        m_pidController.setP(p);
        m_pidController.setI(i);
        m_pidController.setD(d);
        m_pidController.setOutputRange(-1.0, 1.0);
    }


    @Override
    public void setClosedLoopCmd(double velocityCmd_radpersec, double arbFF_V) {

        m_pidController.setReference(Units.radiansPerSecondToRotationsPerMinute(velocityCmd_radpersec), 
                                     CANSparkMax.ControlType.kVelocity,
                                     0,
                                     arbFF_V,
                                     SparkMaxPIDController.ArbFFUnits.kVoltage);
    }


    @Override
    public void setVoltageCmd(double cmd_v) {
        m_motor.setVoltage(cmd_v);
    }


    @Override
    public double getCurrent_A() {
        return m_motor.getOutputCurrent();
    }


    @Override
    public double getVelocity_radpersec() {
        return  Units.degreesToRadians(UnitUtils.RPMtoDegPerSec(m_encoder.getVelocity()));
    }


    @Override
    public void follow(Object leader) {
        if(leader.getClass() == RealSparkMax.class){
            this.m_motor.follow(((RealSparkMax)leader).m_motor);
        } else {
            throw new IllegalArgumentException(leader.getClass().toString() + " cannot be followed by a " + this.getClass().toString());
        }

    }

    @Override
    public double getPosition_rad() {
        return Units.rotationsToRadians(m_encoder.getPosition());
    }


    @Override
    public double getAppliedVoltage_V() {
        return m_motor.getAppliedOutput() * m_motor.getBusVoltage();
    }


}
