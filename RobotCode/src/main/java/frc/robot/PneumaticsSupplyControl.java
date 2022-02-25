package frc.robot;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import frc.lib.Signal.Annotations.Signal;

public class PneumaticsSupplyControl {

    Compressor phCompressor;
    
    @Signal (units = "PSI")
    double currentPressure;
    
    public PneumaticsSupplyControl () {
        phCompressor = new Compressor(1, PneumaticsModuleType.REVPH);
        phCompressor.enableDigital();
    }

    public void setCompressorEnabledCmd(boolean cmd_in){
        if(cmd_in){
            phCompressor.enableDigital();
        } else {
            phCompressor.disable();
        }
        
    }

    public void setCompressorRun(boolean run_cmd){

    }

    public void update(){
        currentPressure = phCompressor.getPressure();

    }
}

