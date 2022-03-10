package frc;

import edu.wpi.first.math.util.Units;

public class UnitUtils{

    
    public static double RPMtoDegPerSec(double rpmIn){ return rpmIn * 360 / 60.0; }
    public static double lbsToKg(double lbs_in){return 0.4535924 * lbs_in;}

    public static double kPaToPsi(double kPa_in){return kPa_in / 6.89475729;}
    public static double psiTokPa(double psi_in){return 6.89475729 * psi_in;}

    public static double wrapAngleDeg(double angle){
        angle %=360;
        angle= angle>180 ? angle-360 : angle;
        angle= angle<-180 ? angle+360 : angle;
        return angle;
    }

    public static double wrapAngleRad(double angle){
       return Units.degreesToRadians(wrapAngleDeg(Units.radiansToDegrees(angle)));
    }

    public static double dtLinearSpeedToMotorSpeed_radpersec(double linear_mps_in){
        return linear_mps_in / (Units.inchesToMeters(Constants.WHEEL_RADIUS_IN)) * Constants.WHEEL_GEAR_RATIO;
    }

    public static double dtMotorSpeedToLinearSpeed_mps(double motor_radpersec_in){
        return motor_radpersec_in * (Units.inchesToMeters(Constants.WHEEL_RADIUS_IN)) / Constants.WHEEL_GEAR_RATIO;
    }

    public static double limitMotorCmd(double motorCmdIn){
        return Math.max(Math.min(motorCmdIn,1.0),-1.0);
    }

}