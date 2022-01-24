package frc.sim;

import edu.wpi.first.wpilibj.simulation.REVPHSim;
import frc.Constants;
import frc.UnitUtils;
import frc.lib.Signal.Annotations.Signal;
import frc.lib.Util.MapLookup2D;

public class PneumaticsSystemSim {

  REVPHSim pneumaticsHub;

  private final int num_tanks = 2;
  private final double tank_volume_L =
      0.574; // (574 mL Clippard air tanks - http://www.andymark.com/product-p/am-2649.htm)
  private final double tank_air_temp_C = 21; // (in DegC. 70degF ~= 21degC)
  private final double switch_pressure_thresh_on_kPa = UnitUtils.psiTokPa(80);
  private final double switch_pressure_thresh_off_kPa = UnitUtils.psiTokPa(125);
  private final double regulator_setpoint_kPa = UnitUtils.psiTokPa(60);

  // Viair 090 air compressor constants (Viair 90C from Andymark -
  // http://www.andymark.com/product-p/am-2005.htm)
  private final double comp_perf_data_sys_press_psi[] = {
    0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 150
  };
  private final double comp_perf_data_flow_ft3_per_min[] = {
    0.88, 0.71, 0.67, 0.64, 0.60, 0.57, 0.53, 0.48, 0.45, 0.43, 0.39, 0.36, 0.34, 0
  };
  private final double comp_perf_data_current_A[] = {7, 8, 8, 9, 9, 9, 10, 10, 10, 10, 9, 9, 9, 9};

  // Physical constants
  private final double gas_const_r = 8.3144621; // (in L*kPa/(K*mol))
  private final double min_press_kpa =
      101.325; // sea level atmospheric pressure - we cannot create a vacuum with a compressor.

  // calculated constants
  private final double tank_total_volume_L = tank_volume_L * num_tanks;
  private final double tank_air_temp_K = tank_air_temp_C + 274.15;
  private final double min_moles_air =
      min_press_kpa * tank_total_volume_L / (gas_const_r * tank_air_temp_K); // (n = PV/rT)

  // State
  @Signal(units = "mols")
  double moles_air_stored = min_moles_air;

  @Signal(units = "lps")
  double extFlow = 0;

  @Signal(units = "lps")
  double totalFlow = 0;

  @Signal(units = "A")
  double compressorCurrent;

  @Signal(units = "kPa")
  double storagePressure;

  @Signal(units = "kPa")
  double supplyPressure;

  @Signal(units = "bool")
  boolean pressureSwitchClosed;

  MapLookup2D compressorFlowLookup;
  MapLookup2D compressorCurrentLookup;

  public PneumaticsSystemSim() {

    pneumaticsHub = new REVPHSim();

    compressorFlowLookup = new MapLookup2D();
    compressorCurrentLookup = new MapLookup2D();

    for (int idx = 0; idx < comp_perf_data_current_A.length; idx++) {
      double current_A = comp_perf_data_current_A[idx];
      double comp_perf_data_sys_press_kPa = UnitUtils.psiTokPa(comp_perf_data_sys_press_psi[idx]);
      double comp_perf_data_flow_L_per_s = comp_perf_data_flow_ft3_per_min[idx] * 0.471947443;
      compressorFlowLookup.insertNewPoint(
          comp_perf_data_sys_press_kPa, comp_perf_data_flow_L_per_s);
      compressorCurrentLookup.insertNewPoint(comp_perf_data_sys_press_kPa, current_A);
    }

    pressureSwitchClosed = true;
  }

  public void setSystemConsumption(double flow_lps) {
    extFlow = -1.0 * flow_lps;
  }

  public void update(boolean isDisabled) {

    if (storagePressure < switch_pressure_thresh_on_kPa) {
      pressureSwitchClosed = true;
    } else if (storagePressure > switch_pressure_thresh_off_kPa) {
      pressureSwitchClosed = false;
    } else {
      // Hysterisis - retain previous state
    }

    boolean compressorRunning = !isDisabled && pressureSwitchClosed;

    totalFlow = extFlow + (compressorRunning ? compressorFlowLookup.lookupVal(storagePressure) : 0);
    compressorCurrent =
        (compressorRunning ? compressorCurrentLookup.lookupVal(storagePressure) : 0);
    double deltaVolume = totalFlow * Constants.SIM_SAMPLE_RATE_SEC;

    // calc moles of air in (n = PV/rT) with saturation since downstream systems shouldn't be able
    // to draw a vaccuum
    double deltaMols = storagePressure * deltaVolume / (gas_const_r * tank_air_temp_K);
    moles_air_stored += deltaMols;
    moles_air_stored = Math.max(moles_air_stored, min_moles_air);

    // Calculate output pressure based on new mole count (P = nrT/V)
    storagePressure =
        moles_air_stored * gas_const_r * tank_air_temp_K / (tank_total_volume_L) - min_press_kpa;
    supplyPressure = Math.min(regulator_setpoint_kPa, storagePressure);

    pneumaticsHub.setCompressorCurrent(compressorCurrent);
    pneumaticsHub.setCompressorOn(compressorRunning);
    pneumaticsHub.setPressureSwitch(pressureSwitchClosed);
  }

  public double getCurrentDraw_A() {
    return compressorCurrent;
  }

  public double getSupplyPressure_kPa() {
    return supplyPressure;
  }

  public void setStoragePressure_kPa(double pressure_kPa) {
    pressure_kPa -= min_press_kpa;
    moles_air_stored = pressure_kPa * tank_total_volume_L / (gas_const_r * tank_air_temp_K);
  }
}
