import { NT4_Client } from "../interfaces/nt4.js";

var nt4Client = new NT4_Client(window.location.hostname, 
                               topicAnnounceHandler,
                               topicUnannounceHandler,
                               valueUpdateHandler,
                               onConnect,
                               onDisconnect
                               );


console.log("Starting connection...");
nt4Client.ws_connect();
console.log("Connection Triggered");

var table = document.getElementById("mainTable");

var subscription = null;

mainRenderLoop();

function topicAnnounceHandler( newTopic ) {
    console.log("----------------------------");
    console.log("Topic Announced");
    console.log(newTopic.name);
    console.log(newTopic.type);
    console.log(newTopic.id);

    var newRow = table.insertRow();
    newRow.id = newTopic.id + "_row";
    newRow.insertCell(0).innerHTML = newTopic.id;
    newRow.insertCell(1).innerHTML = newTopic.name;
    newRow.insertCell(2).innerHTML = newTopic.type;

    
    var valCell = newRow.insertCell(3);
    valCell.innerHTML = "";
    valCell.id = newTopic.id;
    valCell.prevValue = "";
    valCell.colorDecayCounter = 0;

    subscribeToAll();

}

function topicUnannounceHandler( removedTopic ) {
    console.log("----------------------------");
    console.log("Topic UnAnnounced");
    console.log(removedTopic.name);
    document.getElementById(removedTopic.id + "_row").remove();
}

function componentToHex(c) {
    c = Math.round(c);
    if(c > 255){ c = 255;}
    if(c < 0){c = 0;}
    var hex = c.toString(16);
    return hex.length == 1 ? "0" + hex : hex;
  }
  
  function rgbToHex(r, g, b) {
    return "#" + componentToHex(r) + componentToHex(g) + componentToHex(b);
  }

function counterToColor(counter){
    var MAX_COUNTER = 25.0;
    var HIGHLIGHT_R = 0xFC;
    var IDLE_R = 0x11;
    var HIGHLIGHT_G = 0x00;
    var IDLE_G = 0x11;
    var HIGHLIGHT_B = 0x00;
    var IDLE_B = 0x11;
    var frac = counter/MAX_COUNTER;

    var r = frac * HIGHLIGHT_R + (1-frac) * IDLE_R;
    var g = frac * HIGHLIGHT_G + (1-frac) * IDLE_G;
    var b = frac * HIGHLIGHT_B + (1-frac) * IDLE_B;

    return rgbToHex(r,g,b);

}

function mainRenderLoop(){

    for(var i=1, len=table.rows.length; i<len; i++){
        var valCell = table.rows[i].cells[3];
        if(valCell.colorDecayCounter > 0){
            valCell.colorDecayCounter--;
            valCell.style.backgroundColor = counterToColor(valCell.colorDecayCounter);
        }

    }

    requestAnimationFrame(mainRenderLoop);
}


function valueUpdateHandler( topic, timestamp_us, value ) {
    //Update shown value
    var valCell = document.getElementById(topic.id);
    valCell.prevValue = valCell.innerHTML;
    valCell.innerHTML = value.toString().padEnd(25, '\xa0'); //This feels hacky as all getout, but prevents table vibration.

    if(valCell.prevValue !== valCell.innerHTML){
        valCell.colorDecayCounter = 25;
    }

    //update time
    document.getElementById("curTime").innerHTML = "Time: ";
    document.getElementById("curTime").innerHTML += (timestamp_us / 1000000.0).toFixed(2);
    //console.log("----------------------------");
    //console.log("Values Updated");
    //console.log(topic.name);
    //console.log(timestamp_us);
    //console.log(value);
}

function onConnect() {
    table.innerHTML = "";

    document.getElementById("status").innerHTML = "Connected to Server";
    var titleRow = table.insertRow(0);

    var newCell;


    newCell =titleRow.insertCell(0)
    newCell.innerHTML = "<b>ID</b>";
    newCell.onclick = function() { sortTableNumeric(0); };

    newCell =titleRow.insertCell(1)
    newCell.innerHTML = "<b>Name</b>";
    newCell.onclick = function() { sortTableAlphabetic(1); };

    newCell =titleRow.insertCell(2)
    newCell.innerHTML = "<b>Type</b>";
    newCell.onclick = function() { sortTableAlphabetic(2); };

    newCell =titleRow.insertCell(3)
    newCell.innerHTML = "<b>Value</b>";


}

function onDisconnect() {
    document.getElementById("status").innerHTML = "Disconnected from Server";
    subscription = null;
}


function subscribeToAll() {
    if(subscription == null){
        subscription = nt4Client.subscribePeriodic(["/"], 0.02);
    }

}

// from https://stackoverflow.com/questions/7558182/sort-a-table-fast-by-its-first-column-with-javascript-or-jquery
function sortTableNumeric(n){
    var store = [];
    for(var i=1, len=table.rows.length; i<len; i++){
        var row = table.rows[i];
        var sortnr = parseFloat(row.cells[n].textContent || row.cells[n].innerText);
        if(!isNaN(sortnr)) store.push([sortnr, row]);
    }
    store.sort(function(x,y){
        return x[0] - y[0];
    });
    for(var i=0, len=store.length; i<len; i++){
        table.appendChild(store[i][1]);
    }
    store = null;
}

function sortTableAlphabetic(n){
    var store = [];
    for(var i=1, len=table.rows.length; i<len; i++){
        var row = table.rows[i];
        var sortnr = (row.cells[n].textContent || row.cells[n].innerText);
        if(sortnr) store.push([sortnr, row]);
    }
    store.sort(function(x,y){
        return x[0].localeCompare(y[0]);
    });
    for(var i=0, len=store.length; i<len; i++){
        table.appendChild(store[i][1]);
    }
    store = null;
}


window.filterChangeHandler = filterChangeHandler;
function filterChangeHandler(filterSpec_in){
    for(var i=1, len=table.rows.length; i<len; i++){
        var row = table.rows[i];
        var name = row.cells[1].textContent;
        if(name.toLowerCase().includes(filterSpec_in.toLowerCase())){
            row.style.display = ""; //show
        } else {
            row.style.display = "none"; //hide
        }
    }
}