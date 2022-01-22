package frc.robot;

import javax.net.ssl.X509ExtendedTrustManager;

import edu.wpi.first.wpilibj.Timer;
import frc.lib.Signal.Annotations.Signal;

public class Histogram{
    double[] buckets = {0,1,2,3,4,5,7,10,15,20,25,35,60,Double.MAX_VALUE};
    HistogramBucket[] histBuckets = new HistogramBucket[13];
    double loopStartTime;
    double loopEndTime;
    double prevLoopStartTime;
    double prevLoopEndTime;

    @Signal
    double loopPeriodSec;

    @Signal
    double loopDurationSec;


    /* Singleton stuff */
    private static Histogram histogram = null;
    public static synchronized Histogram getInstance() {
        if(histogram == null)
            histogram = new Histogram();
        return histogram;
    }

    private Histogram(){
        for (int x = 0; x < histBuckets.length; x++){
            histBuckets[x] = new HistogramBucket(buckets[x], buckets[x + 1]);
        }
    }

    public void markLoopStart(){
        prevLoopStartTime = loopStartTime;
        loopStartTime = Timer.getFPGATimestamp();
        loopPeriodSec = loopStartTime - prevLoopStartTime;
    }

    public void markLoopEnd(){
        prevLoopEndTime = loopEndTime;
        loopEndTime = Timer.getFPGATimestamp();
        loopDurationSec = loopEndTime - loopStartTime;
        updateBuckets();
    }

    public double getLoopStartTimeSec(){
        return loopStartTime;
    }

    public double getPeriodSec(){
        return loopPeriodSec;
    }
    private void updateBuckets(){
        
        for (int y = 0; y < buckets.length; y ++){
            histBuckets[y].updateTelemetry(loopEndTime);
            histBuckets[y].checkAndInc(loopDurationSec);
        }
    }
    public double[] getHistgram(){
        double[] percentage = new double[histBuckets.length];
        int totalCount = 0;
        for (int y = 0; y < histBuckets.length; y++){
            totalCount += histBuckets[y].getSampleCount();
        }
        for (int x = 0; x < histBuckets.length; x++){
             percentage[x] = (double)histBuckets[x].getSampleCount() / totalCount * 100;
        }
        return percentage;

    }
    public HistogramBucket[] getHistogramBuckets(){
        return histBuckets;
    }
}