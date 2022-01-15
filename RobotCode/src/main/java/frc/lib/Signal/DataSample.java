package frc.lib.Signal;

public class DataSample {
    double value;
    double sample_time_s;
    Signal parentSig; // The signal that this sample belongs to.

    public DataSample(double time_s_in, double val_in, Signal parentSig_in) {
        value = val_in;
        sample_time_s = time_s_in;
        parentSig = parentSig_in;
    }

    public double getSampleTime_s() {
        return sample_time_s;
    }

    public double getVal() {
        return value;
    }

    public Signal getParentSignal() {
        return parentSig;
    }

}
