package frc.robot.Drivetrain;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.motorcontrol.Spark;
import frc.Constants;
import frc.UnitUtils;
import frc.lib.Signal.Annotations.Signal;
import frc.lib.Util.MapLookup2D;
import frc.robot.LoopTiming;

class SwerveModuleControl {

    Spark wheelMotorCtrl;
    Spark azmthMotorCtrl;
    Encoder wheelEnc;
    Encoder azmthEnc;

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

    AzimuthAngleController azmthCtrl;

    MapLookup2D wheelCmdLimitTbl;


    final double WHEEL_MAX_SPEED_RPM = 620; //determined empirically

    PIDController wheelPIDCtrl = new PIDController(0.011, 0, 0.000);


    public SwerveModuleControl(String posId, int wheelMotorIdx, int azmthMotorIdx, int wheelEncoderIdx, int azmthEncoderIdx){

        wheelMotorCtrl = new Spark(wheelMotorIdx);
        azmthMotorCtrl = new Spark(azmthMotorIdx);
        wheelEnc = new Encoder(wheelEncoderIdx, wheelEncoderIdx + 1); //Always assume channel B is one after channel A.
        azmthEnc = new Encoder(azmthEncoderIdx, azmthEncoderIdx + 1);

        wheelEnc.setDistancePerPulse(Constants.WHEEL_ENC_WHEEL_REVS_PER_COUNT);
        azmthEnc.setDistancePerPulse(Constants.AZMTH_ENC_MODULE_REVS_PER_COUNT);

        wheelSpdDesSig = new frc.lib.Signal.Signal("DtModule" + posId + "WheelSpdDes", "RPM");
        wheelSpdActSig = new frc.lib.Signal.Signal("DtModule" + posId + "WheelSpdAct", "RPM");
        azmthPosDesSig = new frc.lib.Signal.Signal("DtModule" + posId + "AzmthPosDes", "deg");
        azmthPosActSig = new frc.lib.Signal.Signal("DtModule" + posId + "AzmthPosAct", "deg");

        wheelCmdLimitTbl = new MapLookup2D();
        wheelCmdLimitTbl.insertNewPoint(0.0, 1.0);
        wheelCmdLimitTbl.insertNewPoint(5.0, 1.0);
        wheelCmdLimitTbl.insertNewPoint(7.0, 0.8);
        wheelCmdLimitTbl.insertNewPoint(15.0, 0.5);
        wheelCmdLimitTbl.insertNewPoint(30.0, 0.1);
        wheelCmdLimitTbl.insertNewPoint(45.0, 0.0);
        wheelCmdLimitTbl.insertNewPoint(90.0, 0.0);

        azmthCtrl = new AzimuthAngleController();

    }

    public void update(double curSpeedFtPerSec, double maxAzmthErr_deg){

        azmthPosAct_deg = azmthEnc.getDistance() * 360.0;

        azmthCtrl.setInputs(desState.angle.getDegrees(), azmthPosAct_deg, curSpeedFtPerSec);
        azmthCtrl.update();

        //Calcaulte desired speed from input state, azimuth controller reversal command, and worst-case azimuth module error.
        wheelMotorSpeedDes_RPM = UnitUtils.DtMPerSectoRPM(desState.speedMetersPerSecond)*(azmthCtrl.getInvertWheelCmd()?-1.0:1.0);
        wheelMotorSpeedDes_RPM *= wheelCmdLimitTbl.lookupVal(maxAzmthErr_deg);

        wheelMotorSpeedAct_RPM = wheelEnc.getRate() * 60;

        //Closed-loop control of wheel velocity
        wheelPIDCtrl.setSetpoint(wheelMotorSpeedDes_RPM);
        double wheelFFCmd = wheelMotorSpeedDes_RPM/WHEEL_MAX_SPEED_RPM;
        double wheelFBCmd = wheelPIDCtrl.calculate(wheelMotorSpeedAct_RPM);
        wheelMotorCmd = UnitUtils.limitMotorCmd(wheelFFCmd+wheelFBCmd);

        wheelMotorCtrl.set(wheelMotorCmd); 
        azmthMotorCtrl.set(azmthCtrl.getMotorCmd()); 

        actState.angle = Rotation2d.fromDegrees(azmthPosAct_deg);
        actState.speedMetersPerSecond = UnitUtils.DtRPMtoMPerSec(wheelMotorSpeedAct_RPM);

        updateTelemetry();
    }

    /**
     * Broadcast signals specific to the visualiation
     */
    public void updateTelemetry(){
        double sampleTimeMs = LoopTiming.getInstance().getLoopStartTimeSec() * 1000;
        wheelSpdDesSig.addSample(sampleTimeMs, wheelMotorSpeedDes_RPM);
        wheelSpdActSig.addSample(sampleTimeMs, wheelMotorSpeedAct_RPM);
        azmthPosDesSig.addSample(sampleTimeMs, azmthCtrl.getSetpoint_deg());
        azmthPosActSig.addSample(sampleTimeMs, azmthPosAct_deg);
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
