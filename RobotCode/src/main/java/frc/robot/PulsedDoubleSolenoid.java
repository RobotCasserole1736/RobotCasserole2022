package frc.robot;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.Timer;

public class PulsedDoubleSolenoid {

    DoubleSolenoid sol;

    DoubleSolenoid.Value cmd = DoubleSolenoid.Value.kOff;
    DoubleSolenoid.Value cmd_prev = DoubleSolenoid.Value.kOff;

    final double ENERGIZE_DURATION = 0.5;

    double energizeEnd = 0;

    /**
     * Wrappers a DoubleSolenoid, but only pulses the fwd/rev valves and normally leaves it off to reduce
     * current draw from the pneumatics board.
     * @param fwdChannel
     * @param revChannel
     */
    public PulsedDoubleSolenoid(int fwdChannel, int revChannel){
        sol = new DoubleSolenoid (PneumaticsModuleType.CTREPCM, fwdChannel, revChannel);
    }

    public void set(DoubleSolenoid.Value cmd_in){
        cmd = cmd_in;
    }

    public void update(){

        if(cmd != cmd_prev){
            energizeEnd = Timer.getFPGATimestamp() + ENERGIZE_DURATION;
            sol.set(cmd);
        }

        if(Timer.getFPGATimestamp() > energizeEnd){
            sol.set(DoubleSolenoid.Value.kOff);
        }        

        cmd_prev = cmd;
    }
    
}
