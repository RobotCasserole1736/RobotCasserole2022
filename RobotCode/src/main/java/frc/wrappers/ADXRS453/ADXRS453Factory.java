package frc.wrappers.ADXRS453;

import frc.robot.Robot;

public class ADXRS453Factory {

    static SimADXRS453 simRef;

    public static CasseroleADXRS453 makeNewGyro(){
        if(Robot.isReal()){
            return new RealADXRS453();
        } else {
            simRef = new SimADXRS453();
            return simRef;
        }
    }

    public static SimADXRS453 getSimGyro(){
        return simRef;
    }
}
