
//////////////////////////////////////////////////
// Logic to run on page load
//////////////////////////////////////////////////

import { CalTile } from "./calTile.js";
import { NT4_CalInf } from "../interfaces/NT4_CalInf.js";

var calTilesMap = new Map();
var mainTable = document.getElementById("calValueTable");

var calInf = new NT4_CalInf(onNewCalAdded, onCalValueChange, onConnect, onDisconnect);

//////////////////////////////////////////////////
// Render & Animation Loop Functions
//////////////////////////////////////////////////

window.resetAll = resetAll;
function resetAll(){
    calTilesMap.forEach(cal => cal.reset());
}

// Re-filter the calibrations shown to the user by a new spec
window.filterChangeHandler = filterChangeHandler;
function filterChangeHandler(filterSpec_in){
    var filterSpec = filterSpec_in.toLowerCase();

    if(filterSpec.length == 0 ){ //no filter, show all
        calTilesMap.forEach(calTile => calTile.show());
    } else { //Filtering, do the inclusion check
        calTilesMap.forEach(calTile => {
            if(calTile.cal.name.toLowerCase().includes(filterSpec)){
                calTile.show();
            } else {
                calTile.hide();
            }
        });
    }
}

function onNewCalAdded(newCal){
    var new_tr = document.createElement("tr");
    new_tr.classList.add("calRow");
    calTilesMap.set(newCal.name, new CalTile(new_tr, newCal, calInf.setCalibrationValue.bind(calInf)));
    mainTable.appendChild(new_tr);
}

function onCalUnAnnounce(oldCal){
    var trToRemove = calTilesMap[oldCal.name].drawDiv;
    mainTable.removeChild(trToRemove);
    calTilesMap.delete(oldCal.name);

}

function onCalValueChange(cal){
    calTilesMap.get(cal.name).updateDisplayedValues();
}

function onConnect(){
    calTilesMap.clear();
}

function onDisconnect(){

}