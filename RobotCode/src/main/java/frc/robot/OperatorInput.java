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

    @Signal(units="cmd")
    double curFwdRevCmd;
    @Signal(units="cmd")
    double curRotCmd;
    @Signal(units="cmd")
    double curSideToSideCmd;

    @Signal(units="bool")
    boolean robotRelative;
   
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



    private OperatorInput(){

        operatorController = new XboxController(0);


    }

    public void update(){
        curFwdRevCmd = -1.0 * operatorController.getLeftY();
        curRotCmd = -1.0 * operatorController.getRightX();
        curSideToSideCmd = -1.0 * operatorController.getLeftX();
        robotRelative = operatorController.getRightBumper();
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

    }

    /**
     * Gets the driver command for fwd/rev
     * 1.0 means "fast as possible forward"
     * 0.0 means stop
     * -1.0 means "fast as possible reverse"
     * @return 
     */
    public double getFwdRevCmd(){
        return curFwdRevCmd;
    }

    /**
     * Gets the driver command for rotate
     * 1.0 means "fast as possible to the left"
     * 0.0 means stop
     * -1.0 means "fast as possible to the right"
     * @return 
     */
    public double getRotateCmd(){
        return curRotCmd;
    }
    public double getSideToSideCmd(){
        return curSideToSideCmd;
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