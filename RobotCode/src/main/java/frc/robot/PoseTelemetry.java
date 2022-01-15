package frc.robot;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.lib.Signal.Signal;

public class PoseTelemetry {

    /* Singleton infratructure*/
    private static PoseTelemetry inst = null;
    public static synchronized PoseTelemetry getInstance() {
        if (inst == null)
            inst = new PoseTelemetry();
        return inst;
    }
    
    public static Field2d field = new Field2d();
    
    //Desired Position says where path planning logic wants the
    // robot to be at any given time. 
    Signal xPosDesFtSig;
    Signal yPosDesFtSig;
    Signal tRotDesDegSig;

    //Estimated position says where you think your robot is at
    // Based on encoders, motion, vision, etc.
    Signal xPosEstFtSig;
    Signal yPosEstFtSig;
    Signal tRotEstDegSig;

    //Actual position defines wherever the robot is actually at
    // at any time. It is unknowable in real life. The simulation
    // generates this as its primary output.
    Signal xPosActFtSig;
    Signal yPosActFtSig;
    Signal tRotActDegSig;

    Pose2d actualPose = new Pose2d();
    Pose2d desiredPose = new Pose2d();
    Pose2d estimatedPose = new Pose2d();



    private PoseTelemetry(){
        xPosDesFtSig     = new Signal("pose_DES_x", "m");
        yPosDesFtSig     = new Signal("pose_DES_y", "m");
        tRotDesDegSig    = new Signal("pose_DES_rot", "rad");

        xPosEstFtSig     = new Signal("pose_EST_x", "m");
        yPosEstFtSig     = new Signal("pose_EST_y", "m");
        tRotEstDegSig    = new Signal("pose_EST_rot", "rad");

        xPosActFtSig     = new Signal("pose_ACT_x", "m");
        yPosActFtSig     = new Signal("pose_ACT_y", "m");
        tRotActDegSig    = new Signal("pose_ACT_rot", "rad");

        SmartDashboard.putData("Field", field);

    }

    public void setActualPose(Pose2d act){
        actualPose = act;
    }
    public void setDesiredPose(Pose2d des){
        desiredPose = des;
    }
    public void setEstimatedPose(Pose2d est){
        estimatedPose = est;
    }

    public void update(double time){
        xPosActFtSig.addSample(time,  (actualPose.getTranslation().getX()));
        yPosActFtSig.addSample(time,  (actualPose.getTranslation().getY()));
        tRotActDegSig.addSample(time, actualPose.getRotation().getRadians());

        xPosDesFtSig.addSample(time,  (desiredPose.getTranslation().getX()));
        yPosDesFtSig.addSample(time,  (desiredPose.getTranslation().getY()));
        tRotDesDegSig.addSample(time, desiredPose.getRotation().getRadians());

        xPosEstFtSig.addSample(time,  (estimatedPose.getTranslation().getX()));
        yPosEstFtSig.addSample(time,  (estimatedPose.getTranslation().getY()));
        tRotEstDegSig.addSample(time, estimatedPose.getRotation().getRadians());

        field.getObject("DesPose").setPose(desiredPose);
        field.getObject("Robot").setPose(actualPose);
        field.getObject("EstPose").setPose(estimatedPose);
    }

}
