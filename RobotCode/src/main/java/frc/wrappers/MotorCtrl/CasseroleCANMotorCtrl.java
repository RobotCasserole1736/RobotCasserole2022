package frc.wrappers.MotorCtrl;

import frc.lib.Calibration.Calibration;
import frc.robot.Robot;
import frc.wrappers.MotorCtrl.Sim.SimSmartMotor;
import frc.wrappers.MotorCtrl.SparkMax.RealSparkMax;
import frc.wrappers.MotorCtrl.TalonFX.RealTalonFX;

public class CasseroleCANMotorCtrl {

    Calibration kP_cal;
    Calibration kI_cal;
    Calibration kD_cal;

    AbstractSimmableMotorController ctrl;

    public enum CANMotorCtrlType {
        TALON_FX,
        SPARK_MAX
    }

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
    
    ///////////////////////////////////////////////////////////////////////////////////
    // Pass-through functions to the underlying controller
    //  This could probably be done more cleanly by making this constructore
    //  follow the factory pattern and letting the user directly interact with the
    //  resulting controller. But, since we don't teach that pattern,
    //  I'm avoiding it for now.
    ///////////////////////////////////////////////////////////////////////////////////

    public void update(){
        // TODO Look for changes to gains
        // TODO update telemetry
    }

    public void setInverted(boolean invert){
        ctrl.setInverted(invert);
    }

    public void setClosedLoopGains(double p, double i, double d){
        //TODO update the coontroller's pid gains
    }

    public void setClosedLoopCmd(double velocityCmd_radpersec, double arbFF_V){
        ctrl.setClosedLoopCmd(velocityCmd_radpersec, arbFF_V);
    }

    public void setVoltageCmd(double cmd_V){
        ctrl.setVoltageCmd(cmd_V);
    }

    public double getCurrent_A(){
        return ctrl.getCurrent_A();
    }

    public double getVelocity_radpersec(){
        return ctrl.getVelocity_radpersec();
    }

    public double getPosition_rad(){
        return ctrl.getPosition_rad();
    }

}
