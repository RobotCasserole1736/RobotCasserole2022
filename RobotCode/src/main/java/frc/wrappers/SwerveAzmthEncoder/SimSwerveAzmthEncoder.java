package frc.wrappers.SwerveAzmthEncoder;

import frc.wrappers.SimDeviceBanks;

public class SimSwerveAzmthEncoder extends AbstractSwerveAzmthEncoder {
 
    double curAngle_rad;

    double STEPS_PER_REV = 4096.0; //Simulate quantization

    public SimSwerveAzmthEncoder(int port){
        SimDeviceBanks.addDIDevice(this, port);
    }

    public void setRawAngle(double curAngle_rad) {
        this.curAngle_rad = curAngle_rad;
    }

    @Override
    public double getRawAngle_rad() {
        return Math.round(curAngle_rad * STEPS_PER_REV/2/Math.PI) * 2*Math.PI/STEPS_PER_REV;
    }
    
}
