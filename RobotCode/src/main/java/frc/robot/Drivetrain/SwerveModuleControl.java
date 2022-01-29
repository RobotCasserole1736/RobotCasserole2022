package frc.robot.Drivetrain;

import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.Timer;
import frc.Constants;
import frc.lib.Signal.Annotations.Signal;
import frc.robot.DriverInput;
import frc.wrappers.MotorCtrl.CasseroleCANMotorCtrl;
import frc.wrappers.SwerveAzmthEncoder.CasseroleSwerveAzmthEncoder;

class SwerveModuleControl {

    CasseroleCANMotorCtrl wheel;
    CasseroleCANMotorCtrl azmth;
    CasseroleSwerveAzmthEncoder azmth_enc;

    SwerveModuleState desState = new SwerveModuleState();
    SwerveModuleState actState = new SwerveModuleState();

    frc.lib.Signal.Signal wheelSpdDesSig;
    frc.lib.Signal.Signal wheelSpdActSig;
    frc.lib.Signal.Signal azmthPosDesSig;
    frc.lib.Signal.Signal azmthPosActSig;

    double wheelMotorSpeedDes_RPM = 0;
    double wheelMotorSpeedAct_RPM = 0;

    double azmthPosAct_deg = 0;

    @Signal(units = "cmd")
    double wheelMotorCmd;

    //TODO create an "Azimuth Controller" which calculates steer motor voltage commands from actual/desired angles



    public SwerveModuleControl(String posId, int wheelMotorIdx, int azmthMotorIdx, int azmthEncoderIdx){

         wheel = new CasseroleCANMotorCtrl("wheel"+posId, wheelMotorIdx, CasseroleCANMotorCtrl.CANMotorCtrlType.TALON_FX);
         azmth = new CasseroleCANMotorCtrl("azmth"+posId, azmthMotorIdx, CasseroleCANMotorCtrl.CANMotorCtrlType.SPARK_MAX);
         azmth_enc = new CasseroleSwerveAzmthEncoder("encoder"+posId, azmthEncoderIdx, 0);
      
        wheelSpdDesSig = new frc.lib.Signal.Signal("DtModule_" + posId + "_azmthDes", "RPM");
        wheelSpdActSig = new frc.lib.Signal.Signal("DtModule_" + posId + "_azmthAct", "RPM");
        azmthPosDesSig = new frc.lib.Signal.Signal("DtModule_" + posId + "_speedDes", "deg");
        azmthPosActSig = new frc.lib.Signal.Signal("DtModule_" + posId + "_speedAct", "deg");

    }

    public void update(double curSpeedFtPerSec, double maxAzmthErr_deg){
     
        DriverInput di;
        di = DriverInput.getInstance();

        wheel.setVoltageCmd(di.getFwdRevCmd() * 12.0);
        azmth.setVoltageCmd(di.getRotateCmd() * 12.0);

        //TODO read the azimuth angle

        //TODO calcualte desired wheel speed


        //TODO invert the wheel speed if needed

        //TODO scale back the wheel speed if our azimuth angle isn't on target

        //TODO measure the actual wheel speed from the drive motor

        //TODO calculate feed-forward drivetrain 

        //TODO send the voltage or speed commands to the motors

        //TODO update the "actualState" object with this module's present steer angle and speed in meters per second

    }

    /**
     * Broadcast signals specific to the visualiation
     */
    public void updateTelemetry(){
        double sampleTime = Timer.getFPGATimestamp(); //TODO - this should actually be coming from the loop timing utility, whenever Lucas finishes it up.
        wheelSpdDesSig.addSample(sampleTime, wheelMotorSpeedDes_RPM);
        wheelSpdActSig.addSample(sampleTime, wheelMotorSpeedAct_RPM);

        //TODO - forward azimuth controller information on to the signals
        azmthPosDesSig.addSample(sampleTime, 0);
        azmthPosActSig.addSample(sampleTime, 0);
    }

    //TODO - test mode update for PID tuning azimuth motor velocity

    public void setDesiredState(SwerveModuleState des){
        desState = des;
    }

    public SwerveModuleState getActualState(){
        return actState;
    }

    public SwerveModuleState getDesiredState(){
        return desState;
    }

}
