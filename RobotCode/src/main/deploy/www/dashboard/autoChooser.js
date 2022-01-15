
export class AutoChooser {

    //////////////////////////////////////
    // Public Class methods
    //////////////////////////////////////
    constructor(draw_elem_id, name_in, modeTitleList_in, valChangedCallback_in) { 
        this.drawElemID = draw_elem_id;
        this.modeTitleList = modeTitleList_in; //List of strings describing states. 
        this.name = name_in;
        this.valChangedCallback = valChangedCallback_in;
        
        // State Variable Defaults
        this.desValText = "Select";
        this.reportNoData();

        //Overall draw div
        this.drawDiv = document.createElement("div");
        this.drawDiv.classList.add("textWidgetBase");
        
        //Display of what auto mode is currently selected
        this.actValDiv = document.createElement("div");
        this.actValDiv.classList.add("textWidgetBase");
        this.actValDiv.classList.add("outlined");
        this.drawDiv.appendChild(this.actValDiv);

        //Dropdown menu to let the user select a new auto mode
        // see https://www.w3schools.com/howto/howto_css_dropdown.asp
        var buttonDiv = document.createElement("div");
        buttonDiv.classList.add("dropdown");
        this.buttonElem = document.createElement("div");
        this.buttonElem.classList.add("dropbtn");
        buttonDiv.appendChild(this.buttonElem);
        var dropdownContent = document.createElement("div");
        dropdownContent.classList.add("dropdown-content");
        for(var autoIdx = 0; autoIdx < this.modeTitleList.length; autoIdx++){
            var modeSelectOption = document.createElement("div");
            modeSelectOption.innerHTML = this.modeTitleList[autoIdx];
            modeSelectOption.classList.add("dropdown-option");
            modeSelectOption.onclick = this.optionClickHandler.bind(this, autoIdx);
            dropdownContent.appendChild(modeSelectOption);
        }
        buttonDiv.appendChild(dropdownContent);

        this.desValDiv = document.createElement("div");
        this.desValDiv.classList.add("textWidgetBase");
        this.desValDiv.classList.add("outlined");
        this.desValDiv.appendChild(buttonDiv);
        this.drawDiv.appendChild(this.desValDiv);

        //Put the chooser into the specified div with tooltip
        this.drawElem = document.getElementById(this.drawElemID);
        this.drawElem.setAttribute("data-tooltip", this.name);
        this.drawElem.appendChild(this.drawDiv);
        

        this.updateFontSize();

    }

    // Call this when NT is disconnected, or data is otherwise not available
    reportNoData(){
        this.hasData = false;
        this.actValText = "****";
    }

    // Call this whenever a new state for the widget is available.
    setActualState(autoModeIdx) { 
        if(autoModeIdx >= 0 && autoModeIdx < this.modeTitleList.length){
            this.actValText = this.modeTitleList[autoModeIdx];
        } else {
            this.actValText = "????";
        }
        this.hasData = true;
    }

    //Call once per render loop to update and redraw the text area
    render() {
        this.updateFontSize();
        this.actValDiv.innerHTML = this.actValText;
        this.buttonElem.innerHTML = this.desValText;
    }

    //////////////////////////////////////
    // Private, Helper methods
    //////////////////////////////////////

    updateFontSize(){
        this.drawDiv.style.fontSize = (this.drawElem.clientHeight * 0.3).toString() + "px";
    }

    optionClickHandler(setIdx){
        this.desValText = this.modeTitleList[setIdx];
        this.valChangedCallback(setIdx);
    }
  }