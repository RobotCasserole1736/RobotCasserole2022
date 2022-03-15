package frc.robot;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.filter.Debouncer;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.math.filter.Debouncer.DebounceType;
import edu.wpi.first.wpilibj.XboxController;
import frc.Constants;
import frc.lib.Calibration.Calibration;
import frc.lib.Signal.Annotations.Signal;

public class DriverInput {
    
    XboxController driverController;
    boolean compressorEnabled = true;

    SlewRateLimiter fwdRevSlewLimiter;
    SlewRateLimiter rotSlewLimiter;
    SlewRateLimiter sideToSideSlewLimiter;


    private static DriverInput di = null;
    public static synchronized DriverInput getInstance() {
        if(di == null)
            di = new DriverInput();
        return di;
    }
    Calibration stickDeadband;
    Calibration fwdRevSlewRate;
    Calibration rotSlewRate;
    Calibration sideToSideSlewRate;

    @Signal(units="cmd")
    double curFwdRevCmd;
    @Signal(units="cmd")
    double curRotCmd;
    @Signal(units="cmd")
    double curSideToSideCmd;

    @Signal(units="bool")
    boolean robotRelative;
   
    @Signal (units="cmd")
    double fwdRevSlewCmd;
    @Signal (units="cmd")
    double rotSlewCmd;
    @Signal (units="cmd")
    double sideToSideSlewCmd;
    @Signal(units="bool")
    boolean shootHighGoal;
    @Signal(units="bool")
    boolean shootLowGoal;
    @Signal(units="bool")
    boolean climbExtend;
    @Signal(units="bool")
    boolean climbRetract;
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
    boolean photonAlign;
    @Signal(units="bool")
    boolean resetOdometry;
    @Signal(units="bool")
    boolean isConnected;
    @Signal(units="bool")
    boolean yeetCargo;

    Debouncer resetOdoDbnc = new Debouncer(0.25, DebounceType.kRising);


    private DriverInput(){

        driverController = new XboxController(0);

        stickDeadband = new Calibration("StickDeadBand", "", 0.1);

        fwdRevSlewRate = new Calibration("fwdRevSlewRate", "", 3);
        rotSlewRate = new Calibration("rotSlewRate", "", 3);
        sideToSideSlewRate = new Calibration("sideToSideSlewRate", "", 3);

        fwdRevSlewLimiter = new SlewRateLimiter(fwdRevSlewRate.get());
        rotSlewLimiter = new SlewRateLimiter(rotSlewRate.get());
        sideToSideSlewLimiter = new SlewRateLimiter(sideToSideSlewRate.get());

    }

    public void update(){

        isConnected = driverController.isConnected();
        
        curFwdRevCmd = -1.0 * driverController.getLeftY();
        curRotCmd = -1.0 * driverController.getRightX();
        curSideToSideCmd = -1.0 * driverController.getLeftX();

        curFwdRevCmd = MathUtil.applyDeadband( curFwdRevCmd,stickDeadband.get()); 
        fwdRevSlewCmd = fwdRevSlewLimiter.calculate(curFwdRevCmd);

        curRotCmd = MathUtil.applyDeadband( curRotCmd,stickDeadband.get());
        rotSlewCmd = rotSlewLimiter.calculate(curRotCmd);
      
        curSideToSideCmd = MathUtil.applyDeadband( curSideToSideCmd,stickDeadband.get());
        sideToSideSlewCmd = sideToSideSlewLimiter.calculate(curSideToSideCmd);
        
        robotRelative = driverController.getRightBumper();
        intakeLowerAndRun = driverController.getRightTriggerAxis()>0.5;

        eject = driverController.getXButton();
        compEnable = driverController.getStartButton();
        compDisable = driverController.getBackButton();

        //B button shifts between shooting and climbing mode
        if(driverController.getBButton()){
            shootLowGoal = false;
            shootHighGoal = false;
            climbExtend = driverController.getPOV()==0;
            climbRetract = driverController.getPOV()==180;
        } else {
            shootLowGoal = driverController.getLeftTriggerAxis()>0.5;
            shootHighGoal = driverController.getLeftBumper();
            climbExtend = false;
            climbRetract = false;
        }

        resetOdometry = resetOdoDbnc.calculate(driverController.getAButton());

        if(fwdRevSlewRate.isChanged() ||
           rotSlewRate.isChanged() ||
           sideToSideSlewRate.isChanged()) {
                fwdRevSlewRate.acknowledgeValUpdate();
                rotSlewRate.acknowledgeValUpdate();
                sideToSideSlewRate.acknowledgeValUpdate();
                fwdRevSlewLimiter = new SlewRateLimiter(fwdRevSlewRate.get());
                rotSlewLimiter = new SlewRateLimiter(rotSlewRate.get());
                sideToSideSlewLimiter = new SlewRateLimiter(sideToSideSlewRate.get());
           }
                
        
        if(driverController.getStartButton()) {
            compressorEnabled = true;
        } else if(driverController.getBackButton()) {
            compressorEnabled = false;
        } else {
            //Maintain old command
        }
        
        //photonAlign = driverController.getAButton();
        if(driverController.getYButton()){
            yeetCargo = true;
        } 
        else {
            yeetCargo = false;
        }
           
        
    }

    /**
     * Gets the driver command for fwd/rev
     * 1.0 means "fast as possible forward"
     * 0.0 means stop
     * -1.0 means "fast as possible reverse"
     * @return 
     */
    public double getFwdRevCmd_mps(){
        return fwdRevSlewLimiter.calculate(curFwdRevCmd) * Constants.MAX_FWD_REV_SPEED_MPS * 0.5;
    }

    /**
     * Gets the driver command for rotate
     * 1.0 means "fast as possible to the left"
     * 0.0 means stop
     * -1.0 means "fast as possible to the right"
     * @return 
     */
    public double getRotateCmd_rps(){
        return rotSlewLimiter.calculate(curRotCmd) * Constants.MAX_FWD_REV_SPEED_MPS;
    }
    public double getSideToSideCmd_mps(){
        return sideToSideSlewLimiter.calculate(curSideToSideCmd) * Constants.MAX_FWD_REV_SPEED_MPS * 0.5;
    }

    public boolean getShootHighGoal(){
        return shootHighGoal;
    }

    public boolean getShootLowGoal(){
        return shootLowGoal;
    }

    public boolean getClimbExtend(){
        return climbExtend;
    }

    public boolean getClimbRetract(){
        return climbRetract;
    }

    public boolean getIntakeLowerAndRun(){
        return intakeLowerAndRun;
    }

    public boolean getIntakeRaise(){
        return intakeRaise;
    }

    public boolean getEject(){
        return eject;
    }

    public boolean getCompEnable(){
        return compEnable;
    }

    public boolean getCompDisable(){
        return compDisable;
    }

    public boolean getRobotRelative(){
        return robotRelative;
    }

    public boolean getCompressorEnabledCmd(){
        return compressorEnabled;
      }
    
    public boolean getPhotonAlign(){
        return photonAlign;
    }

    public boolean getOdoResetCmd(){
        return resetOdometry;
    }
    public boolean getYeetCargoCmd(){
        return yeetCargo;
    }
}