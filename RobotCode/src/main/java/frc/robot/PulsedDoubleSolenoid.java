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

    boolean isExtended = false;

    boolean isEnergizing = false;
    boolean isEnergizingPrev = false;

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

        //In-state updates to drive the transitions 
        if(isEnergizing){
            if(Timer.getFPGATimestamp() > energizeEnd){
                isEnergizing = false;
            }

        } else {
            if(cmd_in != DoubleSolenoid.Value.kOff && cmd_in_prev != cmd_in){
                isEnergizing = true;
                energizeEnd = Timer.getFPGATimestamp() + ENERGIZE_DURATION;
            }
        }

        // State Transitions into and out of the energizing state
        if(isEnergizing == true && isEnergizingPrev == false){
            cmd_to_valve = cmd_in;
            needsSend = true;
        }
        if(isEnergizing == false && isEnergizingPrev == true){
            cmd_to_valve = DoubleSolenoid.Value.kOff;
            needsSend = true;
        }

        // Transmit commands to valve
        if(needsSend){
            sol.set(cmd_to_valve);

            if(cmd_to_valve == DoubleSolenoid.Value.kForward){
                isExtended = true;
            } else if(cmd_to_valve == DoubleSolenoid.Value.kReverse){
                isExtended = false;
            }

        }

        isEnergizing = isEnergizingPrev;
        cmd_in_prev = cmd_in;
    }

    public boolean isExtended(){
        return isExtended;
    }
    
}
