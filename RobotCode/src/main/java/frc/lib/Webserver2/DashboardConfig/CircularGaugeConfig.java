package frc.lib.Webserver2.DashboardConfig;

public class CircularGaugeConfig extends VisibleWidgetConfig {
    
    double minRange;
    double maxRange;
    double minAcceptable;
    double maxAcceptable;

    public CircularGaugeConfig(){
        super();
        this.nominalHeight = 20;
        this.nominalWidth = 20;
    }

    @Override
    public String getJSDeclaration(){
        String retStr = "";
        retStr += String.format("var widget%d = new CircularGauge('widget%d', '%s', %f,%f,%f,%f);\n", idx, idx, name, minRange, maxRange, minAcceptable, maxAcceptable);
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
