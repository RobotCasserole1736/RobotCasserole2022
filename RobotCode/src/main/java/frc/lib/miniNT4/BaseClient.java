package frc.lib.miniNT4;

import java.util.HashMap;
import java.util.Set;

import frc.lib.miniNT4.samples.TimestampedValue;
import frc.lib.miniNT4.topics.Topic;

public abstract class BaseClient {

    public String friendlyName = "";

    HashMap<Integer, Subscription> subscriptions = new HashMap<Integer, Subscription>();

    public BaseClient(){
    
    }

    public void postInit(){
        NT4Server.getInstance().registerClient(this);
    }

    public Subscription subscribe(Set<String> patterns){
        return this.subscribe(patterns, this.subscriptions.size());
    }

    /**
     * Creates a new subscription off of provided patterns and registers it with the server
     * @param patterns
     * @return the newly created and server-registered subscription object
     */
    public Subscription subscribe(Set<String> patterns, int subuid){
        Subscription newSub = new Subscription(patterns, subuid);
        newSub.clientRef = this;
        synchronized(subscriptions){
            subscriptions.put(subuid, newSub);
        }
        return newSub;
    }

    public void unSubscribe(int deadSubId){
        Subscription oldSub;
        synchronized(subscriptions){
            oldSub = subscriptions.remove(deadSubId);
        }

        for(Topic t: NT4Server.getInstance().getAllTopics()){
            t.removeSubscriptionRef(oldSub);
        }

        if(oldSub != null){
            oldSub.stop();
        }
    }

    public void getValues(Set<String> patterns){
        for(Topic t: NT4Server.getInstance().getTopics(patterns)){
            onValueUpdate(t, t.getCurVal());
        }
    }

    public abstract void onAnnounce(Topic newTopic);
    public abstract void onUnannounce(Topic deadTopic);
    public abstract void onValueUpdate(Topic topic, TimestampedValue newVal);
}
