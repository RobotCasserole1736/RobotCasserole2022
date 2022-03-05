package frc.wrappers.SwerveAzmthEncoder;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DutyCycle;
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
        double pulsetime = m_dutyCycle.getOutput() * (1.0 / freq);
        double anglerad = ((pulsetime - 1E-6) / (4.096E-3 - 1E-6)) * 2 * Math.PI;
        return anglerad;
    }

    
}
