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

    private final double SHOOTER_GEAR_RATIO = 1.0;
    private final double SHOOTER_WHEEL_MASS_kg = Units.lbsToKilograms(5.0);
    private final double SHOOTER_WHEEL_RADIUS_m = Units.inchesToMeters(6.0);

    DCMotor drivingMotor = DCMotor.getNEO(1);

    @Signal(units="V")
    double appliedVoltage = 0;

    @Signal(units="radPerSec")
    double speed = 0;

    public ShooterSim(){

        double moi = 0.5 * SHOOTER_WHEEL_MASS_kg * SHOOTER_WHEEL_RADIUS_m * SHOOTER_WHEEL_RADIUS_m;
        motorWithRotatingMass = new FlywheelSim(drivingMotor, SHOOTER_GEAR_RATIO, moi);
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
    }

    public double getCurrentDraw_A(){
        return motorWithRotatingMass.getCurrentDrawAmps();
    }
    
}
