package frc.lib.Webserver2.DashboardConfig;

import java.util.List;

public class AutoChooserConfig extends VisibleWidgetConfig {

    List<String> modeNameList;

    String nt4TopicDesVal = "";

    public AutoChooserConfig(){
        super();
        this.nominalHeight = 5;
        this.nominalWidth = 40;
    }

    private String getJsModeNameListString() {
        String retVal = "";
        retVal += "[";
        for (int idx = 0; idx < modeNameList.size(); idx++) {
            retVal += "'" + modeNameList.get(idx) + "'";
            if (idx != (modeNameList.size() - 1)) {
                retVal += ",";
            }
        }
        retVal += "]";
        return retVal;
    }

    @Override
    public String getJSDeclaration() {
        String retStr = String.format("var widget%d = new AutoChooser('widget%d', '%s', %s, onWidget%dValUpdated);\n", idx, idx,
                name, getJsModeNameListString(), idx);
        retStr += String.format("nt4Client.publishNewTopic(\"%s\", \"int\");", nt4TopicDesVal);
        return retStr;
    }

    @Override
    public String getJSSetData(){
        String retStr = "";
        retStr += "if(name == \"" + nt4TopicCurVal + "\"){ \n";
        retStr += String.format("    widget%d.setActualState(value);\n", idx);
        retStr += "}";
        return retStr;
    }

    @Override
    public String getJSUpdate() {
        return String.format("    widget%d.render();", idx);
    }

    @Override
    public String getJSSetNoData(){
        String retStr = "";
        retStr += String.format("    widget%d.reportNoData();", idx);
        return retStr;
    }

    public String getJSCallback() {
        String retStr = "";
        retStr += String.format("function onWidget%dValUpdated(value) {\n", idx);
        retStr += String.format("    nt4Client.addSample(\"%s\", nt4Client.getServerTime_us(), value);\n", nt4TopicDesVal);
        retStr += "}";
        return retStr;

    }

}
