package frc.robot;

import edu.wpi.first.wpilibj.XboxController;
import frc.lib.Signal.Annotations.Signal;

public class OperatorInput {
    
    XboxController operatorController;

    private static OperatorInput di = null;
    public static synchronized OperatorInput getInstance() {
        if(di == null)
            di = new OperatorInput();
        return di;
    }
   
    @Signal(units="bool")
    boolean runShooter;
    @Signal(units="bool")
    boolean feedShooter;
    @Signal(units="bool")
    boolean climbExtend;
    @Signal(units="bool")
    boolean climbRetract;
    @Signal(units="bool")
    boolean climbTilt;
    @Signal(units="bool")
    boolean climbStraighten;
    @Signal(units="bool")
    boolean intakeLowerAndRun;
    @Signal(units="bool")
    boolean intakeRaise;
    @Signal(units="bool")
    boolean eject;
    @Signal(units="bool")
    boolean isConnected;

    private OperatorInput(){
        operatorController = new XboxController(1);
    }

    public void update(){

        isConnected = operatorController.isConnected();

        if(isConnected){
            runShooter = operatorController.getLeftTriggerAxis()>0.5;
            feedShooter = operatorController.getLeftBumper();
            intakeLowerAndRun = operatorController.getRightTriggerAxis()>0.5;
            eject = operatorController.getXButtonPressed();
            climbExtend = operatorController.getPOV()==0 && operatorController.getBButton();
            climbRetract = operatorController.getPOV()==180 && operatorController.getBButton();
            climbTilt = operatorController.getPOV()==270 && operatorController.getBButton();
            climbStraighten = operatorController.getPOV()==90 && operatorController.getBButton();
    
        } else {
            //USB controller not connected
            runShooter = false;
            feedShooter = false;
            intakeLowerAndRun = false;
            eject = false;
            climbExtend = false;
            climbRetract = false;
            climbTilt = false;
            climbStraighten = false;  
        }

    }

    public boolean getRunShooter(){
        return runShooter;
    }

    public boolean getfeedShooter(){
        return feedShooter;
    }

    public boolean getClimbExtend(){
        return climbExtend;
    }

    public boolean getClimbRetract(){
        return climbRetract;
    }

    public boolean getClimbTilt(){
        return climbTilt;
    }

    public boolean getClimbStraighten(){
        return climbStraighten;
    }

    public boolean getIntakeLowerAndRun(){
        return intakeLowerAndRun;
    }

    public boolean getintakeRaise(){
        return intakeRaise;
    }

    public boolean getEject(){
        return eject;
    }

}