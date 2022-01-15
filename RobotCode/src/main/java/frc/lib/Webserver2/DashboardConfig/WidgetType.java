package frc.lib.Webserver2.DashboardConfig;

public enum WidgetType {
    AUTO_CHOOSER("AutoChooser"), 
    CAMERA("Camera"),
    CIRCULAR_GAUGE("CircularGauge"),
    ICON("Icon"), 
    LINE_GAUGE("LineGauge"), 
    SOUND("Sound"),
    TEXT("Text"); 

    public final String jsClassName;

    private WidgetType(String jsClassName) {
        this.jsClassName = jsClassName;
    }

    public String getJsClassName() {
        return this.jsClassName;
    }
}
