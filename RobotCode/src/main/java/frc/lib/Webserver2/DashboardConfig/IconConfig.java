package frc.lib.Webserver2.DashboardConfig;

public class IconConfig extends VisibleWidgetConfig {
    
    String colorOn = "";
    String symbolPath = "";

    //Mirror the state definitions from JS
    final static int kOFF = 0;
    final static int kON = 1;
    final static int kBLINK_FAST = 2;
    final static int kBLINK_SLOW = 3;
    
    public IconConfig(){
        super();
        this.nominalHeight = 5;
        this.nominalWidth = 5;
    }

    @Override
    public String getJSDeclaration(){
        String retStr = String.format("var widget%d = new Icon('widget%d', '%s', '%s', '%s');\n", idx, idx, name, colorOn, symbolPath);
        return retStr;
    }

    @Override
    public String getJSSetData(){
        String retStr = "";
        retStr += "if(name == \"" + nt4TopicCurVal + "\"){ \n";
        retStr += "    if(value == 1){ \n";
        retStr += String.format("        widget%d.setVal(Icon.kON);\n", idx);
        retStr += "    } else if(value == 2) {\n";
        retStr += String.format("        widget%d.setVal(Icon.kBLINK_FAST);\n", idx);
        retStr += "    } else if(value == 3) {\n";
        retStr += String.format("        widget%d.setVal(Icon.kBLINK_SLOW);\n", idx);
        retStr += "    } else {\n";
        retStr += String.format("        widget%d.setVal(Icon.kOFF);\n", idx);
        retStr += "    }";
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
