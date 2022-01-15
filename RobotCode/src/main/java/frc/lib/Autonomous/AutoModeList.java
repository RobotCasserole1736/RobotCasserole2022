package frc.lib.Autonomous;

import java.util.ArrayList;
import java.util.TreeMap;

public class AutoModeList {

    private TreeMap<String, AutoMode> modeList = new TreeMap<String, AutoMode>();
    private ArrayList<String> orderedModeNameList = new ArrayList<String>(); //Helps keep track of the order the modes were added in, to ensure they end up ordered that same way in the web UI.

    public String name;

    public AutoModeList(String name){
        this.name = name;
    }

    public void add(AutoMode in){
        in.idx = modeList.size();
        modeList.put(in.humanReadableName, in);
        orderedModeNameList.add(in.humanReadableName);
    }

    public AutoMode get(String name){
        return modeList.get(name);
    }

    public AutoMode get(int idx){
        return this.get(orderedModeNameList.get(idx));
    }

    public ArrayList<String> getNameList(){
        return orderedModeNameList;
    }

    public AutoMode getDefault(){
        return modeList.get(orderedModeNameList.get(0)); //TBD - just the first thing added?
    }

    public String getDesModeTopicName(){
        return "/Autonomous/desMode" + this.name;
    }

    public String getCurModeTopicName(){
        return "/Autonomous/curMode" + this.name;
    }
    
}
