package frc.lib.Webserver2.DashboardConfig;

public class SwerveStateConfig extends VisibleWidgetConfig {
    
    double nominalWidth  = 30;
    double nominalHeight = 30;

    SwerveStateTopicSet FLTopics;
    SwerveStateTopicSet FRTopics;
    SwerveStateTopicSet BLTopics;
    SwerveStateTopicSet BRTopics;

    public SwerveStateConfig(){
        super();
        this.nominalHeight = 30;
        this.nominalWidth = 30;
    }

    @Override
    public String getJSDeclaration(){
        String retStr = "";
        retStr += String.format("var widget%d = new SwerveState('widget%d', '%s');\n", idx, idx, name);
        retStr += FLTopics.getSubscriptionJS();
        retStr += FRTopics.getSubscriptionJS();
        retStr += BLTopics.getSubscriptionJS();
        retStr += BRTopics.getSubscriptionJS();
        return retStr;
    }

    @Override
    public String getJSSetData(){
        String retStr = "";
        retStr += FLTopics.getJSSetData(idx);
        retStr += FRTopics.getJSSetData(idx);
        retStr += BLTopics.getJSSetData(idx);
        retStr += BRTopics.getJSSetData(idx);
        return retStr;
    }

    @Override
    public String getJSUpdate() {
        return String.format("    widget%d.render();", idx);
    }
}
