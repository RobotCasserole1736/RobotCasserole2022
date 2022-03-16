package frc.lib.Webserver2.DashboardConfig;

public abstract class WidgetConfig {

    public String name = "";
    public int idx = 0;
    public String nt4TopicCurVal = "";

    public String getTopicSubscriptionStrings(){
        return "\"" + this.nt4TopicCurVal + "\",";
    }

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
