package frc;

import static org.junit.Assert.*;

import edu.wpi.first.hal.HAL;
import frc.sim.ShooterSim;
import frc.wrappers.MotorCtrl.Sim.SimSmartMotor;
import frc.wrappers.SimDeviceBanks;
import org.junit.*;

public class ShooterSimTest {
  SimSmartMotor ctrl;
  ShooterSim shooter;

  @Before // this method will run before each test
  public void setup() {
    assert HAL.initialize(500, 0); // initialize the HAL, crash if failed
    ctrl = new SimSmartMotor(Constants.SHOOTER_MOTOR_CANID);
    ctrl.sim_setSupplyVoltage(12.0);
    shooter = new ShooterSim();
  }

  @After // this method will run after each test
  public void shutdown() throws Exception {
    SimDeviceBanks.clearAllBanks();
  }

  @Test
  public void spoolupTest() {
    ctrl.setVoltageCmd(0.0);
    run(5.0);
    assertTrue(ctrl.getVelocity_radpersec() < 0.1);
    assertTrue(ctrl.getCurrent_A() < 0.1);
    assertTrue(shooter.getCurrentDraw_A() < 0.1);

    ctrl.setVoltageCmd(12.0);
    run(5.0);
    assertTrue(ctrl.getVelocity_radpersec() > 0);
    assertTrue(ctrl.getCurrent_A() > 0);
    assertTrue(shooter.getCurrentDraw_A() > 0);
  }

  private void run(double duration_s) {
    for (int step = 0; step < duration_s / Constants.SIM_SAMPLE_RATE_SEC; step++) {
      shooter.update(false, 12.0);
    }
  }
}
