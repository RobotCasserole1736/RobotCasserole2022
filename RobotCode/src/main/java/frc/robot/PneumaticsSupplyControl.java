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
    boolean compressorEnableCmdPrev;

    private static PneumaticsSupplyControl inst = null;
	public static synchronized PneumaticsSupplyControl getInstance() {
		if(inst == null)
            inst = new PneumaticsSupplyControl();
		return inst;
	}
    
    private PneumaticsSupplyControl () {
        phCompressor = new Compressor(1, PneumaticsModuleType.REVPH);
        phCompressor.enableAnalog(100, 120);

		// Kick off monitor in brand new thread.
	    // Thanks to Team 254 for an example of how to do this!
	    Thread monitorThread = new Thread(new Runnable() {
	        @Override
	        public void run() {
	            try {
	            	while(!Thread.currentThread().isInterrupted()){
	            		update();
	            		Thread.sleep(250);
	            	}
	            } catch (Exception e) {
	                e.printStackTrace();
	            }

	        }
		});

	    //Set up thread properties and start it off
	    monitorThread.setName("PneumaticsSupplyControl");
	    monitorThread.setPriority(Thread.MIN_PRIORITY);
	    monitorThread.start();
    }

    public void setCompressorEnabledCmd(boolean cmd_in){
        compressorEnableCmd = cmd_in;
    }

    private void update(){
        storagePressure= phCompressor.getPressure(); //using the rev pressure sensor
        compressorCurrent = phCompressor.getCurrent();

        if(compressorEnableCmdPrev != compressorEnableCmd){
            if(compressorEnableCmd){
                phCompressor.enableAnalog(100, 120);
            } else {
                phCompressor.disable();
            }
        }

        compressorEnableCmdPrev = compressorEnableCmd;
    }

    public double getStoragePressure(){
        return storagePressure;
    }
}

