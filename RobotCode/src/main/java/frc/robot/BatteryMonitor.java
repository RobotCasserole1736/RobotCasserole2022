package frc.robot;

import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.PowerDistribution.ModuleType;
import frc.lib.Signal.Annotations.Signal;

public class BatteryMonitor {
	private static BatteryMonitor moniter = null;
	private PowerDistribution pd;

	@Signal(units="V")
	double batteryVoltage;
	@Signal(units="A")
	double batteryAmps;
	@Signal(units="Status")
	boolean rioBrownOutStatus;
	@Signal(units="V")
	double busRail3v3;
	@Signal(units="V")
	double busRail5v;
	@Signal(units="V")
	double busRail6v;

	public static synchronized BatteryMonitor getInstance() {
		if(moniter == null)
			moniter = new BatteryMonitor();
		return moniter;
	}

	private BatteryMonitor() {
		pd = new PowerDistribution(1,ModuleType.kRev);
		
	}

	public void update (){
		batteryVoltage = pd.getVoltage();
		batteryAmps = pd.getTotalCurrent();
		rioBrownOutStatus = RobotController.isBrownedOut();
		busRail3v3 = RobotController.getVoltage3V3();
		busRail5v = RobotController.getVoltage5V();
		busRail6v = RobotController.getVoltage6V();
	}

}