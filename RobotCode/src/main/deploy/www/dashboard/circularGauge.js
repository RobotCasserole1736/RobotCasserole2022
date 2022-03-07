
export class CircularGauge {
    //////////////////////////////////////
    // Public Class methods
    //////////////////////////////////////
    constructor(draw_div_id, title, min_range, max_range, min_acceptable, max_acceptable) { 
        this.draw_div_id = draw_div_id;
        this.title = title;
        this.min_range = min_range;
        this.max_range = max_range;
        this.min_acceptable = min_acceptable;
        this.max_acceptable = max_acceptable;

        // State Variable Defaults
        this.hasData = false;
        this.curVal = 0;

        // Animation - intermediate drawn fill percentage
        // to keep stuff feeling smooth and high quality
        this.animatedCurValue = 0;
        this.prevTime = performance.now();

        // Set up drawing canvas within provided div
        this.canvas = document.createElement('canvas');
        this.docElem = document.getElementById(this.draw_div_id );
        this.canvas.id     = this.draw_div_id + "_canvas";
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

        // Draw Title Text
        this.ctx.font = (this.titleTextSize) + "px localMichroma";
        this.ctx.textBaseline = 'middle';
        this.ctx.textAlign = 'center';
        this.ctx.fillStyle = "#FFFFFF";
        this.ctx.fillText(this.title, this.titleTextAnchorX, this.titleTextAnchorY);

        // Draw gauge outline
        this.ctx.strokeStyle = "#FFFFFF";
        this.drawGauge(this.ARC_END_ANGLE, false);

        // Draw Value Text
        this.ctx.font = (this.valueTextSize) + "px localMichroma";
        this.ctx.fillStyle = "#FFFFFF";
        var displayValueStr = "****";
        if(this.hasData){
            displayValueStr = this.getValAsFixedLenStr();
        }
        this.renderTextFixedSpacing(displayValueStr, this.valueTextAnchorX, this.valueTextAnchorY);

        if(this.hasData){
            
            // Calculate filled portion color
            if(this.curVal > this.max_acceptable || this.curVal < this.min_acceptable){
                this.ctx.fillStyle = "#FF2222";
            } else {
                this.ctx.fillStyle = "#22DD22";
            }

            //Animate the arc position smoothly
            var error = this.curVal - this.animatedCurValue ;
            this.animatedCurValue += 10.0 * error * (deltaTime/1000.0);

            //Calculate the end angle to fill the gauge to
            var gaugeFillFrac = (this.animatedCurValue - this.min_range)/(this.max_range - this.min_range);
            gaugeFillFrac = Math.max(gaugeFillFrac, 0);
            gaugeFillFrac = Math.min(gaugeFillFrac, 1);

            var gaugeEndAngle = (this.ARC_END_ANGLE - this.ARC_START_ANGLE) * gaugeFillFrac + this.ARC_START_ANGLE;

            //Draw filled portion of gauge
            this.drawGauge(gaugeEndAngle, true);
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

        this.titleTextSize = Math.round(this.canvas.height*0.09);
        this.titleTextAnchorX = this.canvas.width * 0.5;
        this.titleTextAnchorY = this.canvas.height * 0.075;

        this.valueTextSize = Math.round(this.canvas.height*0.12);
        this.valueTextAnchorX = this.canvas.width * 0.5;
        this.valueTextAnchorY = this.canvas.height * 0.85;
        this.valueTextSpacing = this.valueTextSize*1.0

        this.ARC_START_ANGLE = 3/4 * Math.PI;
        this.ARC_END_ANGLE   = 9/4 * Math.PI;
        this.ARC_MIN_RADIUS  = this.canvas.width * 0.15;
        this.ARC_MAX_RADIUS  = this.canvas.width * 0.35;

    }

    drawGauge(end_angle, filled){
        var x_1   = this.gaugeCenterX + this.ARC_MIN_RADIUS * Math.cos(this.ARC_START_ANGLE);
        var y_1   = this.gaugeCenterY + this.ARC_MIN_RADIUS * Math.sin(this.ARC_START_ANGLE);
        var x_2   = this.gaugeCenterX + this.ARC_MAX_RADIUS * Math.cos(this.ARC_START_ANGLE);
        var y_2   = this.gaugeCenterY + this.ARC_MAX_RADIUS * Math.sin(this.ARC_START_ANGLE);
        var x_3   = this.gaugeCenterX + this.ARC_MAX_RADIUS * Math.cos(end_angle);
        var y_3   = this.gaugeCenterY + this.ARC_MAX_RADIUS * Math.sin(end_angle);
        var x_4   = this.gaugeCenterX + this.ARC_MIN_RADIUS * Math.cos(end_angle);
        var y_4   = this.gaugeCenterY + this.ARC_MIN_RADIUS * Math.sin(end_angle);
        this.ctx.beginPath();
        this.ctx.moveTo(x_1,y_1);
        this.ctx.lineTo(x_2, y_2);
        this.ctx.arc(this.gaugeCenterX, this.gaugeCenterY, this.ARC_MAX_RADIUS, this.ARC_START_ANGLE, end_angle);
        this.ctx.moveTo(x_3,y_3);
        this.ctx.lineTo(x_4, y_4);
        this.ctx.arc(this.gaugeCenterX, this.gaugeCenterY, this.ARC_MIN_RADIUS, end_angle, this.ARC_START_ANGLE, true);
        if(filled){
            this.ctx.fill();
        } else {
            this.ctx.stroke();
        }
    }

    getValAsFixedLenStr(){
        //We don't wish to display any more than four changing digits
        var dispVal = this.curVal
        if(Math.abs(dispVal) >= 9999){
            return "9999";
        } else if (Math.abs(dispVal) <= 0.001){
            return "0.0";
        } else if(Math.abs(dispVal) <= 0.01){
            return dispVal.toPrecision(1);
        } else if(Math.abs(dispVal) <= 0.1){
            return dispVal.toPrecision(2);
        } else if(Math.abs(dispVal) <= 1.0){
            return dispVal.toPrecision(3);
        }else {
            return dispVal.toPrecision(4);
        }
        
    }

    renderTextFixedSpacing(string, startX, startY){
        this.ctx.textAlign = 'center';
        this.ctx.textBaseline = 'middle';

        var spacing = this.valueTextSpacing;
        var totalLen = string.length * spacing;

        if(string[0] == '-'){
            totalLen -= spacing;
        }

        if(string.includes('.')){
            totalLen -= spacing*0.25;
        }

        var xPos = startX - totalLen/2;

        for(var idx=0; idx < string.length; idx++){
            var thisSpace = spacing;
            if(string[idx] == '.'){
                thisSpace *= 0.5;
            }
            xPos += thisSpace/2;
            this.ctx.fillText(string[idx], xPos, startY);
            xPos += thisSpace/2;
        }
    } 

  }