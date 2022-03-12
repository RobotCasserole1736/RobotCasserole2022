/////////////////////////////////////////////////////////////////////////
// SignalDAQLocalFile - reads and announces signal data through the same
// interface as the NT4 signals
//
// Mirroring (I assume) NT4 architecture, it's heavily callback driven
/////////////////////////////////////////////////////////////////////////

export class SignalDAQLocalFile {


    constructor(onSignalAnnounce_in,   //Gets called when a new signal is found in the file
                onSignalUnAnnounce_in, //Gets called when a signal is no longer avaialble.
                onNewSampleData_in,    //Gets called when any new piece of data is read from file
                onConnect_in,          //Gets called once a file is loaded into RAM
                onDisconnect_in,       //Gets called once a file is unloaded.
                statusTextCallback_in) {
        this.onSignalAnnounce = onSignalAnnounce_in;
        this.onSignalUnAnnounce = onSignalUnAnnounce_in;
        this.onNewSampleData = onNewSampleData_in;
        this.onConnect = onConnect_in;
        this.onDisconnect = onDisconnect_in;
        this.statusTextCallback = statusTextCallback_in;

        this.stripTrailingCommaMode = false;

        this.signalNameList = []; //start assuming no signals.

        this.statusTextCallback("No File Loaded");

        this.lineCount = 0;

    }

    load(fileobj){
        var reader = new FileReader();
        reader.readAsText(fileobj);
        reader.onload = this.localFileLoadHandler.bind(this);
    }

    localFileLoadHandler(evt){
        var all_lines = evt.target.result
        this.parseFileContents(all_lines);
    }

    parseFileContents(all_lines){
        var lines = (all_lines + '').split('\n');
        this.lineCount = 0;

        if(lines.length > 3){
            this.parseHeaders(lines[0], lines[1]);
            for(var lineIdx = 2; lineIdx < lines.length; lineIdx++){
                this.parseData(lines[lineIdx]);
            }
            this.statusTextCallback("Parsed " + this.lineCount.toString() + " lines.")
        } else {
            this.statusTextCallback("File Parse Error!");
            throw("Could not parse file! Not enough lines of content.");
        }
    }

    parseHeaders(nameRow, unitsRow){

        //Check whether we do indeed need to strip off a trailing comma for this file
        this.stripTrailingCommaMode = nameRow.endsWith(",");

        if(this.stripTrailingCommaMode){
            //Strip trailing commas
            nameRow = nameRow.replace(/,$/, "");
            unitsRow = unitsRow.replace(/,$/, "");
        }

        this.signalNameList = []

        //Parse rows as CSV.
        var nameList = nameRow.split(',');
        var unitsList = unitsRow.split(',');

        if(nameList.length == unitsList.length){
            for(var sigIdx = 1; sigIdx < nameList.length; sigIdx++){
                this.signalNameList.push(nameList[sigIdx]);
                this.onSignalAnnounce(nameList[sigIdx], unitsList[sigIdx]); 
            }
        } else {
            this.statusTextCallback("File Header Parse Error!");
            throw("Could not parse file! Number of signal names and units is not the same.");
        }
    }

    parseData(row){

        // Skip empty rows
        if(row.length == 0){
            return;
        }

        if(this.stripTrailingCommaMode){
            //Strip trailing commas
            row = row.replace(/,$/, "");
        }

        //Parse row as CSV
        var dataValuesList = row.split(',');

        if(dataValuesList.length == (this.signalNameList.length + 1)){
            var timestamp = parseFloat(dataValuesList[0]) * 1000000; //timestamp should be in microseconds
            for(var sigIdx = 0; sigIdx < this.signalNameList.length; sigIdx++){
                var dataValStr = dataValuesList[sigIdx + 1].trim();
                if(dataValStr.length > 0){
                    //Some data elements may be empty
                    var val = parseFloat(dataValStr); //TODO - support things other than doubles?
                    this.onNewSampleData(this.signalNameList[sigIdx], timestamp, val);
                }
            }
            this.lineCount++;
        } else {
            console.log("Warning: Skipping line. Number of data element in row does not match headers!");
        }
    }

    //Do-nothing functions
    startDAQ(){}
    stopDAQ(){}

    sigNameToValueTopic(name){
        return "Signals/" + name + "/Value"
    }

}