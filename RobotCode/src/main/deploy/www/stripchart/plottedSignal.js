/////////////////////////////////////////////////////////////////////////
// PlottedSignal - A signal, plus additional info for drawing it
// nicely. Includes info for drawing the info next to the plot in a
// table-like thing.
/////////////////////////////////////////////////////////////////////////

import { ColorChooser } from "./colorChooser.js";

export class PlottedSignal {

    constructor(signal_in, initialHue_in, valueAxis_in, drawDiv_in) { 
        this.signal = signal_in;
        this.drawDiv = drawDiv_in;

        this.colorChooser = new ColorChooser(this.drawDiv, initialHue_in);

        this.selected = false;

        this.valueAxis = valueAxis_in;

        // Draw textual value display
        this.drawDiv.classList.add("plottedSignalInfo");
        this.drawDiv.setAttribute('draggable', true);
        this.drawDiv.addEventListener( "dragstart", this.onDragStart.bind(this) );
        this.drawDiv.setAttribute('title', signal_in.name);

        var nameInfo = document.createElement("plottedSignalName");
        nameInfo.innerHTML = signal_in.name.substring(signal_in.name.lastIndexOf(".") + 1); //only show the last portion of the name for long names.
        this.drawDiv.appendChild(nameInfo);

        var unitsInfo = document.createElement("plottedSignalUnits");
        unitsInfo.innerHTML = signal_in.units;
        this.drawDiv.appendChild(unitsInfo);

        this.valueInfo = document.createElement("plottedSignalValue");
        this.valueInfo.innerHTML = "----";
        this.drawDiv.appendChild(this.valueInfo);

        this.updateColor();

    }

    autoScale(startTime, endTime){
        var sampleList = this.signal.getSamples(startTime, endTime);
        this.valueAxis.autoScale(sampleList);
    }

    getSamples(startTime, endTime){
        var sampleList = this.signal.getSamples(startTime, endTime);
        return sampleList;
    }

    updateColor(){
        this.drawDiv.style.color = this.colorChooser.getCurColor();
    }

    showValueAtTime(time_in){
        var sample = null;

        if(time_in == null){
            sample = this.signal.getLatestSample();
        } else {
            sample = this.signal.getSample(time_in);
        }

        if(sample != null){
            if (typeof sample.value === "number") {
                this.valueInfo.innerHTML = sample.value.toPrecision(4);
            } else {
                this.valueInfo.innerHTML = sample.value.toString();
            }
        } else {
            this.valueInfo.innerHTML = "----";
        }

        if(this.selected){
            this.drawDiv.classList.add("selectedText");
        } else {
            this.drawDiv.classList.remove("selectedText");
        }

        this.updateColor();
    }

    onDragStart = e => {
        e.dataTransfer.setData('text/plain', this.signal.name);
    }

}