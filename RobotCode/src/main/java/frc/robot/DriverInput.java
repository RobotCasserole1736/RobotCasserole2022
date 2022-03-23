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


    SlewRateLimiter fwdRevSlewLimiter;
    SlewRateLimiter rotSlewLimiter;
    SlewRateLimiter sideToSideSlewLimiter;

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
    boolean photonAlign;
    @Signal(units="bool")
    boolean resetOdometry;
    @Signal(units="bool")
    boolean isConnected;
    @Signal(units="bool")
    boolean yeetCargo;
    @Signal(units="bool")
    boolean climbEnabled;
    @Signal(units = "bool")
    boolean compressorEnabled = true;

    Debouncer resetOdoDbnc = new Debouncer(0.25, DebounceType.kRising);


    public DriverInput(int controllerIdx){

        driverController = new XboxController(controllerIdx);

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

        if(isConnected){

            
            curFwdRevCmd = -1.0 * driverController.getLeftY();
            curRotCmd = -1.0 * driverController.getRightX();
            curSideToSideCmd = -1.0 * driverController.getLeftX();

            curFwdRevCmd = MathUtil.applyDeadband( curFwdRevCmd,stickDeadband.get()); 
            curRotCmd = MathUtil.applyDeadband( curRotCmd,stickDeadband.get());
            curSideToSideCmd = MathUtil.applyDeadband( curSideToSideCmd,stickDeadband.get());

            if(driverController.getLeftStickButton()){
                curFwdRevCmd = curFwdRevCmd / 2.0;
                curSideToSideCmd = curSideToSideCmd / 2.0;
            } else if(driverController.getRightStickButton()) {
                curRotCmd = curRotCmd / 2.0;
            }
            
            fwdRevSlewCmd = fwdRevSlewLimiter.calculate(curFwdRevCmd);
            rotSlewCmd = rotSlewLimiter.calculate(curRotCmd);
            sideToSideSlewCmd = sideToSideSlewLimiter.calculate(curSideToSideCmd);
            
            robotRelative = driverController.getRightBumper();
            intakeLowerAndRun = driverController.getRightTriggerAxis()>0.5;

            eject = driverController.getXButton();

            //B button shifts between shooting and climbing mode
            if(driverController.getBButton()){
                shootHighGoal = false;
                shootLowGoal = false;
                climbExtend = driverController.getLeftBumper();
                climbRetract = driverController.getLeftTriggerAxis()>0.5;
                climbEnabled = true;
            } else {
                shootHighGoal = driverController.getLeftBumper();
                shootLowGoal = driverController.getLeftTriggerAxis()>0.5;
                climbExtend = false;
                climbRetract = false;
                climbEnabled = false;
            }

            resetOdometry = resetOdoDbnc.calculate(driverController.getAButton());
 
            
            if(driverController.getStartButton()) {
                compressorEnabled = true;
            } else if(driverController.getBackButton()) {
                compressorEnabled = false;
            } else {
                //Maintain old command
            }
            
            //photonAlign = driverController.getAButton(); 
            photonAlign = false; //TODO

            if(driverController.getYButton()){
                yeetCargo = true;
            } else {
                yeetCargo = false;
            }

           

        } else {
            //Controller Unplugged Defaults
            curFwdRevCmd = 0.0;
            curRotCmd = 0.0; 
            curSideToSideCmd = 0.0; 
            yeetCargo = false;
            compressorEnabled = false;
            shootHighGoal = false;
            shootLowGoal = false;
            climbExtend = false;
            climbRetract = false;
            climbEnabled = false;
            robotRelative = false;
            intakeLowerAndRun = false;
            resetOdometry = false;
            intakeLowerAndRun = false;
            eject = false;
            photonAlign = false;
        }

        
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
               
           
        
    }

    /**
     * Gets the driver command for fwd/rev
     * 1.0 means "fast as possible forward"
     * 0.0 means stop
     * -1.0 means "fast as possible reverse"
     * @return 
     */
    public double getFwdRevCmd_mps(){
        return fwdRevSlewCmd * Constants.MAX_FWD_REV_SPEED_MPS * 0.5;
    }

    /**
     * Gets the driver command for rotate
     * 1.0 means "fast as possible to the left"
     * 0.0 means stop
     * -1.0 means "fast as possible to the right"
     * @return 
     */
    public double getRotateCmd_rps(){
        return rotSlewCmd * Constants.MAX_FWD_REV_SPEED_MPS;
    }
    public double getSideToSideCmd_mps(){
        return sideToSideSlewCmd * Constants.MAX_FWD_REV_SPEED_MPS * 0.5;
    }

    public boolean getShootDesired(){
        return shootHighGoal || shootLowGoal || yeetCargo;
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

    public boolean getClimbEnabled(){
        return climbEnabled;
    }
}