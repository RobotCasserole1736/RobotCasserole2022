package frc;

import static org.junit.Assert.*;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.math.util.Units;
import frc.sim.ShooterSim;
import frc.wrappers.SimDeviceBanks;
import frc.wrappers.MotorCtrl.Sim.SimSmartMotor;

import org.junit.*;

public class UnitUtilsTest {


  @Before // this method will run before each test
  public void setup() {
    assert HAL.initialize(500, 0); // initialize the HAL, crash if failed

  }

  @After // this method will run after each test
  public void shutdown() throws Exception {
    SimDeviceBanks.clearAllBanks();
  }

  @Test
  public void dtSpeedConversion() {
    double testSpeed_mps = Constants.MAX_FWD_REV_SPEED_MPS;
    double resultSpeed_rps = UnitUtils.dtLinearSpeedToMotorSpeed_radpersec(testSpeed_mps);
    double resultRPM = Units.radiansPerSecondToRotationsPerMinute(resultSpeed_rps);
    assertTrue(resultRPM > 6000); //result should be near a falcon 500 runout

  }



}