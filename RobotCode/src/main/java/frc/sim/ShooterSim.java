package frc.sim;

import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.simulation.FlywheelSim;
import frc.Constants;
import frc.lib.Signal.Annotations.Signal;
import frc.wrappers.SimDeviceBanks;
import frc.wrappers.MotorCtrl.Sim.SimSmartMotor;

public class ShooterSim {

    FlywheelSim motorWithRotatingMass;

    SimSmartMotor shooterMotor;

    //One Fairlane
    private final double SHOOTER_WHEEL_BOTTOM_MASS_kg = Units.lbsToKilograms(1.56);
    private final double SHOOTER_WHEEL_BOTTOM_RADIUS_m = Units.inchesToMeters(4.0/2);
    //Two Colsons
    private final double SHOOTER_WHEEL_TOP_MASS_kg = Units.lbsToKilograms(0.64);
    private final double SHOOTER_WHEEL_TOP_RADIUS_m = Units.inchesToMeters(3.5/2);
    //MOI fudge factor cuz friction and wheels and shafts and whatnot
    private final double MOI_FUDGE = 1.8;

    SimQuadratureEncoder shooterEncoder = new SimQuadratureEncoder(Constants.SHOOTER_LAUNCH_ENC_A, Constants.SHOOTER_LAUNCH_ENC_B, 2048, Constants.SHOOTER_LAUNCH_ENC_REV_PER_PULSE);
    private double shooterEncoderShaftPosRev = 0;

    DCMotor drivingMotor = DCMotor.getNEO(1);

    @Signal(units="V")
    double appliedVoltage = 0;

    @Signal(units="radPerSec")
    double speed = 0;

    public ShooterSim(){

        double moi_top = MOI_FUDGE * 0.5 * SHOOTER_WHEEL_TOP_MASS_kg * SHOOTER_WHEEL_TOP_RADIUS_m * SHOOTER_WHEEL_TOP_RADIUS_m;
        double moi_bottom = MOI_FUDGE * 0.5 * SHOOTER_WHEEL_BOTTOM_MASS_kg * SHOOTER_WHEEL_BOTTOM_RADIUS_m * SHOOTER_WHEEL_BOTTOM_RADIUS_m;

        motorWithRotatingMass = new FlywheelSim(drivingMotor, Constants.SHOOTER_GEAR_RATIO, moi_top + moi_bottom);
        shooterMotor = (SimSmartMotor) SimDeviceBanks.getCANDevice(Constants.SHOOTER_MOTOR_CANID);
    }

    public void update(boolean isDisabled, double batteryVoltage){
        shooterMotor.sim_setSupplyVoltage(batteryVoltage);

        if(isDisabled){
            appliedVoltage = 0;
        } else {
            appliedVoltage = shooterMotor.getAppliedVoltage_V();
        }

        motorWithRotatingMass.setInputVoltage(appliedVoltage);

        motorWithRotatingMass.update(Constants.SIM_SAMPLE_RATE_SEC);

        speed = motorWithRotatingMass.getAngularVelocityRadPerSec();
        shooterMotor.sim_setActualVelocity(speed / Constants.SHOOTER_GEAR_RATIO);
        shooterMotor.sim_setCurrent(motorWithRotatingMass.getCurrentDrawAmps());

        shooterEncoderShaftPosRev += Units.radiansToRotations(speed * Constants.SIM_SAMPLE_RATE_SEC);
        shooterEncoder.setShaftPositionRev(shooterEncoderShaftPosRev);
    }

    public double getCurrentDraw_A(){
        return shooterMotor.getCurrent_A();
    }

}
