/////////////////////////////////////////////////////////////////////////
// Calibration - wrapper around NT4 to specifically extract cal information
// and allow clients to interact with one or more calibrations
//
// Mirroring (I assume) NT4 architecture, it's heavily callback driven
/////////////////////////////////////////////////////////////////////////

import { NT4_Client } from "./nt4.js";
import { CalObj } from "./calobj.js";

export class NT4_CalInf {

    ///////////////////////////////////////
    // Public API

    constructor(onNewCalAdded_in,           //Gets called when a new calibration is available
                onCalValueUpdated_in,       //Gets called when one calibration's value has changed.
                onConnect_in,               //Gets called once client completes initial handshake with server
                onDisconnect_in) {          //Gets called once client detects server has disconnected
        this.onNewCalAdded = onNewCalAdded_in;
        this.onCalValueUpdated = onCalValueUpdated_in;
        this.onConnect = onConnect_in;
        this.onDisconnect = onDisconnect_in;

        this.allCals = new Map();


        this.nt4Client = new NT4_Client(window.location.hostname, 
                                        this.topicAnnounceHandler.bind(this), 
                                        this.topicUnannounceHandler.bind(this),
                                        this.valueUpdateHandler.bind(this),
                                        this.onConnect.bind(this),
                                        this.onDisconnect.bind(this)
                                        );

        this.nt4Client.subscribePeriodic(["/Calibrations"], 0.5);

        //this.statusTextCallback("Starting connection...");
        this.nt4Client.ws_connect();
        //this.statusTextCallback("NT4 Connected.");

    }
    
    //Submit a new calibration value
    setCalibrationValue(name, value){
        var valTopic = this.calNameToTopic(name, "Value");
        this.nt4Client.addSample(valTopic, this.nt4Client.getServerTime_us(), value);
    }

    ///////////////////////////////////////////
    // Internal implementations

    topicAnnounceHandler(topic){

        if(this.isCalTopic(topic)){
            var calName = this.topicToCalName(topic);

            //we got something new related to calibrations...

            //ensure we've got an object for this cal
            if(!this.allCals.has(calName)){
                var newCal = new CalObj();
                newCal.name = calName;
                this.allCals.set(calName, newCal);
                this.onNewCalAdded(newCal);

            }
        }
    }

    topicUnannounceHandler(topic){
        if(this.isCalTopic(topicName, "Value")){
            var oldTopic = this.allCals.get(this.topicToCalName(topic));
            this.allCals.delete(this.topicToCalName(topic));
            //TODO call user hook
        }
    }

    
    valueUpdateHandler(topic, timestamp, value){
        if(this.isCalTopic(topic)){
            var calName = this.topicToCalName(topic);
            var updatedCal = this.allCals.get(calName);

            if(this.isCalTopic(topic, "Value")){
                updatedCal.value = value;
            } else if(this.isCalTopic(topic, "Name")){
                updatedCal.name = value;
            } else if(this.isCalTopic(topic, "Units")){
                updatedCal.units = value;
            } else if(this.isCalTopic(topic, "Min")){
                updatedCal.min = value;
            } else if(this.isCalTopic(topic, "Max")){
                updatedCal.max = value;
            } else if(this.isCalTopic(topic, "Default")){
                updatedCal.default = value;
            } 

            this.onCalValueUpdated(updatedCal); 
        } 
    }


    /////////////////////////////////////////////////
    // Helper Utiltiies

    calNameToTopic(name, suffix){
        return "/Calibrations/" + name + "/" + suffix;
    }

    isCalTopic(topic, suffix){
        if(suffix === undefined){
            suffix = ".*";
        }
        var replace = "\/Calibrations\/[a-zA-Z0-9 \._]+\/"+suffix;
        var re = new RegExp(replace,"g");
        return re.test(topic.name);
    }

    topicToCalName(topic){
        var replace = "\/Calibrations\/([a-zA-Z0-9 \._]+)\/";
        var re = new RegExp(replace,"g");
        var arr = re.exec(topic.name);
        return arr[1];
    }



}