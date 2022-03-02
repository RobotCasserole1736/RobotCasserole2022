package frc.wrappers.SwerveAzmthEncoder;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DutyCycle;
import edu.wpi.first.wpilibj.DutyCycleEncoder;
import frc.lib.Signal.Annotations.Signal;

public class RealSwerveAzmthEncoder extends AbstractSwerveAzmthEncoder {

    DigitalInput m_digitalInput;
    DutyCycle m_dutyCycle;

    @Signal(units="Hz")
    double freq;

    public RealSwerveAzmthEncoder(int port){
        m_digitalInput = new DigitalInput(port);
        m_dutyCycle = new DutyCycle(m_digitalInput);
    }

    @Override
    public double getRawAngle_rad() {
        freq = m_dutyCycle.getFrequency(); //Track this for fault mode detection
        return m_dutyCycle.getOutput() * Math.PI * 2;
    }

    
}
