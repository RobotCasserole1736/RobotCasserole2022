
export class LineGauge {
    //////////////////////////////////////
    // Public Class methods
    //////////////////////////////////////
    constructor(draw_div_id, name, min_range, max_range, min_acceptable, max_acceptable) { 
        this.draw_div_id = draw_div_id;
        this.name = name;
        this.min_range = min_range;
        this.max_range = max_range;
        this.min_acceptable = min_acceptable;
        this.max_acceptable = max_acceptable;
        
        // State Variable Defaults
        this.hasData = false;
        this.curVal = 0;

        // Animation - intermedeate drawn fill percentage
        // to keep stuff feeling smooth and high quality
        this.animatedCurValue = 0;
        this.prevTime = performance.now();

        // Set up drawing canvas within provided div
        this.canvas = document.createElement('canvas');
        this.docElem = document.getElementById(this.draw_div_id );
        this.canvas.id = this.draw_div_id + "_canvas";
        this.docElem.appendChild(this.canvas);
        this.ctx = this.canvas.getContext("2d");
    }

    // Call this when NT is disconnected, or data is otherwise not available
    reportNoData(){
        this.hasData = false;
        this.curVal = 0;
    }

    // Call this whenever a new value for the widget is available.
    setVal(val) { 
        this.curVal = val;
        this.hasData = true;
    }

    //Call once per render loop to redraw the gauge
    render() {
        var curTime = performance.now();
        var deltaTime = curTime - this.prevTime;

        this.recalcDrawConstants();

        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

        // Draw main text
        this.ctx.font = (this.titleTextSize) + "px localMichroma";
        this.ctx.textBaseline = 'middle';
        this.ctx.fillStyle = "#FFFFFF";
        var displayValueStr = "****"
        if(this.hasData){
            displayValueStr = Math.round(this.curVal).toString();
        }
        var text = this.name + ":   " + displayValueStr;
        this.ctx.fillText(text, this.titleTextAnchorX, this.titleTextAnchorY);

        this.drawGauge();

        if(this.hasData){
            //Animate the arc position smoothly
            var error = this.curVal - this.animatedCurValue ;
            this.animatedCurValue += 10.0 * error * (deltaTime/1000.0);

            //Draw marker on gauge
            if(this.curVal > this.max_acceptable || this.curVal < this.min_acceptable){
                this.ctx.fillStyle = "#FF2222";
            } else {
                this.ctx.fillStyle = "#22DD22";
            }
            this.drawMarker()
        }

        this.prevTime = curTime;

    }

    //////////////////////////////////////
    // Private, Helper methods
    //////////////////////////////////////

    recalcDrawConstants(){
        this.canvas.width  = this.docElem.offsetWidth;
        this.canvas.height = this.docElem.offsetHeight;

        //Drawing configurations

        this.gaugeCenterX = this.canvas.width/2;
        this.gaugeCenterY = this.canvas.height/1.9;

        this.titleTextSize = Math.round(this.canvas.height*0.22);
        this.titleTextAnchorX = this.canvas.width * 0.03;
        this.titleTextAnchorY = this.canvas.height * 0.2;

        this.lineStartX = this.canvas.width * 0.03;
        this.lineStartY = this.canvas.height * 0.55;
        this.lineWidth = this.canvas.width * 0.94;
        this.lineHeight = this.canvas.height * 0.15;

        this.markerRadius =  this.canvas.height * 0.2
    }

    valToXPos(val){
        return this.lineStartX + this.lineWidth * ((val - this.min_range)/(this.max_range - this.min_range));
    }

    drawGauge(){
        //Main gauge outline
        this.ctx.beginPath();
        this.ctx.lineWidth = "1";
        this.ctx.strokeStyle = "#FFFFFF";
        this.ctx.rect(this.lineStartX, this.lineStartY, this.lineWidth, this.lineHeight);
        this.ctx.stroke();

        this.drawRangeMarkLine(this.min_acceptable);
        this.drawRangeMarkLine(this.max_acceptable);
    }

    drawMarker(){
        var adjVal = this.animatedCurValue;
        adjVal = Math.min(adjVal, this.max_range);
        adjVal = Math.max(adjVal, this.min_range);
        var cX = this.valToXPos(adjVal);
        var cY = this.lineStartY + this.lineHeight/2.0;

        //Draw diamond
        this.ctx.beginPath();
        this.ctx.lineWidth = "4";
        this.ctx.strokeStyle = "#BBBBBB";
        this.ctx.moveTo(cX, cY - this.markerRadius); //top
        this.ctx.lineTo(cX - this.markerRadius, cY); //left
        this.ctx.lineTo(cX, cY + this.markerRadius); //bottom
        this.ctx.lineTo(cX + this.markerRadius, cY); //right
        this.ctx.lineTo(cX, cY - this.markerRadius); //top
        this.ctx.fill();
        this.ctx.stroke();
    
    }
  
    drawRangeMarkLine(location){
        if(location > this.min_range && location < this.max_range){
            var markerX = this.valToXPos(location);
            this.ctx.beginPath();
            this.ctx.lineWidth = "4";
            this.ctx.strokeStyle = "#BBBBBB";
            this.ctx.moveTo(markerX, this.lineStartY);
            this.ctx.lineTo(markerX, this.lineStartY + this.lineHeight);
            this.ctx.stroke();
        }
    }

  }