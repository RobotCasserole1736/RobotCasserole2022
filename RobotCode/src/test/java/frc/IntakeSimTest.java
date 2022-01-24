package frc;

import static org.junit.Assert.*;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.simulation.REVPHSim;
import frc.sim.IntakeSim;
import frc.wrappers.MotorCtrl.Sim.SimSmartMotor;
import frc.wrappers.SimDeviceBanks;
import org.junit.*;

public class IntakeSimTest {
  SimSmartMotor ctrl;
  IntakeSim intk;
  REVPHSim phSim;

  @Before // this method will run before each test
  public void setup() {
    assert HAL.initialize(500, 0); // initialize the HAL, crash if failed
    ctrl = new SimSmartMotor(Constants.INTAKE_MOTOR_CANID);
    ctrl.sim_setSupplyVoltage(12.0);
    intk = new IntakeSim();
    phSim = new REVPHSim();
  }

  @After // this method will run after each test
  public void shutdown() throws Exception {
    SimDeviceBanks.clearAllBanks();
  }

  @Test
  public void extendRetractCycleTest() {
    // Set cylender to extend
    phSim.setSolenoidOutput(Constants.INTAKE_SOLENOID, true);
    run(0.1);
    // Middle of extend - be sure we're having some pneumatic flow
    assertTrue(intk.getCylFlow_lps() > 0.1);

    // Let the cylender fully extend
    run(3.0);

    // Make sure we've hit the endstop
    assertTrue(intk.getCylPos_m() > 0.1);
    assertTrue(intk.getCylFlow_lps() < 0.1);

    // Set cylender to retract
    phSim.setSolenoidOutput(Constants.INTAKE_SOLENOID, false);
    run(0.1);

    // Middle of retract - be sure we're having some pneumatic flow
    assertTrue(intk.getCylFlow_lps() > 0.1);

    // Let the cylender fully retract
    run(3.0);

    // Make sure we've hit the endstop
    assertTrue(intk.getCylPos_m() < 0.1);
    assertTrue(intk.getCylFlow_lps() < 0.1);
  }

  @Test
  public void ingestTest() {
    ctrl.setVoltageCmd(12.0);
    run(2.0);
    assertTrue(ctrl.getVelocity_radpersec() > 0);
    assertTrue(ctrl.getCurrent_A() > 0);
    assertTrue(intk.getCurrentDraw_A() > 0);
  }

  @Test
  public void ejectTest() {
    ctrl.setVoltageCmd(-12.0);
    run(2.0);
    assertTrue(ctrl.getVelocity_radpersec() < 0);
    assertTrue(ctrl.getCurrent_A() > 0);
    assertTrue(intk.getCurrentDraw_A() > 0);
  }

  private void run(double duration_s) {
    for (int step = 0; step < duration_s / Constants.SIM_SAMPLE_RATE_SEC; step++) {
      intk.update(false, 12.0, UnitUtils.psiTokPa(60.0));
    }
  }
}
