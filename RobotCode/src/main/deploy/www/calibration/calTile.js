import { SpinBox } from "./spinbox/SpinBox.js";

export class CalTile {

    constructor(drawDiv_in, cal_in, valueSetCallback_in) { 

        this.cal = cal_in;
        this.drawDiv = drawDiv_in;
        this.valueSetCallback = valueSetCallback_in;

        this.spinbox = null;

        this.nameDiv    = this._addColumn(this.cal.name);
        this.unitsDiv   = this._addColumn(this.cal.units);
        this.minDiv     = this._addColumn(this.cal.min);
        this.maxDiv     = this._addColumn(this.cal.max);
        this.defaultDiv = this._addColumn(this.cal.default);
        this.curValDiv  = this._addColumn(this.cal.value);
        this._addControls(this.apply.bind(this), this.reset.bind(this));
    }

    show(){
        this.drawDiv.classList.remove("hidden");
    }

    hide(){
        this.drawDiv.classList.add("hidden");
    }

    reset(){
        this.setCalValue(this.cal.default);
    }

    apply(){
        this.setCalValue(this.spinbox.getValue());
    }

    setCalValue(value){
        this.valueSetCallback(this.cal.name, value);
    }

    // Updates the displayed values for the calibration row
    updateDisplayedValues(){
        if(this.cal.value !== null){
            this.curValDiv.innerHTML = this.cal.value.toString();
        } else {
            this.curValDiv.innerHTML = "***";
        }

        if(this.cal.value != this.cal.default){
            this.curValDiv.classList.add("changed");
        } else {
            this.curValDiv.classList.remove("changed");  
        }

        
        if(this.cal.name != null){    this.nameDiv.innerHTML = this.cal.name.toString(); }
        if(this.cal.units != null){   this.unitsDiv.innerHTML = this.cal.units.toString(); }
        if(this.cal.default != null){ this.defaultDiv.innerHTML = this.cal.default.toString(); }

        if(this.cal.min != null){     
            this.minDiv.innerHTML = this.cal.min.toString(); 
            this.spinbox.options['minimum'] = this.cal.min;
        }
        if(this.cal.max != null){     
            this.maxDiv.innerHTML = this.cal.max.toString(); 
            this.spinbox.options['maximum'] = this.cal.max;
        }

        if(this.cal.min != null & this.cal.max != null){  
            if(this.cal.min != -Infinity & this.cal.max != Infinity){
                var stepSize = (this.cal.max - this.cal.min)/100.0; //For now, just keep it in a reasonable range?
                this.spinbox.options['step'] = stepSize;
                var decimals = Math.ceil(-1.0*Math.log10(stepSize));
                this.spinbox.options['decimals'] = decimals;
            }
        }


    }

    _addColumn(text_in){
        var new_td = document.createElement("td");
        new_td.classList.add("calText");
        new_td.innerHTML = text_in;
        this.drawDiv.appendChild(new_td);
        return new_td;
    }

    _addControls(applyCallback, resetCallback){

        var buttonDiv = document.createElement("div");
 
        var spinBoxContainer = document.createElement("div");
        spinBoxContainer.classList.add("spinBoxContainer");
        this.spinbox = new SpinBox(spinBoxContainer, {'decimals' : 1});
        buttonDiv.appendChild(spinBoxContainer);

        var applyButton = document.createElement("button");
        applyButton.setAttribute("type", "button");
        applyButton.onclick = applyCallback;
        applyButton.innerHTML = "Apply";
        buttonDiv.appendChild(applyButton);


        var resetButton = document.createElement("button");
        resetButton.setAttribute("type", "button");
        resetButton.onclick = resetCallback;
        resetButton.innerHTML = "Reset";
        buttonDiv.appendChild(resetButton);

        var new_td = document.createElement("td");
        new_td.appendChild(buttonDiv);
        this.drawDiv.appendChild(new_td);
        return new_td;
    }

}