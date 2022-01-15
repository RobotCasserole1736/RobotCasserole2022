package frc.lib.Webserver2.DashboardConfig;

public class CameraConfig extends VisibleWidgetConfig {
    
    String streamURL = "";

    public CameraConfig(){
        super();
        this.nominalHeight = 30;
        this.nominalWidth = 40;
    }

    @Override
    public String getJSDeclaration(){
        return String.format("var widget%d = new Camera('widget%d', '%s', '%s');", idx, idx, name, streamURL);
    }
    
}
