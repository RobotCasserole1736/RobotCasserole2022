package frc.wrappers.MotorCtrl.Sim;

import java.util.ArrayList;

import edu.wpi.first.math.util.Units;
import frc.Constants;
import frc.lib.Signal.Annotations.Signal;
import frc.wrappers.SimDeviceBanks;
import frc.wrappers.MotorCtrl.AbstractSimmableMotorController;

/**
 * CTRE doesn't currently support simulating the internal functionality of a TalonFX.
 * We provide a wrapper and interface abstraction here to enable simulation through
 * a transparent wrapper layer in robot code.
 */
public class SimSmartMotor extends AbstractSimmableMotorController {

    public ArrayList<SimSmartMotor> simFollowers = new ArrayList<SimSmartMotor>();

    boolean isInverted;
    double kP;
    double kI;
    double kD;

    @Signal(units="V")
    private double curWindingVoltage;
    @Signal(units="A")
    private double curCurrent;
    @Signal(units="radpersec")
    private double curVel_radpersec;
    @Signal(units="V")
    private double curSupplyVoltage = 12.0;
    @Signal(units="rad")
    private double curPos_rad;

    public SimSmartMotor(int can_id){
        SimDeviceBanks.addCANDevice(this, can_id);
    }


    @Override
    public void setInverted(boolean invert) {
        isInverted = invert;
    }


    @Override
    public void setClosedLoopGains(double p, double i, double d) {
        kP = p;
        kI = i;
        kD = d;
    }


    @Override
    public void setClosedLoopCmd(double velocityCmd_radpersec, double arbFF_V) {
        setVoltageCmd(pidSim(velocityCmd_radpersec, arbFF_V));
    }


    @Override
    public void setVoltageCmd(double cmd_v) {
        curWindingVoltage = limitVoltage(cmd_v);
        for(AbstractSimmableMotorController follower : simFollowers){
            follower.setVoltageCmd(curWindingVoltage);
        }
    }


    // Returns the current draw of the motor controller from the rest of the electrical system.
    @Override
    public double getCurrent_A() {
        return curCurrent;
    }

    @Override
    public double getVelocity_radpersec() {
        return curVel_radpersec;
    }

    @Override
    public double getPosition_rad() {
        return curPos_rad;
    }

    public void sim_setActualVelocity(double velocity_radpersec){
        curVel_radpersec = velocity_radpersec;
    }

    public void sim_setActualPosition(double pos_rad){
        curPos_rad = pos_rad;
    }

    public double sim_getWindingVoltage(){
        return curWindingVoltage;
    }

    public void sim_setSupplyVoltage(double supply_V){
        curSupplyVoltage = supply_V;
    }

    // Set the current flowing through the motor windings
    public void sim_setCurrent(double cur_A){
        curCurrent = cur_A * Math.signum(curSupplyVoltage); //H bridge will reverse current flow
    }

    private double limitVoltage(double in){
        if(in > curSupplyVoltage){
            return curSupplyVoltage;
        } else if (in < -curSupplyVoltage){
            return -curSupplyVoltage;
        } else {
            return in;
        }
    }


    double velErr_accum;
    double velErr_prev;
    /**
     * A rough guess at the behavior of the closed loop controllers on the smart motor controllers
     */
    private double pidSim(double vel_cmd, double arb_ff_frac){
        var velError_RPM = Units.radiansPerSecondToRotationsPerMinute(curVel_radpersec - vel_cmd);
        
        velErr_accum += velError_RPM;
        
        var velErr_delta = (velError_RPM - velErr_prev)/Constants.Ts;

        var pTerm = velError_RPM * kP;
        var dTerm = velErr_delta * kD;
        var iTerm = velErr_accum * kI;
        var fTerm = arb_ff_frac;

        velErr_prev = velError_RPM;

        return limitVoltage(((pTerm + dTerm + iTerm) + fTerm) * curSupplyVoltage);
    }


    @Override
    public void follow(Object leader) {
        if(leader.getClass() == SimSmartMotor.class){
            ((SimSmartMotor)leader).simFollowers.add(this);
        } else {
            throw new IllegalArgumentException(leader.getClass().toString() + " cannot be followed by a " + this.getClass().toString());
        }

    }

    @Override
    public double getAppliedVoltage_V() {
        return curWindingVoltage;
    }

    
}
