package frc.wrappers.ADXRS453;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.util.Units;

public abstract class CasseroleADXRS453 {

    public abstract void reset(double curAngle_deg);
    public abstract void calibrate();
    public abstract double getRate();
    public abstract double getAngle();
    public abstract boolean isConnected();

    public Rotation2d getRotation2d() {
        return new Rotation2d(Units.degreesToRadians(this.getAngle()));
    }
    
}
