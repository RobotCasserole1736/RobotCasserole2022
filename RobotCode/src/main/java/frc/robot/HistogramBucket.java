package frc.robot;


import frc.lib.Signal.Signal;

public class HistogramBucket {

    double lowerThresh = 0;
    double upperThresh = 0;
    int sampleCount = 0;
    Signal countSig;

    public HistogramBucket(double lowerThresh, double upperThresh){
        this.lowerThresh = lowerThresh;
        this.upperThresh = upperThresh;
        this.countSig = new Signal("Load Monitor Bucket " + Double.toString(lowerThresh) + " to " +  Double.toString(upperThresh), "%");
    }

    public void checkAndInc(double val){
        if(val >= lowerThresh && val < upperThresh ){
            this.sampleCount ++;
        }
    }

    public void updateTelemetry(double timeIn){
        this.countSig.addSample(timeIn, this.sampleCount);
    }
    public int getSampleCount(){
        return sampleCount;
    }

    
}