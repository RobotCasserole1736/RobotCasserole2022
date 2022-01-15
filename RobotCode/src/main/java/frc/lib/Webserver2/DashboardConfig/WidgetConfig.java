package frc.lib.Webserver2.DashboardConfig;

public abstract class WidgetConfig {

    String name = "";
    public int idx = 0;

    String nt4TopicCurVal = "";

    public String getHTML(){
        return "";
    }

    public String getJSDeclaration(){
        return "";
    }

    public String getJSUpdate() {
        return "";
    }

    public String getJSSetData() {
        return "";
    }

    public String getJSSetNoData() {
        return "";
    }

    public String getJSCallback() {
        return "";
    }

}
