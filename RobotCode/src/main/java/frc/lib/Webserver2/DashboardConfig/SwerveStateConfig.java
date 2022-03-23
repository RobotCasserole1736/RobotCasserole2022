package frc.lib.Webserver2.DashboardConfig;

public class SwerveStateConfig extends VisibleWidgetConfig {
    
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
        return retStr;
    }

    @Override
    public String getTopicSubscriptionStrings(){
        String retStr = "";
        retStr += FLTopics.getSubscriptionStrings();
        retStr += FRTopics.getSubscriptionStrings();
        retStr += BLTopics.getSubscriptionStrings();
        retStr += BRTopics.getSubscriptionStrings();
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
