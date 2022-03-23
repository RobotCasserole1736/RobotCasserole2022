package frc.lib.Webserver2.DashboardConfig;

import frc.lib.Signal.SignalUtils;

public class SwerveStateTopicSet {
    String azmthDesTopic = "";
    String azmthActTopic = "";
    String speedDesTopic = "";
    String speedActTopic = "";

    int modIdx = 0;

    public static final String PREFIX = "DtModule_";
    public static final String SUFFIX_AZMTH_DES = "_azmthDes";
    public static final String SUFFIX_AZMTH_ACT = "_azmthAct";
    public static final String SUFFIX_WHEEL_DES = "_speedDes";
    public static final String SUFFIX_WHEEL_ACT = "_speedAct";


    public SwerveStateTopicSet(String modName, int modIdx_in){
        azmthDesTopic = SignalUtils.nameToNT4ValueTopic(PREFIX + modName + SUFFIX_AZMTH_DES);
        azmthActTopic = SignalUtils.nameToNT4ValueTopic(PREFIX + modName + SUFFIX_AZMTH_ACT);
        speedDesTopic = SignalUtils.nameToNT4ValueTopic(PREFIX + modName + SUFFIX_WHEEL_DES);
        speedActTopic = SignalUtils.nameToNT4ValueTopic(PREFIX + modName + SUFFIX_WHEEL_ACT);
        modIdx = modIdx_in;
    }

    String getSubscriptionStrings(){
        String retStr = "";
        retStr += "\"" + azmthDesTopic + "\",";
        retStr += "\"" + azmthActTopic + "\",";
        retStr += "\"" + speedDesTopic + "\",";
        retStr += "\"" + speedActTopic + "\",";
        return retStr;
    }

    String getJSSetData(int widgetIdx){
        String retStr = "";
        retStr += "if(name == \"" + azmthDesTopic + "\"){ \n";
        retStr += String.format("    widget%d.setVal(%d, 0, value);\n", widgetIdx, modIdx);
        retStr += "}\n";
        retStr += "if(name == \"" + azmthActTopic + "\"){ \n";
        retStr += String.format("    widget%d.setVal(%d, 1, value);\n", widgetIdx, modIdx);
        retStr += "}\n";
        retStr += "if(name == \"" + speedDesTopic + "\"){ \n";
        retStr += String.format("    widget%d.setVal(%d, 2, value);\n", widgetIdx, modIdx);
        retStr += "}\n";
        retStr += "if(name == \"" + speedActTopic + "\"){ \n";
        retStr += String.format("    widget%d.setVal(%d, 3, value);\n", widgetIdx, modIdx);
        retStr += "}\n";
        return retStr;
    }

}