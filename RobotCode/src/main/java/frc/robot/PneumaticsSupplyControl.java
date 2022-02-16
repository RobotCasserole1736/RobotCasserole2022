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

    public void setCompressorEnabled(boolean enable){
        //enable = phCompressor.enabled();
        //Is above necessary? And if statement here or in update?

    }

    public void update(){
        
        currentPressure = phCompressor.getPressure();

    }
}

