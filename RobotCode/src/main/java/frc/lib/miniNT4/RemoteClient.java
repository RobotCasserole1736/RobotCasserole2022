package frc.lib.miniNT4;

import frc.lib.miniNT4.samples.TimestampedValue;
import frc.lib.miniNT4.topics.Topic;

import java.util.HashSet;
import java.util.Set;

public class RemoteClient extends BaseClient{

    private Socket parentSocket;

    public RemoteClient(Socket ps_in, String name){
        super();
        parentSocket = ps_in;
        friendlyName = name;
        this.postInit();
    }

    void onDisconnect(){

        NT4Server.getInstance().unRegisterClient(this);

        //Implicit unsubscribe from all topics
        synchronized(subscriptions){
            Set<Integer> subIDs = new HashSet<Integer>(this.subscriptions.keySet());
            for(int id : subIDs){
                unSubscribe(id);
            }
        }

        //Implicit unpublish of all published signals
        for(Topic t : NT4Server.getInstance().getAllTopics()){
            if(t.isPublishedBy(this)){
                NT4Server.getInstance().unPublishTopic(t, this);
            }
        }

    }

    @Override
    public void onAnnounce(Topic newTopic) {
        parentSocket.sendAnnounce(newTopic);
    }

    @Override
    public void onUnannounce(Topic deadTopic) {
        parentSocket.sendUnannounce(deadTopic);
    }

    public void setTopicProperties(String name, boolean isPersistant){
        Set<Topic> matchedTopics = NT4Server.getInstance().getTopics(name);
        for(Topic t : matchedTopics){
            t.isPersistent = isPersistant;
        }
    }

    @Override
    public void onValueUpdate(Topic topic, TimestampedValue newVal) {
        parentSocket.sendValueUpdate(topic, newVal);
    }
    
}
