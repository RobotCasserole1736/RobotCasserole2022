package frc.wrappers.ADXRS453;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;

public class RealADXRS453 extends AbstractADXRS453 {

    ADXRS450_Gyro realGyro;

    public RealADXRS453(){
        realGyro = new ADXRS450_Gyro();
    }

    @Override
    public void reset() {
        realGyro.reset();
    }

    @Override
    public void calibrate() {
        realGyro.calibrate();
    }

    @Override
    public double getRate() {
        return realGyro.getRate();
    }

    @Override
    public double getRawAngle() {
        return realGyro.getAngle();
    }

    @Override
    public boolean isConnected() {
        return realGyro.isConnected();
    }
    
}