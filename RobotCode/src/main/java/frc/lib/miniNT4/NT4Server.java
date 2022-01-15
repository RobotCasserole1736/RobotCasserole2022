package frc.lib.miniNT4;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import edu.wpi.first.wpilibj.Timer;
import frc.lib.miniNT4.topics.BooleanTopic;
import frc.lib.miniNT4.topics.DoubleTopic;
import frc.lib.miniNT4.topics.IntegerTopic;
import frc.lib.miniNT4.topics.StringTopic;
import frc.lib.miniNT4.topics.TimeTopic;
import frc.lib.miniNT4.topics.Topic;

public class NT4Server {

    /* Singleton infrastructure */
    private static NT4Server instance;
    public static synchronized NT4Server getInstance() {
        if (instance == null) {
            instance = new NT4Server();
            instance.startServer();
        }
        return instance;
    }

    private Server server;

    //Synchronization required since we add/remove clients and iterate over the list simulaneously
    private Set<BaseClient> clients = Collections.synchronizedSet(new HashSet<BaseClient>());

    private NT4Server() {

    }

    //Lists of all available topics for rapid access
    Hashtable<String, Topic> topicsByName = new Hashtable<String, Topic>();
    Hashtable<Integer, Topic> topicsByID = new Hashtable<Integer, Topic>();


    /**
     * Starts the web server in a new thread. Should be called at the end of robot
     * initialization.
     */
    public void startServer() {

        server = new Server(5810);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        ServletHolder dataServerHolder = new ServletHolder("nt", new Servlet());
        context.addServlet(dataServerHolder, "/nt/*");

        //Automatically create and include the time topic
        TimeTopic tt = new TimeTopic();
        topicsByName.put(tt.name, tt);
        topicsByID.put(tt.id, tt);

        // Kick off server in brand new thread.
        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    server.start();
                    server.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        serverThread.setName("NT4 Main Server");
        serverThread.setPriority(10);
        serverThread.start();
    }

    /**
     * Called internally when a new client has connected
     * @param newClient
     */
    void registerClient(BaseClient newClient){
        synchronized(clients){
            clients.add(newClient);
        }
    }
    /**
     * Called internally when a client is no longer connected
     * @param newClient
     */
    void unRegisterClient(BaseClient deadClient){
        synchronized(clients){
            clients.remove(deadClient);
        }
    }

    public synchronized void unPublishTopic(String deadTopicName, BaseClient client){
        this.unPublishTopic(this.topicsByName.get(deadTopicName), client);
    }

    public synchronized void unPublishTopic(Topic deadTopic, BaseClient client){

        deadTopic.removePublisherRef(client);

        if(!deadTopic.hasPublishers()){
            //No more publishers - topic should be unannounced
            synchronized(clients){
                for(BaseClient c : clients){
                    c.onUnannounce(deadTopic);
                    for(Subscription s : c.subscriptions.values()){
                        s.updateTopicSet();
                    }
                }
            }
    
            topicsByName.remove(deadTopic.name);
            topicsByID.remove(deadTopic.id);
        }

    }

    public synchronized Topic publishTopic(String name, String type, BaseClient client){

        Topic retTopic;

        if(topicsByName.containsKey(name)){
            //Topic already published by someone else - just add this client to the list of publishers
            retTopic = topicsByName.get(name);
        } else {
            //New Topic. Do the factory thing.

            switch(type){
                case NT4TypeStr.FLOAT_64:
                case NT4TypeStr.FLOAT_32:
                    retTopic = new DoubleTopic(name, 0);
                    break;
                case NT4TypeStr.INT:
                    retTopic =  new IntegerTopic(name, 0);
                    break;
                case NT4TypeStr.BOOL:
                    retTopic =  new BooleanTopic(name, false);
                    break;
                case  NT4TypeStr.STR:
                case  NT4TypeStr.JSON:
                    retTopic =  new StringTopic(name, "");
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported topic type " + type);
            }
    
            topicsByName.put(retTopic.name, retTopic);
            topicsByID.put(retTopic.id, retTopic);
    
            synchronized(clients){
                for(BaseClient c : clients){
                    c.onAnnounce(retTopic);
                    for(Subscription s : c.subscriptions.values()){
                        s.updateTopicSet();
                    }
                }
            }
        }

        retTopic.addPublisherRef(client);
        return retTopic;
    }

    /**
     * 
     * @return list of all available topics on the server
     */
    public Set<Topic> getAllTopics(){
        return new HashSet<Topic>(topicsByName.values());
    }

    /**
     * 
     * @return topic with the given ID, or none if nothing found.
     */
    public Topic getTopic(int id){
        return topicsByID.get(id);
    }

    /**
     * 
     * @param pattern String Regex pattern to match topic names
     * @return list of names matching the given regex pattern.
     */
    public Set<Topic> getTopics(String pattern){
        Set<String> p = new HashSet<String>(1);
        p.add(pattern);
        return this.getTopics(p);
    }

    /**
     * 
     * @param prefixes Set of Strings of key prefixes
     * @return list of names matching the given regex pattern.
     */
    public Set<Topic> getTopics(Set<String> prefixes){
        HashSet<Topic> retTopics = new HashSet<Topic>();

        for(Topic topic : topicsByName.values()){
            //For all topics...
            for(String prefix : prefixes){
                if(topic.name.startsWith(prefix)){ //TODo - make prefixes into globs?
                    //On the first match, add it to the ret list, and move on to the next topic (skipping remaining topics).
                    retTopics.add(topic);
                    break;
                }
            }
        }

        return retTopics;
    }

    int topicUIDCounter = 0;
    public int getUniqueTopicID(){
        return topicUIDCounter++;
    }

    public void printCurrentClients(){
        System.out.println("========================");
        System.out.println("== Current Clients:");
        for(BaseClient client : clients){
            System.out.println(client.friendlyName);
        }
        System.out.println("========================\n\n");
    }

    public void printCurrentSubscriptions(){
        System.out.println("========================");
        System.out.println("== Current Subscriptions:");
        for(BaseClient client : clients){
            System.out.println("> " + client.friendlyName);
            for(Subscription sub : client.subscriptions.values()){
                System.out.println(">>  " + sub.toString());
            }
        }
        System.out.println("========================\n\n");
    }

    /**
     * 
     * @return current server microseconds time
     */
    public long getCurServerTime(){
        return Math.round(Timer.getFPGATimestamp() * 1000000l);
    }

}
