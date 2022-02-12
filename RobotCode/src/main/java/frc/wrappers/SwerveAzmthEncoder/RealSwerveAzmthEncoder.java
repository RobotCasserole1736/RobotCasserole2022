package frc.wrappers.SwerveAzmthEncoder;

import edu.wpi.first.wpilibj.DutyCycleEncoder;
import frc.lib.Signal.Annotations.Signal;

public class RealSwerveAzmthEncoder extends AbstractSwerveAzmthEncoder {

    DutyCycleEncoder dc;

    @Signal(units="Hz")
    double freq;

    public RealSwerveAzmthEncoder(int port){
        dc = new DutyCycleEncoder(port);
        dc.setDistancePerRotation(2 * Math.PI);
    }

    @Override
    public double getRawAngle_rad() {
        freq = dc.getFrequency();//Track this for fault mode detection
        return dc.getDistance();
    }

    
}
