package frc.lib.Calibration;

import java.util.HashSet;

import edu.wpi.first.wpilibj.DriverStation;
import frc.lib.miniNT4.NT4Server;
import frc.lib.miniNT4.NT4TypeStr;
import frc.lib.miniNT4.samples.TimestampedDouble;
import frc.lib.miniNT4.samples.TimestampedString;
import frc.lib.miniNT4.topics.Topic;

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

/**
 * DESCRIPTION: <br>
 * Single Calibration. Describes a piece of data which is usually constant, but
 * can be overridden by a cal wrangler from a csv file on the RIO's filesystem.
 * This enables a software team to control what a pit crew has control over
 * (Shooter speed is a good candidate. Port number for left drivetrain motor A
 * is a bad candidate). <br>
 * <br>
 * USAGE:
 * <ol>
 * <li>Instantiate the calibration with a default value, and reference to the
 * wrangler</li>
 * <li>At runtime, use the get() method to read the calibrated value. The
 * returned value may change depending on what the wrangler has
 * overwritten.</li>
 * </ol>
 * 
 * 
 */

public class Calibration {
    /** Default value the calibration will take on. */
    public final double default_val;
    /** Human-readable name for the calibration. */
    public final String name;
    public final String units;
    /**
     * Present value for the calibration. Starts at default_val, but might get
     * changed
     */
    public volatile double cur_val;
    /** True if the user has (somehow) made this calibration a non-default value */
    public volatile boolean overridden;
    /**
     * Gets set to true every time the cal value is changed. Software may optionally
     * watch this boolean to see if the user has commanded a change, and then call
     * the acknowledgeValUpdate() method to indicate they have processed the new
     * value
     */
    private boolean is_updated;
    /** Upper limit on the allowed calibration range */
    public double max_cal;
    /** Lower limit on the allowed calibration range */
    public double min_cal;

    Topic calUnitsTopic;
    Topic calMinTopic;
    Topic calMaxTopic;
    Topic calDefaultTopic;
    Topic calValueTopic;

    /**
     * Constructor for a new calibratable value.
     * 
     * @param name_in        String for the name of the calibration. Best to make it
     *                       the same of the variable name.
     * @param default_val_in Default value for the calibration. Will keep this value
     *                       unless the wrangler overwrites it.
     */
    public Calibration(String name_in,  double default_val_in) {

        /* default stuff and stuff */
        default_val = default_val_in;
        cur_val = default_val;
        this.name = name_in;
        overridden = false;
        is_updated = false;
        this.units = "";
        min_cal = Double.NEGATIVE_INFINITY;
        max_cal = Double.POSITIVE_INFINITY;
        commonConstructor();
    }


    /**
     * Constructor for a new calibratable value.
     * 
     * @param name_in        String for the name of the calibration. Best to make it
     *                       the same of the variable name.
     * @param default_val_in Default value for the calibration. Will keep this value
     *                       unless the wrangler overwrites it.
     */
    public Calibration(String name_in, String units, double default_val_in) {

        /* default stuff and stuff */
        default_val = default_val_in;
        cur_val = default_val;
        this.name = name_in;
        overridden = false;
        is_updated = false;
        this.units = units;
        min_cal = Double.NEGATIVE_INFINITY;
        max_cal = Double.POSITIVE_INFINITY;
        commonConstructor();
    }

    /**
     * Constructor for a new calibratable value with range limiting
     * 
     * @param name_in        String for the name of the calibration. Best to make it
     *                       the same of the variable name.
     * @param default_val_in Default value for the calibration. Will keep this value
     *                       unless the wrangler overwrites it.
     * @param wrangler_in    Reference to the wrangler which will control this
     *                       calibration.
     * @param min_in         Minimum allowable calibration value. If a user attempts
     *                       to override the value outside this range, a WARNING:
     *                       will be thrown and the calibrated value will be capped
     *                       at the minimum.
     * @param max_in         Maximum allowable calibration value. If a user attempts
     *                       to override the value outside this range, a WARNING:
     *                       will be thrown and the calibrated value will be capped
     *                       at the maximum.
     */
    public Calibration(String name_in, double default_val_in, double min_in, double max_in) {

        /* default stuff and stuff */
        this.name = name_in;
        min_cal = min_in;
        max_cal = max_in;
        this.units="";

        default_val = limitRange(default_val_in);
        cur_val = default_val;

        commonConstructor();
    }


    /**
     * Constructor for a new calibratable value with range limiting
     * 
     * @param name_in        String for the name of the calibration. Best to make it
     *                       the same of the variable name.
     * @param default_val_in Default value for the calibration. Will keep this value
     *                       unless the wrangler overwrites it.
     * @param wrangler_in    Reference to the wrangler which will control this
     *                       calibration.
     * @param min_in         Minimum allowable calibration value. If a user attempts
     *                       to override the value outside this range, a WARNING:
     *                       will be thrown and the calibrated value will be capped
     *                       at the minimum.
     * @param max_in         Maximum allowable calibration value. If a user attempts
     *                       to override the value outside this range, a WARNING:
     *                       will be thrown and the calibrated value will be capped
     *                       at the maximum.
     */
    public Calibration(String name_in, String units, double default_val_in, double min_in, double max_in) {

        /* default stuff and stuff */
        this.name = name_in;
        min_cal = min_in;
        max_cal = max_in;
        this.units=units;

        default_val = limitRange(default_val_in);
        cur_val = default_val;

        commonConstructor();
    }

    private void commonConstructor() {
        overridden = false;
        is_updated = false;

        calUnitsTopic   = NT4Server.getInstance().publishTopic(this.getUnitsTopic(),   NT4TypeStr.STR, CalWrangler.getInstance());
        calMinTopic     = NT4Server.getInstance().publishTopic(this.getMinTopic(),     NT4TypeStr.FLOAT_64, CalWrangler.getInstance());
        calMaxTopic     = NT4Server.getInstance().publishTopic(this.getMaxTopic(),     NT4TypeStr.FLOAT_64, CalWrangler.getInstance());
        calDefaultTopic = NT4Server.getInstance().publishTopic(this.getDefaultTopic(), NT4TypeStr.FLOAT_64, CalWrangler.getInstance());
        calValueTopic   = NT4Server.getInstance().publishTopic(this.getValueTopic(),   NT4TypeStr.FLOAT_64, CalWrangler.getInstance());

        calUnitsTopic.submitNewValue(new TimestampedString(this.units, 0));
        calMinTopic.submitNewValue(new TimestampedDouble(this.min_cal, 0));
        calMaxTopic.submitNewValue(new TimestampedDouble(this.max_cal, 0));
        calDefaultTopic.submitNewValue(new TimestampedDouble(this.default_val, 0));
        calValueTopic.submitNewValue(new TimestampedDouble(this.cur_val, 0));

        CalWrangler.getInstance().register(this);

    }

    private double limitRange(double in) {
        double temp;
        // Cross-check that default value is in-range
        if (in < min_cal) {
            DriverStation.reportWarning("WARNING: Calibration: Requested value for " + name
                    + " is too small. Setting value to minimum value of " + Double.toString(min_cal), false);
            temp = min_cal;
        } else if (in > max_cal) {
            DriverStation.reportWarning("WARNING: Calibration: Requested value for " + name
                    + " is too large. Setting value to maximum value of " + Double.toString(max_cal), false);
            temp = max_cal;
        } else {
            temp = in;
        }
        return temp;
    }

    /**
     * Retrieve the present value of this calibration. This is the method to use
     * whenever the calibratable value is to be read.
     * 
     * @return Present value of the calibration
     */
    public double get() {
        if (overridden)
            return cur_val;
        else
            return default_val;
    }

    /**
     * Check if the calibration has been changed by the user. Once the calibration
     * has been changed, this method will continue to return true until the user
     * calls the acknowledgeValUpdate() method. <br>
     * <br>
     * The intent of this functionality is to allow software the ability to run
     * special procedures if the value changes (ex: update and reset PID tune
     * values). The acknowledgement makes sure any code that cares about cal value
     * changes can always see when a new one occurs. <br>
     * <br>
     * Note you never technically have to worry about this, for most cases. If
     * you're reading the cal value every loop, you'll probably be ok. The bigger
     * thing is when you need to not only read the cal value, but also start over
     * some other piece of code or something like that. <br>
     * <br>
     * As I was typing the above paragraph, I began to have flashbacks to my
     * parallel processing classes in college, and just poked about 23472394732894
     * holes in my previous logic about "always seeing changes in cal values".
     * There's lots of ways to break it, especially if the cal updater and the code
     * that cares are in different theads. Ah well. The redeeming characteristic of
     * this case is the cal values <i>hopefully</i> won't change too fast, because
     * they're human entered. This <i>should</i> mean that we always see the cycle
     * of change-process-acknowledge cleanly, but if we don't, well.... hmmm...
     * let's just cross our fingers for now. <br>
     * <br>
     * Isn't software fun?
     * 
     * @return True if the calibration's value has changed at least once since the
     *         last acknowledgment, false if not.
     */
    public boolean isChanged() {
        return is_updated;
    }

    /**
     * For bookkeeping purposes, mark that the cal value update has been processed.
     * Not at all required to worry about, but can help with bookkeeping with code
     * that must do something when cal values are changed.
     */
    public void acknowledgeValUpdate() {
        if(is_updated)
            System.out.println("Info: Calibration " + this.name + " acknowledged update ");

        is_updated = false;
    }

    /**
     * Get the default value of the calibration. Will not be overridden by wrangler.
     * Do not use unless you're certain you don't wan't any updates the wrangler may
     * apply to this calibration.
     * 
     * @return Default value for the calibration
     */
    public double getDefault() {
        return default_val;
    }

    /**
     * Set a new value to override the present calibration. Value will be limited to
     * allowable min/max range for this calibration.
     * 
     * @param val_in Value to set.
     */
    public void setOverride(double val_in) {
        double temp = limitRange(val_in);
        cur_val = temp;
        overridden = true;
        is_updated = true;
        System.out.println("Info: Calibration " + this.name + " set to " + Double.toString(cur_val));
    }

    /**
     * Returns the calibration back to the default value.
     */
    public void reset() {
        overridden = false;
        cur_val = default_val;
    }

    
    String getValueTopic(){   return "/Calibrations/"+this.name+"/Value"; }
    String getDefaultTopic(){ return "/Calibrations/"+this.name+"/Default"; }
    String getUnitsTopic(){   return "/Calibrations/"+this.name+"/Units"; }
    String getMinTopic(){     return "/Calibrations/"+this.name+"/Min"; }
    String getMaxTopic(){     return "/Calibrations/"+this.name+"/Max"; }


}
