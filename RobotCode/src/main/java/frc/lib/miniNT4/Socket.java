package frc.lib.miniNT4;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageTypeException;

import edu.wpi.first.wpilibj.DriverStation;
import frc.lib.miniNT4.samples.TimestampedInteger;
import frc.lib.miniNT4.samples.TimestampedValue;
import frc.lib.miniNT4.samples.TimestampedValueFactory;
import frc.lib.miniNT4.topics.Topic;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

public class Socket extends WebSocketAdapter {

    RemoteClient clientInf;

    @Override
    public void onWebSocketText(String message) {
        JSONParser parser = new JSONParser();
        try {
            handleIncoming((JSONObject) parser.parse(message));
        } catch (Exception e) {
            DriverStation.reportWarning("Could not parse WS json message " + message + "\n" + e.getMessage(),
                    e.getStackTrace());
        }
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        var unpacker = MessagePack.newDefaultUnpacker(payload, offset, len);
        int topicID = -9999;
        try {
            topicID = unpacker.unpackInt();
        } catch (IOException e) {
            DriverStation.reportWarning("Could not parse topic from WS binary message:\n" + e.getMessage(),
                    e.getStackTrace());
            return;
        }

        if(topicID >= 0){
            //Normal data topic update
            Topic topicToUpdate = NT4Server.getInstance().getTopic(topicID);

            if(topicToUpdate == null){
                DriverStation.reportWarning("Invalid Topic ID " + topicID, true);
                return;
            }

            TimestampedValue newVal;
            try {
                newVal = TimestampedValueFactory.fromMsgPack(unpacker);
            } catch (IOException e) {
                DriverStation.reportWarning("Could not unpack timestamps and values from WS binary message:\n" + e.getMessage(),
                        e.getStackTrace());
                return;
            } catch (MessageTypeException e){
                DriverStation.reportWarning("Unexpected message datatypes in WS binary message:\n" + e.getMessage(),
                        e.getStackTrace());
                return;
            }

            topicToUpdate.submitNewValue(newVal);

        } else if (topicID == -1){
            try {
                long timestamp_us = unpacker.unpackLong(); //ignored, should be zero, but still needs to be unpacked.
                int typeIdx = unpacker.unpackInt(); //Ignored, but still needs to be unpacked.
                long userTimestamp = unpacker.unpackLong();

                // Immedeately reply with the current timestamp
                this.sendValueUpdate(NT4Server.getInstance().getTopic(-1), new TimestampedInteger(userTimestamp, NT4Server.getInstance().getCurServerTime()));

                //System.out.println("Syncing client to time " + NT4Server.getInstance().getCurServerTime()/1000000.0);
            } catch (IOException e) {
                DriverStation.reportWarning("Could not handle time synchronization message: \n" + e.getMessage(),
                        e.getStackTrace());
            }
            

        } else {
            DriverStation.reportWarning("Invalid Topic ID " + topicID, true);
            return;
        }


    }

    @Override
    public void onWebSocketConnect(Session sess) {
        super.onWebSocketConnect(sess);

        String clientName = sess.getUpgradeRequest().getRequestURI().toString();
        clientInf = new RemoteClient(this, clientName);
        // Announce all existing topics to the client
        for(Topic t : NT4Server.getInstance().getAllTopics()){
            this.sendAnnounce(t);
        }
        System.out.println("Connected: " + clientInf.friendlyName);
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);
        clientInf.onDisconnect();
        System.out.println("Disconnected: " + clientInf.friendlyName);
    }

    void sendWebSocketString(String str){
        try {
            RemoteEndpoint tmp = getRemote();
            //System.out.println("MSG: Server to " + clientInf.friendlyName + " : \n" + str);
            if(tmp != null){
                tmp.sendString(str);
            } else {
                DriverStation.reportWarning("Could not send message to " + clientInf.friendlyName + " - client was null.", false);
            }
        } catch (Exception e) {
            DriverStation.reportWarning("Could not send message to " + clientInf.friendlyName + "\n" + e.getMessage(), e.getStackTrace());
        }
    }

    @SuppressWarnings("unchecked") 
    public synchronized void sendAnnounce(Topic topic){

        JSONObject properties = new JSONObject();
        properties.put("persistent", topic.isPersistent);

        JSONObject params = new JSONObject();
        params.put("name", topic.name);
        params.put("id", topic.id);
        params.put("type", topic.getTypestring());
        params.put("properties", properties);

        JSONObject obj = new JSONObject();
        obj.put("method", "announce");
        obj.put("params", params);

        sendWebSocketString(obj.toJSONString());

    }

    @SuppressWarnings("unchecked") 
    public synchronized void sendUnannounce(Topic topic){

        JSONObject params = new JSONObject();
        params.put("name", topic.name);
        params.put("id", topic.id);

        JSONObject obj = new JSONObject();
        obj.put("method", "unannounce");
        obj.put("params", params);

        sendWebSocketString(obj.toJSONString());

    }

    public synchronized void sendValueUpdate(Topic topic, TimestampedValue val){
        //System.out.println("MSG: Server to " + clientInf.friendlyName + " :");
        //System.out.println("Value Update: " + topic.name + " = " + val.toNiceString());

        ByteBuffer buff;

        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        try {
            packer.packInt(topic.id);
            packer.packLong(val.timestamp_us);
            packer.packInt(topic.getTypeInt());
            val.packValue(packer);
            packer.close();
            buff = ByteBuffer.wrap(packer.toByteArray());
        } catch (IOException e) {
            DriverStation.reportWarning("Could not construct MessagePack for value update to " + clientInf.friendlyName + "\n" + e.getMessage(), e.getStackTrace());
            return;
        }

        try {
            RemoteEndpoint curRemote = getRemote();
            if(curRemote != null){
                curRemote.sendBytes(buff);
            }
        } catch (Exception e) {
            DriverStation.reportWarning("Could not transmit value update to " + clientInf.friendlyName + "\n" + e.getMessage(), e.getStackTrace());
        }


    }

    private HashSet<String> parseStringSet(JSONArray arr){
        HashSet<String> retSet = new HashSet<String>(arr.size());
        for(int i = 0; i < arr.size(); i++){
            retSet.add((String)arr.get(i));
        }
        return retSet;
    }

    void handleIncoming(JSONObject data) throws ParseException{
        //System.out.println("MSG: " + clientInf.friendlyName + " to Server : \n" + data.toJSONString());

        String method = (String) data.get("method");
        JSONObject params = (JSONObject) data.get("params");

        String name;
        String type;
        HashSet<String> prefixes = new HashSet<String>();
        int subuid;
        JSONObject options;


        switch(method){
            case "publish":
                name = (String) params.get("name");
                type = (String) params.get("type");
                NT4Server.getInstance().publishTopic(name, type, clientInf);
            break;
            case "unpublish":
                name = (String) params.get("name");
                NT4Server.getInstance().unPublishTopic(name, clientInf);
            break;
            case "setproperties":
                //TODO
            break;
            case "getvalues":
                prefixes = parseStringSet((JSONArray) params.get("prefixes"));
                clientInf.getValues(prefixes);
            break;
            case "subscribe":
                prefixes = parseStringSet((JSONArray) params.get("prefixes"));
                subuid = ((Number)params.get("subuid")).intValue();
                Subscription newSub = clientInf.subscribe(prefixes, subuid);
                options = (JSONObject) params.get("options");
                
                if(options.containsKey("immediate")){
                    newSub.isImmediate = (boolean) options.get("immediate");
                }

                if(options.containsKey("periodic")){
                    newSub.periodicTxRate_sec = ((Number)options.get("periodic")).doubleValue();
                }

                if(options.containsKey("logging")){
                    newSub.isLogging = (boolean) options.get("logging");
                }

                newSub.start();

            break;
            case "unsubscribe":
                subuid = ((Number)params.get("subuid")).intValue();
                clientInf.unSubscribe(subuid);

            break;
            default:
                throw new IllegalArgumentException("Unrecognized method " + method);
        }
    }

}