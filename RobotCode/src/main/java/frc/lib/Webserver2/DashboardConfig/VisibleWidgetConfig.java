package frc.lib.Webserver2.DashboardConfig;

public abstract class VisibleWidgetConfig extends WidgetConfig {

    public double xPos = 0.0;
    public double yPos = 0.0;
    public double sizeScaleFactor = 1.0;
    double nominalWidth  = 0.0;
    double nominalHeight = 0.0;

    @Override
    public String getHTML(){
        return genHtmlDeclaration();
    }
    
    String genHtmlDeclaration() {
        double height = nominalHeight * sizeScaleFactor;
        double width = nominalWidth * sizeScaleFactor;
        return "<div class=\"widgetBase\" style=\"top:" + Double.toString(yPos-height/2) + "%;left:" + Double.toString(xPos-width/2)
                + "%;height:" + Double.toString(height) + "vw;width:" + Double.toString(width) + "vw\" id=\"widget"
                + Integer.toString(idx) + "\"></div>";
    }

}
