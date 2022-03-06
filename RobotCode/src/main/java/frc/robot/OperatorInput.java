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
    boolean compEnable;
    @Signal(units="bool")
    boolean compDisable;
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
            compEnable = operatorController.getStartButton();
            compDisable = operatorController.getBackButton();
            climbExtend = operatorController.getPOV()==0 && operatorController.getBButton();
            climbRetract = operatorController.getPOV()==180 && operatorController.getBButton();
            climbTilt = operatorController.getPOV()==270 && operatorController.getBButton();
            climbStraighten = operatorController.getPOV()==90 && operatorController.getBButton();
    
        } else {
            runShooter = false;
            feedShooter = false;
            intakeLowerAndRun = false;
            eject = false;
            compEnable = false;
            compDisable = false;
            climbExtend = false;
            climbRetract = false;
            climbTilt = false;
            climbStraighten = false;
            
        }


    }

    public boolean getrunShooter(){
        return runShooter;
    }

    public boolean getfeedShooter(){
        return feedShooter;
    }

    public boolean getclimbExtend(){
        return climbExtend;
    }

    public boolean getclimbRetract(){
        return climbRetract;
    }

    public boolean getclimbTilt(){
        return climbTilt;
    }

    public boolean getclimbStraighten(){
        return climbStraighten;
    }

    public boolean getintakeLowerAndRun(){
        return intakeLowerAndRun;
    }

    public boolean getintakeRaise(){
        return intakeRaise;
    }

    public boolean geteject(){
        return eject;
    }

    public boolean getcompEnable(){
        return compEnable;
    }

    public boolean getcompDisable(){
        return compDisable;
    }
    
}