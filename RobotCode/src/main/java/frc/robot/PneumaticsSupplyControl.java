package frc.robot;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import frc.lib.Signal.Annotations.Signal;

public class PneumaticsSupplyControl {

    Compressor phCompressor;
    
    @Signal (units = "PSI")
    double storagePressure;

    @Signal(units="A")
    double compressorCurrent;

    @Signal
    boolean compressorEnableCmd;

    private static PneumaticsSupplyControl inst = null;
	public static synchronized PneumaticsSupplyControl getInstance() {
		if(inst == null)
            inst = new PneumaticsSupplyControl();
		return inst;
	}
    
    private PneumaticsSupplyControl () {
        phCompressor = new Compressor(1, PneumaticsModuleType.REVPH);
        phCompressor.enableDigital();
    }

    public void setCompressorEnabledCmd(boolean cmd_in){
        compressorEnableCmd = cmd_in;
    }

    public void update(){
        storagePressure= phCompressor.getPressure(); //using the rev pressure sensor
        compressorCurrent = phCompressor.getCurrent();

        if(compressorEnableCmd){
            phCompressor.enableDigital();
        } else {
            phCompressor.disable();
        }
    }

    public double getStoragePressure(){
        return storagePressure;
    }
}

