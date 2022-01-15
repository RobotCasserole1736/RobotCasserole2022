/////////////////////////////////////////////////////////////////////////
// Signal Selector - allows the user to select which signals they
// want recorded.
/////////////////////////////////////////////////////////////////////////

import { SelectableSignal } from "./selectableSignal.js";

export class SignalSelector {


    constructor(drawDiv_in) { 
        this.selectableSignalsList = [];
        this.drawDiv = drawDiv_in;

    }

    // Add a new signal to the selector list
    addSignal(signal_in){
        var newSelSigDiv = document.createElement("button");
        this.selectableSignalsList.push(new SelectableSignal(signal_in,newSelSigDiv));
        this.drawDiv.appendChild(newSelSigDiv);
    }

    // Remove signal from selector list
    removeSignal(signal_in){
        for(var idx = 0; idx < this.selectableSignalsList.length; idx++){
            if(signal_in == this.selectableSignalsList[idx].signal){
                this.drawDiv.removeChild(this.selectableSignalsList[idx].drawDiv);
                this.selectableSignalsList.splice(idx, 1); //remove that signal and splice the list back together so we don't have null entries the middle
            }
        }
    }

    clearSignalList(){
        for(var idx = 0; idx < this.selectableSignalsList.length; idx++){
            this.drawDiv.removeChild(this.selectableSignalsList[idx].drawDiv);
        }    
        this.selectableSignalsList = [];
    }

    selectSignalByName(sigName){
        this.selectableSignalsList.forEach(ssig => {
            if(ssig.signal.name == sigName){
                ssig.select();
            }
        });
    }

    //Get all currently-selected signals
    getSelectedSignalList(){
        var retList = [];

        this.selectableSignalsList.forEach(ssig => {
            if(ssig.isSelected){
                retList.push(ssig.signal);
            }
        });

        return retList;
    }

    // Re-filter the signals shown to the user by a new spec
    setFilterSpec(filterSpec_in){
        var filterSpec = filterSpec_in.toLowerCase();

        if(filterSpec.length == 0 ){ //no filter, show all
            this.selectableSignalsList.forEach(ssig => ssig.show());
        } else { //Filtering, do the inclusion check
            this.selectableSignalsList.forEach(ssig => {
                if(ssig.signal.name.toLowerCase().includes(filterSpec)){
                    ssig.show();
                } else {
                    ssig.hide();
                }
            });
        }
    }

    clearSelection(){
        this.selectableSignalsList.forEach(ssig => ssig.unselect());
    }

    enableUserInteraction(){
        this.selectableSignalsList.forEach(ssig => ssig.enable());
    }

    disableUserInteraction(){
        this.selectableSignalsList.forEach(ssig => ssig.disable());
    }

}