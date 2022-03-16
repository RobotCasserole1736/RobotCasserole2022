package frc.lib.Webserver2.DashboardConfig;

public class SoundConfig extends WidgetConfig {

    String filePath = "";
    boolean looping = false;


    @Override
    public String getJSDeclaration(){
        String retStr = String.format("var widget%d = new Sound('%s', '%s', %s);\n", idx, name, filePath, looping?"true":"false");
        return retStr;
    }

    @Override
    public String getJSSetData(){
        String retStr = "";
        retStr += "if(name == \"" + nt4TopicCurVal + "\"){ ";
        retStr += String.format("    widget%d.setVal(value);", idx);
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
    
}
