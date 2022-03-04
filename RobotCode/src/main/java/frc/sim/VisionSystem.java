package frc.sim;

import org.photonvision.SimVisionSystem;
import org.photonvision.SimVisionTarget;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.util.Units;

public class VisionSystem {

    ////////////////////////////////////////////////////////////////
    // Simulated Vision System.
    // Configure these to match your PhotonVision Camera,
    // pipeline, and LED setup.
    double camDiagFOV = 170.0; // degrees - assume wide-angle camera
    double camPitch = -5.0; // degrees
    double camHeightOffGround = 0.25; // meters
    double maxLEDRange = 20; // meters
    int camResolutionWidth = 640; // pixels
    int camResolutionHeight = 480; // pixels
    double minTargetArea = 10; // square pixels

    SimVisionSystem simVision =
    new SimVisionSystem(
            "photonvision",
            camDiagFOV,
            camPitch,
            new Transform2d(),
            camHeightOffGround,
            maxLEDRange,
            camResolutionWidth,
            camResolutionHeight,
            minTargetArea);

    ////////////////////////////////////////////////////////////////
    // Targets - balls
    // Uhhhhh nine inch ball I guess, whcih we model as a 9x9 square?
    double targetWidth = Units.inchesToMeters(9); // meters
    double targetHeight = Units.inchesToMeters(9); // meters
    //IDK somewhere on the field, let's say (10, 10)
    double tgtXPos = Units.feetToMeters(10);
    double tgtYPos = Units.feetToMeters(10);

    //Hack the fact that the sim vision system models the world as retroreflective targets,
    // but we're actually looking at spheres.
    Pose2d frontHalfOfBall = new Pose2d(new Translation2d(tgtXPos, tgtYPos), new Rotation2d(0.0));
    Pose2d backHalfOfBall = new Pose2d(new Translation2d(tgtXPos, tgtYPos), Rotation2d.fromDegrees(180));

    public VisionSystem(){
        simVision.addSimVisionTarget(new SimVisionTarget(frontHalfOfBall, targetHeight/2, targetWidth, targetHeight));
        simVision.addSimVisionTarget(new SimVisionTarget(backHalfOfBall,  targetHeight/2, targetWidth, targetHeight));
    }

    public void update(Pose2d curPose){
        simVision.processFrame(curPose);
    }
    
}
