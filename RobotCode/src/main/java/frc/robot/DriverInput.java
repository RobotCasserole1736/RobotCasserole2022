package frc.robot;

import edu.wpi.first.wpilibj.XboxController;
import frc.lib.Signal.Annotations.Signal;

public class DriverInput {
    
    XboxController driverController;

    @Signal(units="cmd")
    double curFwdRevCmd;
    @Signal(units="cmd")
    double curRotCmd;
    @Signal(units="cmd")
    double curSideToSideCmd;

    public DriverInput(){

        driverController = new XboxController(0);

    }

    public void update(){
        curFwdRevCmd = -1.0 * driverController.getLeftY();
        curRotCmd = -1.0 * driverController.getRightX();
        curSideToSideCmd = -1.0 * driverController.getLeftX();
    }

    /**
     * Gets the driver command for fwd/rev
     * 1.0 means "fast as possible forward"
     * 0.0 means stop
     * -1.0 means "fast as possible reverse"
     * @return 
     */
    double getFwdRevCmd(){
        return curFwdRevCmd;
    }

    /**
     * Gets the driver command for rotate
     * 1.0 means "fast as possible to the left"
     * 0.0 means stop
     * -1.0 means "fast as possible to the right"
     * @return 
     */
    double getRotateCmd(){
        return curRotCmd;
    }
    double getSideToSideCmd(){
        return curSideToSideCmd;
    }
    
}