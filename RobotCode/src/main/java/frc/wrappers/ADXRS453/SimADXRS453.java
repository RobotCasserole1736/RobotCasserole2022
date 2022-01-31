package frc.wrappers.ADXRS453;

import frc.Constants;
import frc.wrappers.SimDeviceBanks;

public class SimADXRS453 extends AbstractADXRS453 {

    double rate; // in rad/sec
    double angle; // in rad

    public SimADXRS453(){
        SimDeviceBanks.addSPIDevice(this, 0); //todo are we actually on CS 0?
    }

    @Override
    public void reset() {
        rate = 0;
        angle = 0;
    }

    @Override
    public void calibrate() {
        //nothing to do
        System.out.println("Sim Gyro Calibration Completed!");
    }

    @Override
    public double getRate() {
        return rate;
    }

    @Override
    public double getRawAngle() {
        return angle;
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    public void simUpdate(double newRate_radpersec){
        rate = newRate_radpersec; //Sim gyro is inverted
        angle += newRate_radpersec * Constants.SIM_SAMPLE_RATE_SEC;
    }   

    public void simSetAngle(double newAngle_rad){
        rate = 0;
        angle = newAngle_rad;
    }   
    
}
