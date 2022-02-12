package frc.robot;

import edu.wpi.first.wpilibj.XboxController;
import frc.lib.Signal.Annotations.Signal;

public class OperatorInput {
    
    XboxController OperatorController;

    private static OperatorInput di = null;
    public static synchronized OperatorInput getInstance() {
        if(di == null)
            di = new OperatorInput();
        return di;
    }

    @Signal()
    boolean runShooter;
    @Signal()
    boolean feedShooter;


    private OperatorInput(){
        OperatorController = new XboxController(1);

    }

    public void update(){
        runShooter = OperatorController.getXButton();
        feedShooter = OperatorController.getBButton();
        
    }

    public boolean getRunShooter(){
        return runShooter;
    }

    public boolean getFeedShooter(){
        return feedShooter;
    }
    
}