package frc.wrappers.MotorCtrl;

import frc.lib.Calibration.Calibration;
import frc.lib.Signal.Annotations.Signal;
import frc.robot.Robot;
import frc.wrappers.MotorCtrl.Sim.SimSmartMotor;
import frc.wrappers.MotorCtrl.SparkMax.RealSparkMax;
import frc.wrappers.MotorCtrl.TalonFX.RealTalonFX;

public class CasseroleCANMotorCtrl {

    public enum CANMotorCtrlType {
        TALON_FX,
        SPARK_MAX
    }

    Calibration kP_cal;
    Calibration kI_cal;
    Calibration kD_cal;

    AbstractSimmableMotorController ctrl;

    @Signal(units="V")
    private double appliedVoltage;

    @Signal(units = "radpersec")
    private double actVel;

    @Signal(units = "A")
    private double current;

    @Signal(units = "radpersec")
    private double desVel;

    @Signal(units = "rad")
    private double actPos;

    public CasseroleCANMotorCtrl(String prefix, int can_id, CANMotorCtrlType type){
        if(Robot.isSimulation()){
            ctrl = new SimSmartMotor(can_id);
        } else {
            switch(type){
                case TALON_FX:
                    ctrl = new RealTalonFX(can_id);
                    break;
                case SPARK_MAX:
                    ctrl = new RealSparkMax(can_id);
                    break;
            }
        }

        kP_cal = new Calibration(prefix + "_kP", "", 0);
        kI_cal = new Calibration(prefix + "_kI", "", 0);
        kD_cal = new Calibration(prefix + "_kD", "", 0);
    }
    

    public void update(){
        // Handle Calibration Changes
        if(kP_cal.isChanged() || kI_cal.isChanged() || kD_cal.isChanged()){
            ctrl.setClosedLoopGains(kP_cal.get(), kI_cal.get(), kD_cal.get());
            kP_cal.acknowledgeValUpdate();
            kI_cal.acknowledgeValUpdate();
            kD_cal.acknowledgeValUpdate();
        }
        actVel = ctrl.getVelocity_radpersec();
        actPos = ctrl.getPosition_rad();
        current = ctrl.getCurrent_A();
        appliedVoltage = ctrl.getAppliedVoltage_V();
    }

    public void setInverted(boolean invert){
        ctrl.setInverted(invert);
    }

    public void setClosedLoopGains(double p, double i, double d){
        kP_cal.cur_val = p;
        kI_cal.cur_val = i;
        kD_cal.cur_val = d;
    }

    public void setClosedLoopCmd(double velocityCmd_radpersec, double arbFF_V){
        ctrl.setClosedLoopCmd(velocityCmd_radpersec, arbFF_V);
        desVel = velocityCmd_radpersec;
    }

    public void setVoltageCmd(double cmd_V){
        ctrl.setVoltageCmd(cmd_V);
    }

    public double getCurrent_A(){
        return current;
    }

    public double getVelocity_radpersec(){
        return actVel;
    }

    public double getPosition_rad(){
        return actPos;
    }

}