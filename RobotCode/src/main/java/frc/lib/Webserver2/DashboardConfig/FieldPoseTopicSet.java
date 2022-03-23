package frc.lib.Webserver2.DashboardConfig;

import frc.lib.Signal.SignalUtils;
import frc.lib.Webserver2.DashboardConfig.FieldPose.PoseType;

public class FieldPoseTopicSet {
    String poseXTopic;
    String poseYTopic;
    String poseRotTopic;

    PoseType type;

    public FieldPoseTopicSet(String root, PoseType type){
        this.poseXTopic = SignalUtils.nameToNT4ValueTopic(root + "_" + type.toStr() + "_x");
        this.poseYTopic = SignalUtils.nameToNT4ValueTopic(root + "_" + type.toStr() + "_y");
        this.poseRotTopic = SignalUtils.nameToNT4ValueTopic(root + "_" + type.toStr() + "_rot");
        this.type = type;
    }

    String getSubscriptionStrings(){
        String retStr = "";
        retStr += "\"" + poseXTopic + "\",";
        retStr += "\"" + poseYTopic + "\",";
        retStr += "\"" + poseRotTopic + "\",";
        return retStr;
    }


    String getJSSetData(int widgetIdx){
        String retStr = "";
        retStr += "if(name == \"" + poseXTopic + "\"){ \n";
        retStr += String.format("    widget%d.setVal(%d, 0, value);\n", widgetIdx, type.toInt());
        retStr += "}\n";
        retStr += "if(name == \"" + poseYTopic + "\"){ \n";
        retStr += String.format("    widget%d.setVal(%d, 1, value);\n", widgetIdx, type.toInt());
        retStr += "}\n";
        retStr += "if(name == \"" + poseRotTopic + "\"){ \n";
        retStr += String.format("    widget%d.setVal(%d, 2, value);\n", widgetIdx, type.toInt());
        retStr += "}\n";
        return retStr;
    }

}