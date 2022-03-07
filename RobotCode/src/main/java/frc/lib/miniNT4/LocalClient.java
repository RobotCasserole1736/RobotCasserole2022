package frc.lib.miniNT4;

/**
 * LocalClients represent roboRIO code interfaces which act like remote clients
 * with all the same abilities to subscribe/unsubscribe, publish/unpublish, recieve announcements and value updates.
 * 
 * All the cases where a piece of data is provided for the client to handle are implemented as abstract functions (which user code must fill in)
 */
public abstract class LocalClient extends BaseClient{

    public LocalClient(){
        super();
        this.friendlyName = this.getClass().getName();
        this.postInit();
    }

}
