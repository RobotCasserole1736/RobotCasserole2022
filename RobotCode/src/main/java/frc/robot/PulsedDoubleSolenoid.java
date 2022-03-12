package frc.robot;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.Timer;

public class PulsedDoubleSolenoid {

    DoubleSolenoid sol;

    DoubleSolenoid.Value cmd_in = DoubleSolenoid.Value.kOff;
    DoubleSolenoid.Value cmd_in_prev = DoubleSolenoid.Value.kOff;

    DoubleSolenoid.Value cmd_to_valve = DoubleSolenoid.Value.kOff;

    final double ENERGIZE_DURATION = 0.5;

    double energizeEnd = 0;

    /**
     * Wrappers a DoubleSolenoid, but only pulses the fwd/rev valves and normally leaves it off to reduce
     * current draw from the pneumatics board.
     * @param fwdChannel
     * @param revChannel
     */
    public PulsedDoubleSolenoid(int fwdChannel, int revChannel){
        sol = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, fwdChannel, revChannel);
    }

    public void set(DoubleSolenoid.Value cmd_in){
        this.cmd_in = cmd_in;
    }

    public void update(){

        boolean needsSend = false;

        if(cmd_in != cmd_in_prev){
            energizeEnd = Timer.getFPGATimestamp() + ENERGIZE_DURATION;
            cmd_to_valve = cmd_in;
            needsSend = true;
        }

        if(Timer.getFPGATimestamp() > energizeEnd){
            cmd_to_valve = DoubleSolenoid.Value.kOff;
            needsSend = true;

        }      
        
        if(needsSend){
            sol.set(cmd_to_valve);
        }

        cmd_in_prev = cmd_in;
    }
    
}
