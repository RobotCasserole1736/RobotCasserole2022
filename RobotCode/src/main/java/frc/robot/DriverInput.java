package frc.robot;

import edu.wpi.first.wpilibj.XboxController;
import frc.lib.Signal.Annotations.Signal;

public class DriverInput {
    
    XboxController driverController;

    private static DriverInput di = null;
    public static synchronized DriverInput getInstance() {
        if(di == null)
            di = new DriverInput();
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



    private DriverInput(){

        driverController = new XboxController(0);


    }

    public void update(){
        curFwdRevCmd = -1.0 * driverController.getLeftY();
        curRotCmd = -1.0 * driverController.getRightX();
        curSideToSideCmd = -1.0 * driverController.getLeftX();
        robotRelative = driverController.getRightBumper();
        runShooter = driverController.getLeftTriggerAxis()>0.5;
        feedShooter = driverController.getLeftBumper();
        intakeLowerAndRun = driverController.getRightTriggerAxis()>0.5;
        eject = driverController.getXButtonPressed();
        compEnable = driverController.getStartButton();
        compDisable = driverController.getBackButton();
        climbExtend = driverController.getPOV()==0 && driverController.getBButton();
        climbRetract = driverController.getPOV()==180 && driverController.getBButton();
        climbTilt = driverController.getPOV()==270 && driverController.getBButton();
        climbStraighten = driverController.getPOV()==90 && driverController.getBButton();

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