package frc.wrappers.ADXRS453;


public abstract class AbstractADXRS453 {

    public abstract void reset();
    public abstract void calibrate();
    public abstract double getRate();
    public abstract double getRawAngle();
    public abstract boolean isConnected();
    
}
