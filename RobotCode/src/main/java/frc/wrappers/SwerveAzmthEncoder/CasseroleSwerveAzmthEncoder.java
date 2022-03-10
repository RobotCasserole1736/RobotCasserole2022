package frc.wrappers.SwerveAzmthEncoder;

import edu.wpi.first.math.geometry.Rotation2d;
import frc.UnitUtils;
import frc.lib.Calibration.Calibration;
import frc.lib.Signal.Annotations.Signal;
import frc.robot.Robot;

public class CasseroleSwerveAzmthEncoder  {

    AbstractSwerveAzmthEncoder enc;

    @Signal(units="rad")
    double curAngleRad;

    Calibration mountingOffsetCal;


    public CasseroleSwerveAzmthEncoder(String prefix, int port, double dfltMountingOffset_rad){
        if(Robot.isReal()){
            enc = new RealSwerveAzmthEncoder(port);
        } else {
            enc = new SimSwerveAzmthEncoder(port);
        }
        mountingOffsetCal = new Calibration(prefix + "MountingOffset", "rad", dfltMountingOffset_rad);
    }

    public void update(){
        curAngleRad = UnitUtils.wrapAngleRad( enc.getRawAngle_rad() - mountingOffsetCal.get());
    }

    public double getAngle_rad(){
        return curAngleRad;
    }

    public Rotation2d getRotation2d() {
        return new Rotation2d(this.getAngle_rad());
    }
    
}
