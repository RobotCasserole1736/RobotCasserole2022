package frc.sim;

import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.simulation.FlywheelSim;
import edu.wpi.first.wpilibj.simulation.REVPHSim;
import frc.Constants;
import frc.UnitUtils;
import frc.lib.Signal.Annotations.Signal;
import frc.wrappers.SimDeviceBanks;
import frc.wrappers.MotorCtrl.Sim.SimSmartMotor;

public class IntakeSim {

    // Spinny Parts
    FlywheelSim motorWithRotatingMass;
    SimSmartMotor intakeMotor;
    private final double INTAKE_GEAR_RATIO = 1.0;
    private final double INTAKE_WHEEL_MASS_kg = Units.lbsToKilograms(8.0);
    private final double INTAKE_WHEEL_RADIUS_m = Units.inchesToMeters(1.0);
    DCMotor drivingMotor = DCMotor.getNeo550(1);
    
    @Signal(units="V")
    double appliedVoltage = 0;
    @Signal(units="radPerSec")
    double speed = 0;

    // Pushy Parts
    private final double CYL_STROKE_LEN_M = 0.25;
    private final double CYL_DIAMETER_M = 0.01;
    private final double CYL_NOM_ACTUATION_TIME_SEC = 0.5;
    private final double CYL_NOM_PRESSURE_KPA = UnitUtils.psiTokPa(60.0);
    REVPHSim pneumaticsHub;


    @Signal(units="m")
    double cylPos = 0;

    @Signal(units="lps")
    double cylFlow = 0;


    public IntakeSim(){

        double moi = 0.5 * INTAKE_WHEEL_MASS_kg * INTAKE_WHEEL_RADIUS_m * INTAKE_WHEEL_RADIUS_m;
        motorWithRotatingMass = new FlywheelSim(drivingMotor, INTAKE_GEAR_RATIO, moi);
        intakeMotor = (SimSmartMotor) SimDeviceBanks.getCANDevice(Constants.INTAKE_MOTOR_CANID);

        pneumaticsHub = new REVPHSim();
    }

    public void update(boolean isDisabled, double batteryVoltage, double supplyPressure_kPa){

        // Spinny
        intakeMotor.sim_setSupplyVoltage(batteryVoltage);

        if(isDisabled){
            appliedVoltage = 0;
        } else {
            appliedVoltage = intakeMotor.getAppliedVoltage_V();
        }

        motorWithRotatingMass.setInputVoltage(appliedVoltage);

        motorWithRotatingMass.update(Constants.SIM_SAMPLE_RATE_SEC);

        speed = motorWithRotatingMass.getAngularVelocityRadPerSec();
        intakeMotor.sim_setActualVelocity(speed * INTAKE_GEAR_RATIO);
        intakeMotor.sim_setCurrent(motorWithRotatingMass.getCurrentDrawAmps());

        // Pushy
        boolean shouldExtend = pneumaticsHub.getSolenoidOutput(Constants.INTAKE_SOLENOID) & !isDisabled;
        double speedFrac = supplyPressure_kPa / CYL_NOM_PRESSURE_KPA;
        double travelSpeed_mps = speedFrac * CYL_STROKE_LEN_M / CYL_NOM_ACTUATION_TIME_SEC; 
        if(shouldExtend){
            if(cylPos >= CYL_STROKE_LEN_M){
                travelSpeed_mps = 0; // at endstop
            }
        } else {
            travelSpeed_mps *= -1.0;
            if(cylPos <= 0){
                travelSpeed_mps = 0; // at endstop
            }
        }
        cylPos += travelSpeed_mps * Constants.SIM_SAMPLE_RATE_SEC;
        cylFlow = Math.abs(travelSpeed_mps) * Math.PI * CYL_DIAMETER_M * 1000; //Convert cubic meters to liters

    }

    public double getCurrentDraw_A(){
        return intakeMotor.getCurrent_A();
    }

    public double getCylFlow_lps(){
        return cylFlow;
    }
        
    public double getCylPos_m(){
        return cylPos;
    }
}
