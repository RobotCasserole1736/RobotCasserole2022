package frc.lib.Webserver2.DashboardConfig;

public class FieldPose extends VisibleWidgetConfig {
    
    double minRange;
    double maxRange;
    double minAcceptable;
    double maxAcceptable;

    FieldPoseTopicSet desTopics;
    FieldPoseTopicSet actTopics;
    FieldPoseTopicSet expTopics;

    
    public enum PoseType {
        Desired(0, "DES"), 
        Estimated(1, "EST"), 
        Actual(2, "ACT"); 
        public final int value;
        public final String abrev;
        private PoseType(int value, String abrev) { this.value = value; this.abrev = abrev;}
        public int toInt() {return this.value;}
        public String toStr() {return this.abrev;}
    }

    public FieldPose(){
        super();
        this.nominalHeight = 20;
        this.nominalWidth = 40;
    }

    @Override
    public String getJSDeclaration(){
        String retStr = "";
        retStr += String.format("var widget%d = new FieldPose('widget%d', '%s');\n", idx, idx, name);
        return retStr;
    }
    
    @Override
    public String getTopicSubscriptionStrings(){
        String retStr = "";
        retStr += desTopics.getSubscriptionStrings();
        retStr += actTopics.getSubscriptionStrings();
        retStr += expTopics.getSubscriptionStrings();
        return retStr;
    }

    @Override
    public String getJSSetData(){
        String retStr = "";
        retStr += desTopics.getJSSetData(idx);
        retStr += actTopics.getJSSetData(idx);
        retStr += expTopics.getJSSetData(idx);
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
