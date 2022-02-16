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

    private final double SHOOTER_GEAR_RATIO = 3.0/2.0;
    //One Fairlane
    private final double SHOOTER_WHEEL_BOTTOM_MASS_kg = Units.lbsToKilograms(1.56);
    private final double SHOOTER_WHEEL_BOTTOM_RADIUS_m = Units.inchesToMeters(4.0/2);
    //Two Colsons
    private final double SHOOTER_WHEEL_TOP_MASS_kg = Units.lbsToKilograms(0.64);
    private final double SHOOTER_WHEEL_TOP_RADIUS_m = Units.inchesToMeters(3.5/2);


    DCMotor drivingMotor = DCMotor.getNEO(1);

    @Signal(units="V")
    double appliedVoltage = 0;

    @Signal(units="radPerSec")
    double speed = 0;

    public ShooterSim(){

        double moi_top = 0.5 * SHOOTER_WHEEL_TOP_MASS_kg * SHOOTER_WHEEL_TOP_RADIUS_m * SHOOTER_WHEEL_TOP_RADIUS_m;
        double moi_bottom = 0.5 * SHOOTER_WHEEL_BOTTOM_MASS_kg * SHOOTER_WHEEL_BOTTOM_RADIUS_m * SHOOTER_WHEEL_BOTTOM_RADIUS_m;

        motorWithRotatingMass = new FlywheelSim(drivingMotor, SHOOTER_GEAR_RATIO, moi_top + moi_bottom);
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
        shooterMotor.sim_setActualVelocity(speed * SHOOTER_GEAR_RATIO);
        shooterMotor.sim_setCurrent(motorWithRotatingMass.getCurrentDrawAmps());
    }

    public double getCurrentDraw_A(){
        return shooterMotor.getCurrent_A();
    }

}
