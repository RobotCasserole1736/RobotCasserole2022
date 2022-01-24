package frc.sim;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.simulation.BatterySim;
import edu.wpi.first.wpilibj.simulation.PDPSim;
import edu.wpi.first.wpilibj.simulation.RoboRioSim;
import frc.Constants;
import frc.UnitUtils;
import frc.lib.Signal.Annotations.Signal;

public class RobotModel {

    PneumaticsSystemSim ps;
    IntakeSim intake;
    DrivetrainModel dt;
    ShooterSim shooter;

    PDPSim pdp;

    final double QUIESCENT_CURRENT_DRAW_A = 2.0; //Misc electronics
    final double BATTERY_NOMINAL_VOLTAGE = 13.2; //Nicely charged battery
    final double BATTERY_NOMINAL_RESISTANCE = 0.040; //40mOhm - average battery + cabling

    @Signal(units="A")
    double currentDraw_A = QUIESCENT_CURRENT_DRAW_A;
    @Signal(units="V")
    double batteryVoltage_V = BATTERY_NOMINAL_VOLTAGE;

    @Signal
    boolean isBrownedOut;


    public RobotModel(){
        dt = new DrivetrainModel();
        shooter = new ShooterSim();
        ps = new PneumaticsSystemSim();
        intake = new IntakeSim();
        pdp = new PDPSim();
        reset(Constants.DFLT_START_POSE);
    }

    public void reset(Pose2d pose){
        dt.modelReset(pose);
        ps.setStoragePressure_kPa(UnitUtils.psiTokPa(120));
    }

    public void update(boolean isDisabled){

        long numIter = Math.round(Constants.Ts / Constants.SIM_SAMPLE_RATE_SEC);

        for(long count = 0; count < numIter; count++){
            //Calculate motor disablement due to either actually being in disabled mode,
            // or due to brownout.
            isBrownedOut = (batteryVoltage_V < 6.5);
            isDisabled |= isBrownedOut;

            ps.setSystemConsumption(intake.getCylFlow_lps());
            ps.update(isDisabled);

            intake.update(isDisabled, batteryVoltage_V, ps.getSupplyPressure_kPa());
            
            dt.update(isDisabled, batteryVoltage_V);
            shooter.update(isDisabled, batteryVoltage_V);

            currentDraw_A = QUIESCENT_CURRENT_DRAW_A + dt.getCurrentDraw() + shooter.getCurrentDraw_A() + intake.getCurrentDraw_A() + ps.getCurrentDraw_A();

            //batteryVoltage_V = BatterySim.calculateLoadedBatteryVoltage(BATTERY_NOMINAL_VOLTAGE, BATTERY_NOMINAL_RESISTANCE, currentDraw_A);

            RoboRioSim.setVInVoltage(batteryVoltage_V*0.98);
            pdp.setVoltage(batteryVoltage_V);
            pdp.setCurrent(0,currentDraw_A); //Hack just so that getTotalCurrent works in robot code
        }

    }

    public Pose2d getCurActPose(){
        return dt.field.getRobotObject().getPose();
    }

}