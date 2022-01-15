package frc;

public class UnitUtils{

    
    public static double RPMtoDegPerSec(double rpmIn){ return rpmIn * 60.0 / 360.0; }
    public static double lbsToKg(double lbs_in){return 0.4535924 * lbs_in;}

    public static double wrapAngleDeg(double angle){
        angle %=360;
        angle= angle>180 ? angle-360 : angle;
        angle= angle<-180 ? angle+360 : angle;
        return angle;
    }

    public static double limitMotorCmd(double motorCmdIn){
        return Math.max(Math.min(motorCmdIn,1.0),-1.0);
    }

}