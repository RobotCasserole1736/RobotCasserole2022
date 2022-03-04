/////////////////////////////////////////////////////////////////////////
// SignalDAQ - wrapper around NT4 to specifically extract signal information
// and allow clients to request one or more signals
//
// Mirroring (I assume) NT4 architecture, it's heavily callback driven
/////////////////////////////////////////////////////////////////////////

import { NT4_Client } from "./nt4.js";

export class SignalDAQNT4 {


    constructor(onSignalAnnounce_in,   //Gets called when server announces enough topics to form a new signal
                onSignalUnAnnounce_in, //Gets called when server unannounces any part of a signal
                onNewSampleData_in,    //Gets called when any new data is available
                onConnect_in,          //Gets called once client completes initial handshake with server
                onDisconnect_in,        //Gets called once client detects server has disconnected
                statusTextCallback_in) {
        this.onSignalAnnounce = onSignalAnnounce_in;
        this.onSignalUnAnnounce = onSignalUnAnnounce_in;
        this.onNewSampleData = onNewSampleData_in;
        this.onConnect = onConnect_in;
        this.onDisconnect = onDisconnect_in;
        this.statusTextCallback = statusTextCallback_in;

        this.daqSignalList = new Set(); //start assuming no signals.

        this.daqRunning = false;

        this.rxCount = 0;

        this.nt4Client = new NT4_Client(window.location.hostname, 
                                        this.topicAnnounceHandler.bind(this), 
                                        this.topicUnannounceHandler.bind(this),
                                        this.valueUpdateHandler.bind(this),
                                        this.onConnect.bind(this),
                                        this.onDisconnect.bind(this)
                                        );

        this.statusTextCallback("Starting connection...");
        this.nt4Client.ws_connect();
        this.statusTextCallback("NT4 Connected.");
    }

    topicAnnounceHandler( newTopic ) {
        if(this.isSignalUnitsTopic(newTopic)){
            //If a signal units topic is announced, request what those units value actually is.
            this.nt4Client.getValues([newTopic.name]);
        }
    }

    topicUnannounceHandler( removedTopic ) {
        if(this.isSignalUnitsTopic(removedTopic)){
            this.onSignalUnAnnounce(this.unitsTopicToSigName(removedTopic));
        } 
    }

    valueUpdateHandler(topic, timestamp, value){
        if(this.isSignalUnitsTopic(topic)){
            // Got new value for the units of a signal
            var sigName = this.unitsTopicToSigName(topic);
            var sigUnits = value;
            this.onSignalAnnounce(sigName, sigUnits); //Announce signal when we know the value of its units
        } else {
            // Got a new sample
            var sigName = this.valueTopicToSigName(topic);
            this.onNewSampleData(sigName, timestamp, value);
            if(this.daqRunning){
                this.rxCount++;
            }
            this.updateStatusText();
        }
    }

    //Request a signal get added to the DAQ
    addSignal(signalNameIn){
        this.daqSignalList.add(signalNameIn);
    }

    //Call to remove a signal from the DAQ
    removeSignal(signalNameIn){
        this.daqSignalList.delete(signalNameIn);
    }

    clearSignalList(){
        this.daqSignalList.clear();
    }

    //Request RIO start sending periodic updates with data values
    startDAQ(){
        this.daqRunning = true;
        this.daqSignalList.forEach(sigName => {
            this.nt4Client.subscribeLogging([this.sigNameToValueTopic(sigName)]);
        });
        this.rxCount = 0;
        this.updateStatusText();
    }

    //Request RIO stop sending periodic updates
    stopDAQ(){
        this.nt4Client.clearAllSubscriptions();
        this.daqRunning = false;
        this.updateStatusText();
    }

    updateStatusText(){
        var text = "";
        if(this.daqRunning){
            text += "DAQ Running";
        } else {
            text += "DAQ Stopped";
        }
        text += " RX Count: " + this.rxCount.toString();
        this.statusTextCallback(text);
    }

    sigNameToValueTopic(name){
        return "/Signals/" + name + "/value"
    }

    valueTopicToSigName(topic){
        var tmp = topic.name;
        tmp = tmp.replace(/^\/Signals\//, '');
        tmp = tmp.replace(/\/value/, '');
        return tmp;
    }

    isSignalValueTopic(topic){
        return topic.match(/Signals\/[a-zA-Z0-9\._]+\/value/);
    }

    sigNameToUnitsTopic(name){
        return "/Signals/" + name + "/units"
    }

    unitsTopicToSigName(topic){
        var tmp = topic.name;
        tmp = tmp.replace(/^\/Signals\//, '');
        tmp = tmp.replace(/\/units/, '');
        return tmp;
    }

    isSignalUnitsTopic(topic){
        return topic.name.match(/^\/Signals\/[a-zA-Z0-9\._]+\/units/);
    }

    isSignalValueTopic(topic){
        return topic.name.match(/^\/Signals\/[a-zA-Z0-9\._]+\/value/);
    }


}