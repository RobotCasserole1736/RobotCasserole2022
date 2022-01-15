
export class Text {

    //////////////////////////////////////
    // Public Class methods
    //////////////////////////////////////
    constructor(draw_elem_id, name_in) { 
        this.drawElemID = draw_elem_id;
        this.name = name_in;
        
        // State Variable Defaults
        this.reportNoData();

        this.drawDiv = document.createElement("div");
        this.drawDiv.classList.add("textWidgetBase");

        this.drawElem = document.getElementById(this.drawElemID);
        this.drawElem.setAttribute("data-tooltip", this.name);
        this.drawElem.appendChild(this.drawDiv);

        this.updateFontSize();

    }

    // Call this when NT is disconnected, or data is otherwise not available
    reportNoData(){
        this.hasData = false;
        this.text = "****";
    }

    // Call this whenever a new state for the widget is available.
    setVal(thingToDisplay) { 
        this.text = thingToDisplay.toString(); //Best guess for now?
        this.hasData = true;
    }

    //Call once per render loop to update and redraw the text area
    render() {
        this.updateFontSize();
        this.drawDiv.innerHTML = this.text;
    }

    //////////////////////////////////////
    // Private, Helper methods
    //////////////////////////////////////

    updateFontSize(){
        this.drawDiv.style.fontSize = (this.drawElem.clientHeight * 0.4).toString() + "px";
    }

  }