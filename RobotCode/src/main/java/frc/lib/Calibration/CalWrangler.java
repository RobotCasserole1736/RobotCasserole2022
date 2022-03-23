package frc.lib.Calibration;

/*
 *******************************************************************************************
 * Copyright (C) FRC Team 1736 Robot Casserole - www.robotcasserole.org
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

import java.util.ArrayList;
import java.util.HashSet;

import edu.wpi.first.wpilibj.DriverStation;
import frc.lib.miniNT4.LocalClient;
import frc.lib.miniNT4.samples.TimestampedValue;
import frc.lib.miniNT4.topics.Topic;

/**
 * DESCRIPTION: <br>
 * Calibration Wrangler. Manages the full set of calibrations in the software.
 */

public class CalWrangler extends LocalClient {

    /* Singleton infrastructure */
    private static CalWrangler instance;

    public static CalWrangler getInstance() {
        if (instance == null) {
            instance = new CalWrangler();
        }
        return instance;
    }

    private CalWrangler(){
        super();
    }

    /** Full set of all registered calibrations on this robot */
    public ArrayList<Calibration> registeredCals = new ArrayList<Calibration>(0);

    /**
     * Resets all registered calibrations back to default values
     * 
     * @return 0 on success, nonzero on failure
     */

    public int resetAllCalsToDefault() {
        synchronized(registeredCals){
            for (Calibration cal : registeredCals) {
                cal.reset();
            }
        }
        return 0;
    }

    /**
     * Register a calibration with the wrangler. Registration adds a reference to
     * the calibration to the wrangler so when the wrangler is called upon to update
     * calibration values, it knows which values it should be changing. This
     * function is called automatically by the constructor for calibrations. Unless
     * something very intersting is happening, the user should never have to call
     * it.
     * 
     * @param cal_in The calibration to add to this wrangler.
     * @return 0 on success, nonzero on failure
     */
    public int register(Calibration cal_in) {
        int ret_val = 0;
        synchronized(registeredCals){
            if (registeredCals.contains(cal_in)) {
                DriverStation.reportWarning("[CalWrangler] WARNING: " + cal_in.name
                        + " has already been added to the cal wrangler. Nothing done.", false);
                ret_val = -1;
            } else {
                registeredCals.add(cal_in);

                ret_val = 0;
            }
        }
        return ret_val;
    }

    /**
     * Return a reference to a registered calibration, given its name. Case
     * sensitive.
     * 
     * @param name_in Name of the calibration to look up.
     * @return Reference to calibration, or null if no registered cal matches the
     *         name.
     */
    public Calibration getCalFromName(String name_in) {
        synchronized(registeredCals){
            for (Calibration cal : registeredCals) {
                if (cal.name.equals(name_in)) {
                    return cal;
                }
            }
        }
        return null;
    }

    public void subscribeAll(){
        var subs = new HashSet<String>(1);

        synchronized(registeredCals){
            for (Calibration cal : registeredCals) {
                subs.add(cal.getValueTopic());
            }
        }

        this.subscribe(subs).start();
    }

    @Override
    public void onAnnounce(Topic newTopic) {
        //nothing to do
    }

    @Override
    public void onUnannounce(Topic deadTopic) {
        //nothing to do
    }

    @Override
    public void onValueUpdate(Topic topic, TimestampedValue newVal) {
        synchronized(registeredCals){
            for(Calibration cal : registeredCals){
                if(cal.getValueTopic().equals(topic.name)){
                    cal.setOverride((Double) newVal.getVal());
                }
            }
        }
    }

}
