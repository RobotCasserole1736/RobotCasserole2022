package frc.lib.Signal;

public class SignalUtils {

    public static String nameToNT4ValueTopic(String name){ return "/Signals/" + name + "/value"; }
    public static String nameToNT4UnitsTopic(String name){ return "/Signals/" + name + "/units"; }

    
}
