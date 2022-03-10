package frc.lib.LoadMon;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Tracer;

public class SegmentTimeTracker {
    private Tracer trace;
    private String name;

    private final double max_dur_sec;


    public double loopPeriodSec;
    public double loopDurationSec;

    double startTimeSec;

    
    public SegmentTimeTracker(String name, double max_dur_sec){
        trace = new Tracer();
        this.name = name;
        this.max_dur_sec = max_dur_sec;
    }

    public void start(){
        loopPeriodSec = Timer.getFPGATimestamp() - startTimeSec;
        startTimeSec = Timer.getFPGATimestamp();
        trace.clearEpochs();
    }

    public void mark(String epochName){
        trace.addEpoch(epochName);
    }

    public void end(){
        loopDurationSec = Timer.getFPGATimestamp() - startTimeSec;
        if (loopDurationSec > max_dur_sec) {
            System.out.println(name + " Long Loop Detected:");
            trace.printEpochs(System.out::println);
        }
    }

}
