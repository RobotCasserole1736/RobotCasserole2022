package frc.robot;

import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.util.Color;
import frc.lib.Signal.Annotations.Signal;

import com.revrobotics.ColorMatch;
import com.revrobotics.ColorMatchResult;
import com.revrobotics.ColorSensorV3;

public class Ballcolordetector {
    private final I2C.Port i2cPort = I2C.Port.kMXP;
    private final ColorSensorV3 m_colorSensor = new ColorSensorV3(i2cPort);

    private final ColorMatch m_colorMatcher = new ColorMatch();

    private final Color kBlueTarget = new Color(0.143, 0.427, 0.429);
    private final Color kRedTarget = new Color(0.561, 0.232, 0.114);

    @Signal
    boolean blueDetected = false;
    @Signal
    boolean redDetected = false;

    public Ballcolordetector() {
        m_colorMatcher.addColorMatch(kBlueTarget);
        m_colorMatcher.addColorMatch(kRedTarget);
        m_colorMatcher.setConfidenceThreshold(0.5);
    }

    public void update() {
        Color detectedColor = m_colorSensor.getColor();

        ColorMatchResult match = m_colorMatcher.matchClosestColor(detectedColor);

        if (match.color == kBlueTarget) {
            blueDetected = true;
            redDetected = false;
        } else if (match.color == kRedTarget) {
            blueDetected = false;
            redDetected = true;
        } else {
            blueDetected = false;
            redDetected = false;
        }
    }
}