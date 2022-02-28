package frc.lib.miniNT4;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import frc.lib.miniNT4.samples.TimestampedValue;
import frc.lib.miniNT4.topics.Topic;

public class Subscription{

    Set<String> topicPatterns;
    BaseClient clientRef;

    // Immediate mode - just force the network operation right away if true.
    boolean isImmediate = false;

    //Periodic - background value rate
    double periodicTxRate_sec = 0.1;

    //If true, store values in a stack between transmissions
    //If false, just remember most recent.
    boolean isLogging = false;

    HashMap<Topic, LinkedList<TimestampedValue>> sampleQueues = new HashMap<Topic, LinkedList<TimestampedValue>>();
    Thread txThread;
    volatile boolean backgroundThreadRunCmd = false;

    int subuid;


    public Subscription(Set<String> patterns_in, int subuid_in){
        topicPatterns = patterns_in;
        subuid = subuid_in;
        updateTopicSet();
    }

    public void updateTopicSet(){
        //Based on the current set of patterns, find topics that exist
        //For each topic, add a subscription reference.
        // Every time the value is updated, this subscription will be notified.
        for(Topic t :NT4Server.getInstance().getTopics(topicPatterns)){
            t.addSubscriptionRef(this);

            //Ensure the SampleQueue's has any new topics we found
            if(!sampleQueues.containsKey(t)){
                sampleQueues.put(t, new LinkedList<TimestampedValue>());
                //Send the new topic's current value
                // This implements the implicit "getValue" required on subscription
                this.onNewValue(t, t.getCurVal());
            }
        }

    }

    public void onNewValue(Topic topic, TimestampedValue newVal){
        if(isImmediate){
            clientRef.onValueUpdate(topic, newVal);
        } else {

            LinkedList<TimestampedValue> l = sampleQueues.get(topic);

            if(l != null){
                if(!isLogging){
                    //When not logging, just remember the most recent value.
                    l.clear();
                }
    
                //Put the new value into the queue for transmission
                l.addLast(newVal);
            }
        }
    }


    public void start(){
        // Kick off transmission in new thread, clocked to periodic execution.
        txThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(backgroundThreadRunCmd){
                        long start_ms = System.currentTimeMillis();
                        txPeriodic();
                        long sleepDur = Math.max(5l, (long)(periodicTxRate_sec * 1000.0) - (System.currentTimeMillis() - start_ms));
                        Thread.sleep(sleepDur);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        txThread.setName("NT4 Subscription " + clientRef.friendlyName + " UID " + Integer.toString(subuid));
        txThread.setDaemon(true);
        txThread.setPriority(10);
        backgroundThreadRunCmd = true;
        txThread.start();
    }

    public void stop(){
        backgroundThreadRunCmd = false;
        try {
            txThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void txPeriodic(){
        if(!isImmediate){
            for(Topic topic : sampleQueues.keySet()){
                LinkedList<TimestampedValue> l = sampleQueues.get(topic);
    
                //Empty all the values out of the queue and submit for transmission
                TimestampedValue newVal = l.pollFirst();
                while(newVal != null){
                    clientRef.onValueUpdate(topic, newVal);
                    newVal = l.pollFirst();
                }
            }
        }
    }

    public String toString(){
        return Integer.toString(this.subuid) + " : " + this.topicPatterns.toString();
    }

}