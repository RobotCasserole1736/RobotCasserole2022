/*
 *******************************************************************************************
 * Copyright (C) 2020 FRC Team 1736 Robot Casserole - www.robotcasserole.org
 *******************************************************************************************
 *
 * This software is released under the MIT Licence - see the license.txt
 *  file in the root of this repo.
 *
 * Non-legally-binding statement from Team 1736:
 *  Thank you for taking the time to read through our software! We hope you
 *   find it educational and informative! 
 *  Please feel free to snag our software for your own use in whatever project
 *   you have going on right now! We'd love to be able to help out! Shoot us 
 *   any questions you may have, all our contact info should be on our website
 *   (listed above).
 *  If you happen to end up using our software to make money, that is wonderful!
 *   Robot Casserole is always looking for more sponsors, so we'd be very appreciative
 *   if you would consider donating to our club to help further STEM education.
 */

package frc.robot;

import edu.wpi.first.wpilibj.PWM;
import frc.Constants;
import edu.wpi.first.wpilibj.DriverStation;

public class LEDController {

    private static LEDController ledCtrl = null;

    PWM ctrl;

    private DriverStation.Alliance curAlliance;

    public static synchronized LEDController getInstance() {
        if (ledCtrl == null)
            ledCtrl = new LEDController();
        return ledCtrl;
    }

    public enum LEDPatterns {
        Pattern0(0), // Red Color Sparkle
        Pattern1(1), // Blue Color Sparkle
        Pattern4(4), // Blue Fade
        Pattern5(5), // Red Fade
        Pattern6(6), // Rainbow Fade Chase
        PatternDisabled(-1); // CasseroleColorStripeChase

        public final int value;

        private LEDPatterns(int value) {
            this.value = value;
        }

        public int toInt() {
            return this.value;
        }
    }

    // This is the private constructor that will be called once by getInstance() and
    // it
    // should instantiate anything that will be required by the class
    private LEDController() {

        ctrl = new PWM(Constants.LED_CONTROLLER_PORT);
       //TODO: Fix following line
        //ctrl.setSafetyEnabled(false);

        Thread monitorThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        ledUpdater();
                        Thread.sleep(200);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Set up thread properties and start it off
        monitorThread.setName("CasseroleLEDThread");
        monitorThread.setPriority(Thread.MIN_PRIORITY);
        monitorThread.start();
    }

    /*
     * This is a Utility Function to tell the updater what alliance color we are
     */

    public void teamColor(){
        curAlliance = DriverStation.getAlliance();
    }

    public void ledUpdater() {
        double matchTime = DriverStation.getMatchTime();
        if (matchTime <= 30 && matchTime >= 0) {
            ctrl.setSpeed(1.0);
        } else if (curAlliance == DriverStation.Alliance.Blue) {
            if (DriverStation.isAutonomous() == true) {
                ctrl.setSpeed(-0.5);
            } else {
                ctrl.setSpeed(0.25);
            }
        } else {
            if (DriverStation.isAutonomous() == true) {
                ctrl.setSpeed(-1.0);
            } else {
                ctrl.setSpeed(0.5);
            }
        }

    }
}