package frc.lib.Webserver2.DashboardConfig;

import frc.lib.Signal.SignalUtils;

public class SwerveStateTopicSet {
    String azmthDesTopic = "";
    String azmthActTopic = "";
    String speedDesTopic = "";
    String speedActTopic = "";

    int modIdx = 0;


    public SwerveStateTopicSet(String root, int modIdx_in){
        azmthDesTopic = SignalUtils.nameToNT4ValueTopic(root + "_azmthDes");
        azmthActTopic = SignalUtils.nameToNT4ValueTopic(root + "_azmthAct");
        speedDesTopic = SignalUtils.nameToNT4ValueTopic(root + "_speedDes");
        speedActTopic = SignalUtils.nameToNT4ValueTopic(root + "_speedAct");
        modIdx = modIdx_in;
    }

    String getSubscriptionJS(){
        String retStr = "";
        retStr += String.format("nt4Client.subscribePeriodic([\"%s\"], 0.05);\n", azmthDesTopic);
        retStr += String.format("nt4Client.subscribePeriodic([\"%s\"], 0.05);\n", azmthActTopic);
        retStr += String.format("nt4Client.subscribePeriodic([\"%s\"], 0.05);\n", speedDesTopic);
        retStr += String.format("nt4Client.subscribePeriodic([\"%s\"], 0.05);\n", speedActTopic);
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