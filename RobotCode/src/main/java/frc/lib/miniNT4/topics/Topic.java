package frc.lib.miniNT4.topics;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import frc.lib.miniNT4.BaseClient;
import frc.lib.miniNT4.Subscription;
import frc.lib.miniNT4.samples.TimestampedValue;

abstract public class Topic{
    public int id;
    public String name;    
    public boolean isPersistent = false;

    public TimestampedValue curValue;

    //Synchronization required because we modify the refs and iterate over the set from different threads.
    Set<Subscription> subscriptionRefs = Collections.synchronizedSet(new HashSet<Subscription>());
    Set<BaseClient> publisherRefs = Collections.synchronizedSet(new HashSet<BaseClient>());


    public Topic(String name_in, TimestampedValue default_in){
        default_in.timestamp_us = 0; //ensure we are storing default as the default
        curValue = default_in;
        name = name_in;
    }

    /**
     * Submit a new value to the server
     * @param newSample new value for this topic
     */
    public void submitNewValue(TimestampedValue newSample){
        curValue = newSample;
        
        synchronized(subscriptionRefs){
            for(Subscription sub : subscriptionRefs){
                sub.onNewValue(this, newSample);
            }
        }
    }

    public long getLastChange(){
        return curValue.timestamp_us;
    }

    public void addSubscriptionRef(Subscription sub){
        subscriptionRefs.add(sub);
    }

    public void removeSubscriptionRef(Subscription sub){
        subscriptionRefs.remove(sub);
    }

    public void addPublisherRef(BaseClient pub){
        publisherRefs.add(pub);
    }

    public void removePublisherRef(BaseClient pub){
        publisherRefs.remove(pub);
    }

    public boolean hasPublishers(){
        return (publisherRefs.size() > 0) && this.isPersistent == false;
    }

    public boolean isPublishedBy(BaseClient client){
        return (publisherRefs.contains(client));
    }

    public abstract String getTypestring();
    public abstract int getTypeInt();
    public TimestampedValue getCurVal() {
        return this.curValue;
    }

}