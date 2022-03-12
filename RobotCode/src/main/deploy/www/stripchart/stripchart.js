/////////////////////////////////////////////////////////////////////////
// Stripchart - top-level entrypoint for the stripcharts view
/////////////////////////////////////////////////////////////////////////

import { Plot } from './plot.js'
import { SignalSelector } from './signalSelector.js';
import { Signal } from './signal.js';
import { Sample } from './sample.js';
import { SignalDAQNT4 } from '../interfaces/signalDAQ_NT4.js';
import { SignalDAQLocalFile } from '../interfaces/signalDAQ_localFile.js';

var plotsContainer = document.getElementById("plotsContainer");
var plotList = [];
var plotUniqueIdIdx = 0; //Used to ensure every newly added plot has a unique id

var LOCAL_STORAGE_KEY_NAME = "CasseroleStripchartConfig";

var local_storage_available = false;
if (typeof(Storage) !== "undefined") {
    local_storage_available = true;
} 

var allSignalsMap = new Map();
var signalSelector = new SignalSelector(document.getElementById("selectableSignalContainer"))

var mainDAQ = null;
var recordingStartTime = null;
var recordingEndTime = null;
var recordingRunning = false;

var vars = getUrlVars();
if("fname" in vars){
    var fname = vars["fname"]
    //User specified the file argument in the URL, so load it in.
    goFiles();
    var url = "http://" + window.location.hostname + ":" + window.location.port + "/" + fname;

    setFileStatusText("Downloading file...");
    var request = new XMLHttpRequest();
    request.open('GET', url, true);
    request.send(null);
    request.onreadystatechange = function () {
        if (request.readyState === 4 && request.status === 200) {
            var type = request.getResponseHeader('Content-Type');
            if (type.indexOf("text") !== 1) {
                setFileStatusText("Parsing File...");
                mainDAQ.parseFileContents(request.responseText);
                handleZoomFullBtnClick();
                setFileStatusText(fname);
            } else {
                setFileStatusText("Error on download!");
            }
        }
    }

} else {
    //Default to showing live data
    goLive();
}


//Add our first plot
addPlot();

//Start main animation loop
mainAnimationLoop();

//Attach key handler
document.addEventListener('keydown', keypressHandler);

/////////////////////////////////////////////////////////////
//Utility Functions


function addPlot(){

    var newPlotContainer = document.createElement('plot');
    newPlotContainer.id = "plot" + plotUniqueIdIdx.toString();
    plotUniqueIdIdx++;
    newPlotContainer.classList.add("outlined");

    plotsContainer.appendChild(newPlotContainer);

    var plotToAdd = new Plot(newPlotContainer, signalFromName, updateAllPlotValueAxisCount);
    plotToAdd.chart.mouseoverAtTimeCallback = onChartMouseOver; //Install our mouseover handler for cursor purposes
    plotToAdd.chart.zoomRangeUpdateCallback = onChartZoomAction; //Install our zoom handler for synced zoom purposes
    plotList.push(plotToAdd); //Assume add to end

}

function removePlot(){
    if(plotList.length > 1){
        var remIdx = plotList.length-1;  // assume last plot removed.

        plotsContainer.removeChild(plotList[remIdx].drawDiv);
        delete plotList[remIdx];
        plotList.splice(remIdx, 1);
    }    
}

function startRecording(){
    signalSelector.disableUserInteraction();
    saveCurrentConfig();
    mainDAQ.clearSignalList();
    signalSelector.getSelectedSignalList().forEach(sig => mainDAQ.addSignal(sig.name));
    allSignalsMap.forEach(sig => sig.clearValues());
    recordingStartTime = null;
    recordingEndTime = null;
    recordingRunning = true;
    mainDAQ.startDAQ();
}

function stopRecording(){
    if(mainDAQ != null){
        mainDAQ.stopDAQ();
    }
    recordingRunning = false;
    signalSelector.enableUserInteraction();
    plotList.forEach(plot=>plot.setDrawRange(recordingStartTime, recordingEndTime));

}


/////////////////////////////////////////////////////////////
//User Button Handlers

window.handleStartBtnClick = handleStartBtnClick;
function handleStartBtnClick(){
    startRecording();
}

window.handleStopBtnClick = handleStopBtnClick;
function handleStopBtnClick(){
    stopRecording();
}

window.handleAddChartBtnClick = handleAddChartBtnClick;
function handleAddChartBtnClick(){
    addPlot();
}

window.handleRmChartBtnClick = handleRmChartBtnClick;
function handleRmChartBtnClick(){
    removePlot();
}

window.handleZoomFullBtnClick = handleZoomFullBtnClick;
function handleZoomFullBtnClick(){
    plotList.forEach(plot=>plot.setDrawRange(recordingStartTime, recordingEndTime));
}

window.unselectAllBtnClick = unselectAllBtnClick;
function unselectAllBtnClick(){
    signalSelector.clearSelection();
    saveCurrentConfig();
}

window.filterChangeHandler = filterChangeHandler;
function filterChangeHandler(filterSpec_in){
    signalSelector.setFilterSpec(filterSpec_in);
}

window.handleModeChange = handleModeChange;
function handleModeChange(){
    var checkbox = document.getElementById("modeCheckbox");
    if(checkbox.checked){
        goFiles();
    } else {
        goLive();
    }
}

window.handleFileSelect = handleFileSelect;
function handleFileSelect(files_in){
    var fileobj = files_in[0];
    goFiles();
    mainDAQ.load(fileobj);
    setFileStatusText(fileobj.name);
    
}


var isVisible = false;
window.handleSignalShowHide = handleSignalShowHide;
function handleSignalShowHide(){
    isVisible = !isVisible
    document.getElementById("signalPane").style.width = isVisible ?  "33%" : "0%";    
}

/////////////////////////////////////////////////////////////
// DAQ Source Change Handlers

function goLive(){
    document.getElementById("modeCheckbox").checked = false;
    document.getElementById("start_btn").classList.remove("hidden");
    document.getElementById("stop_btn").classList.remove("hidden");
    document.getElementById("fileSelector").classList.add("hidden");
    stopRecording();
    allSignalsMap.clear();
    signalSelector.clearSignalList();

    setFileStatusText("");
    setDAQStatusText("");
    mainDAQ = new SignalDAQNT4(onSignalAnnounce,onSignalUnAnnounce,onNewSampleData,onConnect,onDisconnect, setDAQStatusText);

    recordingStartTime = null;
    recordingEndTime = null;
    recordingRunning = false;
}

function goFiles(){
    document.getElementById("modeCheckbox").checked = true;
    document.getElementById("start_btn").classList.add("hidden");
    document.getElementById("stop_btn").classList.add("hidden");
    document.getElementById("fileSelector").classList.remove("hidden");
    stopRecording();
    allSignalsMap.clear();
    signalSelector.clearSignalList();

    setFileStatusText("No File Loaded");
    setDAQStatusText("");
    mainDAQ = new SignalDAQLocalFile(onSignalAnnounce,onSignalUnAnnounce,onNewSampleData,onConnect,onDisconnect,setDAQStatusText);

    recordingStartTime = null;
    recordingEndTime = null;
    recordingRunning = false;
}

/////////////////////////////////////////////////////////////
//Data Event Handlers

function onConnect(){
    allSignalsMap.clear();

}

function onDisconnect(){
    stopRecording();
}

function onSignalAnnounce(name, units){
    if(!allSignalsMap.has(name)){ //in case the same signal is announced multiple times, only add unique ones.
        var newSignal = new Signal(name, units);
        allSignalsMap.set(name, newSignal);
        signalSelector.addSignal(newSignal);
        restoreConfig(); 
        plotList.forEach(plt => plt.rectifySignalReferencesByName());
    }
}

function onSignalUnAnnounce(name){
    if(allSignalsMap.has(name)){ 
        var sigToRemove = allSignalsMap.get(name);
        signalSelector.removeSignal(sigToRemove);
        allSignalsMap.delete(name);
    }
}

function onNewSampleData(name, timestamp, value){
    if(allSignalsMap.has(name)){
        var sampleTimeSec = timestamp/1000000.0;
        allSignalsMap.get(name).addSample(new Sample(sampleTimeSec, value));

        //Save off incoming sample timing stats
        //TODO - does NT4 expose a better way to do this?
        if(recordingStartTime == null){
            recordingStartTime = sampleTimeSec;
        }
        recordingEndTime = sampleTimeSec;
    }
}

//Plot callback supporting drag/drop events
// allowing signals to be added to plots using only their name
function signalFromName(name_in){
    return allSignalsMap.get(name_in);
}

// Plot callback supporting keeping the number of 
// value axes aligned (in turn keeps the time vertically aligned across all plots).
function updateAllPlotValueAxisCount(){
    var maxNumAxes = 0;
    plotList.forEach(plot => {
        maxNumAxes = Math.max(maxNumAxes, plot.valueAxesMap.size);
    })
    plotList.forEach(plot => {
        plot.setNumValueAxes(maxNumAxes);
    })
}

///////////////////////////
// Mouse Events

function onChartMouseOver(timeAtMouse){
    if(recordingRunning){
        plotList.forEach(plot=>plot.setCursorPos(null)); //disable cursor while running
    } else {
        plotList.forEach(plot=>plot.setCursorPos(timeAtMouse));
    }
}


function onChartZoomAction(startTime, endTime){
    if(!recordingRunning){
        plotList.forEach(plot=>plot.setDrawRange(startTime, endTime));
    } 
}

///////////////////////////
// Keyboard Events

function keypressHandler(e){
    if(e.code == "Delete"){
        //Remove all selected signals
        plotList.forEach(plot=>{
            plot.plottedSignalsMap.forEach(ps => {
                if(ps.selected == true){
                    plot.removePlottedSignal(ps.signal.name);
                }
            });
        });
    } else if(e.code == "KeyA"){
        //Select All
        plotList.forEach(plot=>{
            plot.plottedSignalsMap.forEach(ps => {
                ps.selected = true;
            });
        });
    } else if(e.code == "KeyU"){
        // Clear Measurement
        unselectAllBtnClick();
    }
}

///////////////////////////
// Status Display Updaters

function setFileStatusText(in_text){
    document.getElementById("filePickerStatus").innerHTML = in_text;
}

function setDAQStatusText(in_text){
    document.getElementById("daqStatus").innerHTML = in_text;
}

///////////////////////////
// Animation Loop

function mainAnimationLoop(){
    if(recordingRunning){
        plotList.forEach(plot=>plot.setDrawRange(recordingEndTime - 10.0, recordingEndTime));
    } 
    plotList.forEach(plot=>plot.mainAnimationLoop());
    window.requestAnimationFrame(mainAnimationLoop);
}

/////////////////////////////////////////////////////////////
//Save/Recal config handlers
function saveCurrentConfig(){
    if(local_storage_available){
        var lsData = new Map();

        var ls_sel_signals = [];
        signalSelector.getSelectedSignalList().forEach(selSig =>{
            ls_sel_signals.push(selSig.name);
        });

        lsData["selSigList"] = ls_sel_signals;
        lsData["plotCount"] = plotList.length;

        var plottedSignalNameListList = [];

        plotList.forEach(plot => {
            var plottedSignalNameList = [];

            plot.plottedSignalsMap.forEach(ps => {
                plottedSignalNameList.push(ps.signal.name);
            })

            plottedSignalNameListList.push(plottedSignalNameList);
        });

        lsData["plottedSignalNameListList"] = plottedSignalNameListList;


        localStorage.setItem(LOCAL_STORAGE_KEY_NAME, JSON.stringify(lsData));
    }
}


function restoreConfig(){
    var lsData = new Map();
    var ls_sel_signals = [];
    var plotCount = null;
    var plottedSignalNameListList = null;
    if(local_storage_available == true){
        lsData = JSON.parse(localStorage.getItem(LOCAL_STORAGE_KEY_NAME))

        if(lsData != null){
            ls_sel_signals = lsData["selSigList"];
            plotCount = lsData["plotCount"];
            plottedSignalNameListList = lsData["plottedSignalNameListList"];
        }

        ls_sel_signals.forEach(sigName => {
            signalSelector.selectSignalByName(sigName);
        });

        if(plotCount != null){
            while(plotList.length < plotCount){
                addPlot();
            }

            if(plottedSignalNameListList != null){
                var endIdx = Math.min(plottedSignalNameListList.length, plotCount);
                for(var pltIdx = 0; pltIdx < endIdx; pltIdx++){
                    var plottedSignalNameList = plottedSignalNameListList[pltIdx];
                    plottedSignalNameList.forEach(psName => {
                        var sig = signalFromName(psName);
                        if(sig){
                            plotList[pltIdx].addSignal(sig);
                            signalSelector.selectSignalByName(psName);
                        }
                    });
                }
            }
        }


    }


}

/////////////////////////////////////////////////////////////
// Other Utilities
function getUrlVars() {
    var vars = {};
    var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
        vars[key] = value;
    });
    return vars;
}