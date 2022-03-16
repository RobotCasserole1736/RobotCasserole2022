package frc.lib.Webserver2.DashboardConfig;

public class TextConfig extends VisibleWidgetConfig {

    public TextConfig(){
        super();
        this.nominalHeight = 5;
        this.nominalWidth = 20;
    }

    @Override
    public String getJSDeclaration(){
        String retStr = String.format("var widget%d = new Text('widget%d', '%s');\n", idx, idx, name);
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
