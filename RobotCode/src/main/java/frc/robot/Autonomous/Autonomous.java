package frc.robot.Autonomous;

import java.util.Set;

import edu.wpi.first.math.geometry.Pose2d;
import frc.lib.AutoSequencer.AutoSequencer;
import frc.lib.Autonomous.AutoMode;
import frc.lib.Autonomous.AutoModeList;
import frc.lib.Util.CrashTracker;
import frc.lib.miniNT4.LocalClient;
import frc.lib.miniNT4.NT4Server;
import frc.lib.miniNT4.NT4TypeStr;
import frc.lib.miniNT4.samples.TimestampedInteger;
import frc.lib.miniNT4.samples.TimestampedValue;
import frc.lib.miniNT4.topics.Topic;
import frc.robot.Autonomous.Modes.BallPickup;
import frc.robot.Autonomous.Modes.DoNothing;
import frc.robot.Autonomous.Modes.DriveFwd;
import frc.robot.Autonomous.Modes.Wait;
import frc.robot.Autonomous.Modes.many_Pickup;
import frc.robot.Drivetrain.DrivetrainControl;


/*
 *******************************************************************************************
 * Copyright (C) 2022 FRC Team 1736 Robot Casserole - www.robotcasserole.org
 *******************************************************************************************
 *
 * This software is released under the MIT Licence - see the license.txt
 *  file in the root of this repo.
 *
 * Non-legally-binding statement from Team 1736:
 *  Thank you for taking the time to read through our software! We hope you
 *    find it educational and informative! 
 *  Please feel free to snag our software for your own use in whatever project
 *    you have going on right now! We'd love to be able to help out! Shoot us 
 *    any questions you may have, all our contact info should be on our website
 *    (listed above).
 *  If you happen to end up using our software to make money, that is wonderful!
 *    Robot Casserole is always looking for more sponsors, so we'd be very appreciative
 *    if you would consider donating to our club to help further STEM education.
 */


public class Autonomous extends LocalClient  {

    Topic curDelayModeTopic = null;
    Topic curMainModeTopic = null;

    long curDelayMode_dashboard = 0;
    long curMainMode_dashboard = 0;

    public AutoModeList mainModeList = new AutoModeList("main");
    public AutoModeList delayModeList = new AutoModeList("delay");

    AutoMode curDelayMode = null;
    AutoMode curMainMode = null;

    AutoMode prevDelayMode = null;
    AutoMode prevMainMode = null;

    
    /* Singleton infratructure*/
    private static Autonomous inst = null;
    public static synchronized Autonomous getInstance() {
        if (inst == null)
            inst = new Autonomous();
        return inst;
    }

    AutoSequencer seq;


    private Autonomous(){
        seq = new AutoSequencer("Autonomous");

        delayModeList.add(new Wait(0.0));
        delayModeList.add(new Wait(3.0));
        delayModeList.add(new Wait(6.0));
        delayModeList.add(new Wait(9.0));

        mainModeList.add(new BallPickup());
        mainModeList.add(new DriveFwd());
        mainModeList.add(new DoNothing());
        mainModeList.add(new many_Pickup());
        

        // Create and subscribe to NT4 topics
        curDelayModeTopic = NT4Server.getInstance().publishTopic(delayModeList.getCurModeTopicName(), NT4TypeStr.INT, this);
        curMainModeTopic = NT4Server.getInstance().publishTopic(mainModeList.getCurModeTopicName(), NT4TypeStr.INT, this);
        curDelayModeTopic.submitNewValue(new TimestampedInteger(0, 0));
        curMainModeTopic.submitNewValue(new TimestampedInteger(0, 0));

        this.subscribe(Set.of(delayModeList.getDesModeTopicName(), mainModeList.getDesModeTopicName()), 0).start();

        curDelayMode = delayModeList.getDefault();
        curMainMode  = mainModeList.getDefault();

    }

    /* This should be called periodically in Disabled, and once in auto init */
    public void sampleDashboardSelector(){
        curDelayMode = delayModeList.get((int)curDelayMode_dashboard);
        curMainMode = mainModeList.get((int)curMainMode_dashboard);	
        if(curDelayMode != prevDelayMode || curMainMode != prevMainMode){
            loadSequencer();
            prevDelayMode = curDelayMode;
            prevMainMode = curMainMode;
        }
    }


    public void startSequencer(){
        sampleDashboardSelector(); //ensure it gets called once more
        DrivetrainControl.getInstance().setKnownPose(curMainMode.getInitialPose());
        if(curMainMode != null){
            seq.start();
        }
    }

    public void loadSequencer(){
        
        CrashTracker.logGenericMessage("Initing new auto routine " + curDelayMode.humanReadableName + "s delay, " + curMainMode.humanReadableName);

        seq.stop();
        seq.clearAllEvents();

        curDelayMode.addStepsToSequencer(seq);
        curMainMode.addStepsToSequencer(seq);
    
        DrivetrainControl.getInstance().setKnownPose(getStartPose());

        curDelayModeTopic.submitNewValue(new TimestampedInteger(curDelayMode.idx));
        curMainModeTopic.submitNewValue(new TimestampedInteger(curMainMode.idx));
        
    }


    /* This should be called periodically, always */
    public void update(){
        seq.update();
    }

    /* Should be called when returning to disabled to stop and reset everything */
    public void reset(){
        seq.stop();
        loadSequencer();
    }

    public boolean isActive(){
        return (seq.isRunning() && curMainMode != null);
    }

    public Pose2d getStartPose(){
        return curMainMode.getInitialPose();
    }

    @Override
    public void onAnnounce(Topic newTopic) {}
    @Override
    public void onUnannounce(Topic deadTopic) {}

    @Override
    public void onValueUpdate(Topic topic, TimestampedValue newVal) {
        if(topic.name.equals(delayModeList.getDesModeTopicName())){
            curDelayMode_dashboard = (Long) newVal.getVal();
        } else if(topic.name.equals(mainModeList.getDesModeTopicName())){
            curMainMode_dashboard =(Long) newVal.getVal();
        }         
    }
}