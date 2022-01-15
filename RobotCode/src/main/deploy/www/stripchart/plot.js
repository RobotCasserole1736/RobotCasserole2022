/////////////////////////////////////////////////////////////////////////
// Plot - collection of the fastChart object, plus the table which
// shows signal names and values, and a set of value axes.
/////////////////////////////////////////////////////////////////////////


import { FastChart } from './fastChart.js';
import { PlottedSignal } from './plottedSignal.js';
import { ValueAxis } from './valueAxis.js';


export class Plot {

    constructor(drawDiv_in, signalFromNameCallback_in, numAxesUpdatedCallback_in) { 

        //Save off a reference to the relevant div
        this.drawDiv = drawDiv_in;

        // Init the plotted signals list to be empty
        this.plottedSignalsMap = new Map();
        this.valueAxesMap = new Map();
        this.numValueAxes = 0; //Note, this may be bigger than valueAxesMap.size to align with other charts.

        // External ranges to synchronize all plots in terms of what they show 
        // and what actually needs drawn on the screen at any given time.
        this.drawStartTime = 0;
        this.drawEndTime = 0;

        this.cursorTime = null;

        //Each plot has has two side-by-side flex containers - one for highcharts (the actual plot window)
        // and one for the table of currently plotted signals.
        this.hcContainer = document.createElement('plotHighchartsContainer');
        this.hcContainer.id = this.drawDiv.id + "_hcContainer";
        this.psContainer = document.createElement('plotSignalInfoContainer');
        this.psContainer.id = this.drawDiv.id + "_psContainer";
        
        this.drawDiv.appendChild(this.hcContainer);
        this.drawDiv.appendChild(this.psContainer);

        //Configure the whole plot div to support signals dropped onto them.
        this.signalFromNameCallback = signalFromNameCallback_in; //supporting drop operation requires getting the actual signal object using only the name, which would come from the next architectural layer up. Yucky, but functional.
        this.numAxesUpdatedCallback = numAxesUpdatedCallback_in;
        this.drawDiv.addEventListener('dragenter', this.dragEnter)
        this.drawDiv.addEventListener('dragover', this.dragOver);
        this.drawDiv.addEventListener('dragleave', this.dragLeave);
        this.drawDiv.addEventListener('drop', this.drop);

        this.chart = new FastChart(this.hcContainer);

        //Create a list of default hues to use for coloring signals
        this.defaultHueList = [];
        for(var i = 0; i < 10; i++){
            this.defaultHueList.push((107 * i) % 360); //made up numbers to make some spread out colors or whatevs
        }
        this.colorCounter = 0;

        this.numAxesUpdatedCallback();

    }

    setCursorPos(cursorTime_in){
        this.cursorTime = cursorTime_in;

    }

    setNumValueAxes(num_in){
        this.numValueAxes = Math.max(this.valueAxesMap.size, num_in);
    }

    drawDataToChart(){
        //Clear and reset plot
        this.chart.recalcDrawConstants(this.numValueAxes);
        this.chart.clearDrawing();

        //Calculate and set up min/max x and y ranges
        this.chart.setTimeRange(this.drawStartTime, this.drawEndTime);
        this.valueAxesMap.forEach(va => va.resetScale());
        this.plottedSignalsMap.forEach(ps => ps.autoScale(this.drawStartTime, this.drawEndTime));

        //Draw chart elements. Z order: first = back, last = front.
        this.chart.drawAxes(this.valueAxesMap);
        this.chart.setCursorPos(this.cursorTime);
        this.chart.drawZoomBox();
        this.chart.drawXMarkers();

        //Draw all non-selected signals
        this.plottedSignalsMap.forEach(ps => {
            if(ps.selected == false){
                var samples = ps.getSamples(this.drawStartTime,this.drawEndTime);
                this.chart.drawSeries(samples, ps.valueAxis.minVal, ps.valueAxis.maxVal, ps.colorChooser.getCurColor(), ps.selected);
            }
        });

        //Draw selected signals
        this.plottedSignalsMap.forEach(ps => {
            if(ps.selected == true){
                var samples = ps.getSamples(this.drawStartTime,this.drawEndTime);
                this.chart.drawSeries(samples, ps.valueAxis.minVal, ps.valueAxis.maxVal, ps.colorChooser.getCurColor(), ps.selected);
            }
        });

        this.chart.drawCursor();
        
    }



    addSignal(signal_in){
        if(!this.plottedSignalsMap.has(signal_in.name)){
            //Check if we already have an axis to put this on
            var newValueAxis = null;
            this.valueAxesMap.forEach(va => {
                if(va.units == signal_in.units){
                    newValueAxis = va;
                }
            });

            //If we didn't have an existing axis, make a new one
            if(newValueAxis == null){
                newValueAxis = new ValueAxis(signal_in.units);
                this.valueAxesMap.set(signal_in.units, newValueAxis);
                this.numAxesUpdatedCallback();
            }

            var initialHue = this.defaultHueList[this.colorCounter % this.defaultHueList.length];
            this.colorCounter++;
            var newPltSigDiv = document.createElement("plottedSignalInfo");
            var newPS = new PlottedSignal(signal_in, initialHue, newValueAxis, newPltSigDiv);
            newPltSigDiv.addEventListener("mouseup", this.mouseup.bind(this));
            newPltSigDiv.addEventListener("contextmenu", this.contextmenu.bind(this));
            newPltSigDiv.addEventListener("click", this.click.bind(this));
            newPltSigDiv.setAttribute("data:sigName", signal_in.name);
            this.plottedSignalsMap.set(signal_in.name, newPS);
            this.psContainer.appendChild(newPltSigDiv);
        }
    }

    removePlottedSignal(psName_in){

        var ps_in = this.plottedSignalsMap.get(psName_in);
        var oldUnits = ps_in.valueAxis.units;

        // Remove plotted signal tile
        this.psContainer.removeChild(ps_in.drawDiv);

        // Remove plotted signal object by value (not by key)
        this.plottedSignalsMap.delete(psName_in);

        // Check if any other signal was using the same axis.
        var axisOrphaned = true;
        this.plottedSignalsMap.forEach(ps => {
            if(ps.valueAxis.units == oldUnits){
                axisOrphaned = false;
            }
        });

        // Remove the axis if no one else is using it.
        if(axisOrphaned){
            this.valueAxesMap.delete(oldUnits);
            this.numAxesUpdatedCallback();
        }
    }

    //When a DAQ changes out, there signals that disappear and get 
    // recreated. Technically they're not the same as previous. However,
    // sometimes it makes sense to re-attach plotted signals to underlying 
    // signal objects by using the referenced name.
    rectifySignalReferencesByName(){
        this.plottedSignalsMap.forEach((ps, sigName) => {
            var newSig = this.signalFromNameCallback(sigName);
            if(newSig != null){
                ps.signal = newSig;
            }
        });
    }

    setDrawRange(startTime, endTime){
        this.drawStartTime = startTime;
        this.drawEndTime = endTime;
    }

    ////////////////////////////////////////////
    // Main Animation Loop & utilities
    mainAnimationLoop(){

        this.drawDataToChart();
        this.updateDisplayedValues();
     }

    updateDisplayedValues(){
        this.plottedSignalsMap.forEach(ps => {
            if(this.cursorTime == null){
                ps.showValueAtTime(null); //latest
            } else {
                ps.showValueAtTime(this.cursorTime);
            }
        })
    }

    ////////////////////////////////////////////
    // Drag & Drop Handlers
    dragEnter = e => {
        e.preventDefault();
        this.drawDiv.classList.add('drag-over');
    }
    
    dragOver = e => {
        e.preventDefault();
        this.drawDiv.classList.add('drag-over');
    }
    
    dragLeave = e => {
        this.drawDiv.classList.remove('drag-over');
    }
    
    drop = e => {
        // get the draggable element
        e.preventDefault();
        const signalName = e.dataTransfer.getData('text/plain');
        this.addSignal(this.signalFromNameCallback(signalName));
        this.drawDiv.classList.remove('drag-over');
    }

    ////////////////////////////////////////////
    // Signal mouse handlers

    mouseup = e => {

        var sigName = e.currentTarget.getAttribute("data:sigName");
        var ps = this.plottedSignalsMap.get(sigName);

        this.plottedSignalsMap.forEach(ps => { ps.colorChooser.hide()}); //always close out all color choosers on click.

        if(e.which == 1){
            //left click toggles selected
            ps.selected ^= true; //toggle
        } else if(e.which == 2){
            //Middle click removes
            this.removePlottedSignal(sigName);
        } else if(e.which == 3) {
            //Right click shows color chooser
            ps.colorChooser.show(e.pageX, e.pageY);
        }

        e.preventDefault();
    }

    click = e => {
        if(e.which == 2){
            //prevent signal remove from doing that scroll-with-mouse-move
            e.preventDefault();
        }
    }

    contextmenu = e => {
        //do nothing, action handled in mouseup
        e.preventDefault();
    }

}
