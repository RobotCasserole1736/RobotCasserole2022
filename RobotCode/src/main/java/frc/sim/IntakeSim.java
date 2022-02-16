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
    FlywheelSim horizMotorWithRotatingMass;
    FlywheelSim vertMotorWithRotatingMass;
    SimSmartMotor horizIntakeMotor;
    SimSmartMotor vertIntakeMotor;
    private final double INTAKE_GEAR_RATIO = 1.0;
    private final double INTAKE_WHEEL_MASS_kg = Units.lbsToKilograms(8.0);
    private final double INTAKE_WHEEL_RADIUS_m = Units.inchesToMeters(1.0);
    DCMotor drivingMotor = DCMotor.getNeo550(1);
    
    @Signal(units="V")
    double horizAppliedVoltage = 0;
    @Signal(units="radPerSec")
    double horizSpeed = 0;

    @Signal(units="V")
    double vertAppliedVoltage = 0;
    @Signal(units="radPerSec")
    double vertSpeed = 0;


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
        horizMotorWithRotatingMass = new FlywheelSim(drivingMotor, INTAKE_GEAR_RATIO, moi);
        horizIntakeMotor = (SimSmartMotor) SimDeviceBanks.getCANDevice(Constants.HORIZ_INTAKE_MOTOR_CANID);

        vertMotorWithRotatingMass = new FlywheelSim(drivingMotor, INTAKE_GEAR_RATIO, moi);
        vertIntakeMotor = (SimSmartMotor) SimDeviceBanks.getCANDevice(Constants.LEFT_VERT_INTAKE_MOTOR_CANID);

        pneumaticsHub = new REVPHSim();
    }

    public void update(boolean isDisabled, double batteryVoltage, double supplyPressure_kPa){

        // Spinny
        horizIntakeMotor.sim_setSupplyVoltage(batteryVoltage);
        vertIntakeMotor.sim_setSupplyVoltage(batteryVoltage);

        if(isDisabled){
            horizAppliedVoltage = 0;
            vertAppliedVoltage = 0;
        } else {
            horizAppliedVoltage = horizIntakeMotor.getAppliedVoltage_V();
            vertAppliedVoltage = vertIntakeMotor.getAppliedVoltage_V();
        }

        horizMotorWithRotatingMass.setInputVoltage(horizAppliedVoltage);
        vertMotorWithRotatingMass.setInputVoltage(vertAppliedVoltage);

        horizMotorWithRotatingMass.update(Constants.SIM_SAMPLE_RATE_SEC);
        vertMotorWithRotatingMass.update(Constants.SIM_SAMPLE_RATE_SEC);

        horizSpeed = horizMotorWithRotatingMass.getAngularVelocityRadPerSec();
        horizIntakeMotor.sim_setActualVelocity(horizSpeed * INTAKE_GEAR_RATIO);
        horizIntakeMotor.sim_setCurrent(horizMotorWithRotatingMass.getCurrentDrawAmps());

        vertSpeed = vertMotorWithRotatingMass.getAngularVelocityRadPerSec();
        vertIntakeMotor.sim_setActualVelocity(vertSpeed * INTAKE_GEAR_RATIO);
        vertIntakeMotor.sim_setCurrent(vertMotorWithRotatingMass.getCurrentDrawAmps());

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
        return horizIntakeMotor.getCurrent_A() + vertIntakeMotor.getCurrent_A();
    }

    public double getCylFlow_lps(){
        return cylFlow;
    }
        
    public double getCylPos_m(){
        return cylPos;
    }
}
