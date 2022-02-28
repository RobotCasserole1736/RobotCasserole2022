package frc.sim;

import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.simulation.FlywheelSim;
import frc.Constants;
import frc.lib.Signal.Annotations.Signal;

class SimpleMotorWithMassModel {

    @Signal(units="RPM")
    double speedAct_RPM;
    @Signal(units="A")
    double current_A;
    @Signal(units="rev")
    double curDisplacement_Rev;

    FlywheelSim fwSim;
    double gearing;

    boolean invertFlywheel = false;

    public SimpleMotorWithMassModel(DCMotor motor, double gearing, double moi){
        if(gearing <= 0){
            invertFlywheel = true;
            gearing *= -1.0;
        }
        fwSim = new FlywheelSim(motor, gearing, moi);
        this.gearing = gearing;
    }

    /**
     * Set the motor back to zero speed and zero current draw.
     */
    public void modelReset(){
        
    }

    /**
     * Step through one loop of simulation for the motor
     * @param supplyVoltage_in Present battery supply voltage to the controller. Nominally 12.5 or so, but reduce to simulatle a dying battery
     * @param motorCommand_in Speed controller command - 1.0 = full fwd, 0.0 = stop, -1.0 = full reverse
     */
    public void update(double motorVoltage_in){

        fwSim.setInputVoltage(motorVoltage_in);

        fwSim.update(Constants.SIM_SAMPLE_RATE_SEC);

        speedAct_RPM = fwSim.getAngularVelocityRPM() * (invertFlywheel?-1.0:1.0);
        current_A = fwSim.getCurrentDrawAmps();

        curDisplacement_Rev += speedAct_RPM / 60 * Constants.SIM_SAMPLE_RATE_SEC;

    }

    /**
     * 
     * @return The present speed of the rotating mass
     */
    double getSpeed_RPM(){
        return speedAct_RPM;
    }

    /**
     * 
     * @return The present current draw of the mechanism
     */
    double getCurrent_A(){
        return current_A;
    }

    /**
     * 
     * @return The present displacement of the azimuth module in Revolutions
     */
    double getAzmthShaftPosition_Rev(){
        return curDisplacement_Rev;
    }

        /**
     * 
     * @return The present displacement of the motor shaft in Revolutions
     */
    double getMotorPosition_Rev(){
        return getAzmthShaftPosition_Rev() * gearing;
    }


}