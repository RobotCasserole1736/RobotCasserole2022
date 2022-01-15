package frc;


public class Constants {
    //////////////////////////////////////////////////////////////////
    // Drivetrain Physical
    //////////////////////////////////////////////////////////////////

    // Max achievable speed/rotation
    public static final double kMaxSpeed = 3.0; // 3 meters per second.
    public static final double kMaxAngularSpeed = Math.PI; // 1/2 rotation per second.

    public static final double kTrackWidth_in = 25.0;
    public static final double kWheelDiameter_in = 6.0;

    public static final double kDtGearRatio = 1.0/ 12.0;

    //////////////////////////////////////////////////////////////////
    // Electrical
    //////////////////////////////////////////////////////////////////
    public static final int kDtLeftLeaderCAN_ID    = 1;
    public static final int kDtLeftFollowerCAN_ID  = 2;
    public static final int kDtRightLeaderCAN_ID   = 3;
    public static final int kDtRightFollowerCAN_ID = 4;
    public static final double kMaxDtAppliedVoltage = 10.0;


    //////////////////////////////////////////////////////////////////
    // Nominal Sample Times
    //////////////////////////////////////////////////////////////////
    public static final double Ts = 0.02;

   
}
