
export class FastChart {
    //////////////////////////////////////
    // Public Class methods
    //////////////////////////////////////
    constructor(drawContainer_in) { 

        this.startTime = 0;
        this.endTime = 1;

        this.yMin = 0;
        this.yMax = 1;
        
        this.cursorTime = null;

        this.mouseoverAtTimeCallback = null; //expect to be set to a function by the top-level stripcharts functionality to sync all charts.

        this.zoomRangeDn = null;
        this.zoomRangeUp = null;

        this.zoomRangeUpdateCallback = null; //expect to be set to a function by the top-level stripcharts functionality to sync all charts.

        // Set up drawing canvas within provided div
        this.drawContainer = drawContainer_in;
        this.drawDiv = document.createElement('chartDrawDiv');
        this.drawContainer.appendChild(this.drawDiv);
        this.canvas = document.createElement('canvas');
        this.canvas.id     = this.drawDiv.id + "_canvas";
        this.drawDiv.appendChild(this.canvas);
        this.ctx = this.canvas.getContext("2d");
        this.canvas.addEventListener('mousemove', this.mouseoverHandler.bind(this), false);
        this.canvas.addEventListener('mouseleave', this.mouseleaveHandler.bind(this), false);
        this.canvas.addEventListener('mouseup', this.mouseupHandler.bind(this), false);
        this.canvas.addEventListener('mousedown', this.mousedownHandler.bind(this), false);
        if (this.canvas.addEventListener) {
            this.canvas.addEventListener("mousewheel", this.mousewheelHandler.bind(this), false);
            this.canvas.addEventListener("DOMMouseScroll", this.mousewheelHandler.bind(this), false);
        } else {
            this.canvas.attachEvent("onmousewheel", this.mousewheelHandler.bind(this));
        }
        

    }


    setTimeRange(startTime_in, endTime_in){
        this.startTime = startTime_in;
        this.endTime = endTime_in;
    }

    clearDrawing(){
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
    }

    //////////////////////////////////////
    // Private, Helper methods
    //////////////////////////////////////

    recalcDrawConstants(numValueAxes){
        this.canvas.width  = this.drawContainer.clientWidth;
        this.canvas.height = this.drawContainer.clientHeight;

        //Drawing configurations

        this.AXIS_MARGIN = 10;
        this.VALUE_AXIS_WIDTH = 30;
        this.AXIS_Y_BOTTOM_MARGIN = 30;

        this.plotOriginX_px = Math.round(this.AXIS_MARGIN + this.VALUE_AXIS_WIDTH * numValueAxes);
        this.plotOriginY_px = Math.round(this.canvas.height - this.AXIS_Y_BOTTOM_MARGIN);

        this.xAxisLen_px = this.canvas.width - this.plotOriginX_px;
        this.yAxisLen_px = this.plotOriginY_px;

        this.dataMarkerCircleRadius = this.canvas.height * 0.005;

        this.cursorTime = null;

    }

    drawXMarkers(){
        this.getTickMarkList(this.startTime, this.endTime, 1.0).forEach(markerTime => {
            var xPos = this.timeToX_px(markerTime);
            this.ctx.strokeStyle = "#555555";
            this.ctx.lineWidth = 1;
            this.ctx.beginPath();
            this.ctx.moveTo(xPos, 0);
            this.ctx.lineTo(xPos, this.plotOriginY_px);
            this.ctx.stroke();

            this.ctx.strokeStyle = "#FFFFFF";
            this.ctx.lineWidth = 1;
            this.ctx.beginPath();
            this.ctx.moveTo(xPos, this.plotOriginY_px);
            this.ctx.lineTo(xPos, this.canvas.height);
            this.ctx.stroke();

            this.ctx.font = "18px monospace";
            this.ctx.textBaseline = 'top';
            this.ctx.fillStyle = "#FFFFFF";
            this.ctx.fillText(markerTime.toPrecision(3), xPos + 2, this.plotOriginY_px + 2);
        });

    }

    drawYMarkers(yMin, yMax){
        //TODO
    }

    setCursorPos(newTime){
        this.cursorTime = newTime;
    }

    drawCursor(){
        if(this.cursorTime != null){
            var cursorPos = this.timeToX_px(this.cursorTime);
            this.ctx.strokeStyle = "#FFFF00";
            this.ctx.lineWidth = 2;
            this.ctx.beginPath();
            this.ctx.moveTo(cursorPos, 0);
            this.ctx.lineTo(cursorPos, this.canvas.height);
            this.ctx.stroke();
        }
    }

    drawZoomBox(){
        if(this.zoomRangeDn != null && this.cursorTime != null){
            var zpx = this.timeToX_px(this.zoomRangeDn);
            var cpx = this.timeToX_px(this.cursorTime);
            this.ctx.fillStyle = "#333355";
            this.ctx.lineWidth = 0;
            this.ctx.moveTo(zpx, 0);
            this.ctx.lineTo(cpx, 0);
            this.ctx.lineTo(cpx, this.plotOriginY_px);
            this.ctx.lineTo(zpx, this.plotOriginY_px);
            this.ctx.lineTo(zpx, 0);
            this.ctx.fill();
        }
    }

    getTickMarkList(min, max, decimationFactor){
        var range = max - min;
        var orderOfMag = Math.pow(10.0, Math.floor(Math.log10(range)));
        var markerSpacing = 1;

        if (range / orderOfMag >= 5)
            markerSpacing = orderOfMag;
        else if (range / (orderOfMag / 2.0) >= 5)
            markerSpacing = orderOfMag / 2.0;
        else
            markerSpacing = orderOfMag / 5.0;

        markerSpacing *= decimationFactor;

        var markerStart = Math.ceil(min / markerSpacing) * markerSpacing;
        var markerCur = markerStart;
        var markerList = [];

        while(markerCur < max){
            markerList.push(markerCur);
            markerCur += markerSpacing;
        }
        return markerList;
    }

    drawAxes(valueAxisMap){
        this.ctx.strokeStyle = "#FFFFFF";
        this.ctx.lineWidth = 2;
        this.ctx.beginPath();
        //X axis
        this.ctx.moveTo(this.AXIS_MARGIN, this.plotOriginY_px);
        this.ctx.lineTo(this.canvas.width, this.plotOriginY_px);
        this.ctx.stroke();


        //Y axes
        var vaIdx = 0;
        valueAxisMap.forEach( va => {
            var xPos = this.plotOriginX_px - vaIdx * this.VALUE_AXIS_WIDTH;
            this.ctx.moveTo(xPos, 0);
            this.ctx.lineTo(xPos, this.canvas.height);
            this.ctx.stroke();

            var yMin = va.minVal;
            var yMax = va.maxVal;


            this.ctx.font = "12px monospace";
            this.ctx.textBaseline = 'middle';
            this.ctx.textAlign = 'center';
            this.ctx.fillStyle = "#FFFFFF";
            var labelXPos = xPos - this.VALUE_AXIS_WIDTH/2;
            var labelYPos = (this.plotOriginY_px + this.canvas.height)/2;

            // underlined, rotated text for units label
            this.ctx.save()
            this.ctx.translate(labelXPos,labelYPos)
            this.ctx.rotate(-Math.PI/4);
            this.ctx.fillText(va.units,0,0);
            this.ctx.fillText("_____",0,0);
            this.ctx.restore();


            this.getTickMarkList(yMin, yMax, 2.0).forEach(markerVal => {
                var yPos = this.valToY_px(markerVal, yMin, yMax);
                this.ctx.strokeStyle = "#FFFFFF";
                this.ctx.lineWidth = 1;
                this.ctx.beginPath();
                this.ctx.moveTo(xPos - this.VALUE_AXIS_WIDTH/2.1, yPos);
                this.ctx.lineTo(xPos, yPos);
                this.ctx.stroke();
    
                this.ctx.save();
                this.ctx.translate(xPos, yPos);
                this.ctx.rotate(-Math.PI/2);
                this.ctx.font = "15px monospace";
                this.ctx.textBaseline = 'bottom';
                this.ctx.textAlign = 'left';
                this.ctx.fillStyle = "#FFFFFF";
                this.ctx.fillText(markerVal.toPrecision(3), 2, -2);
                this.ctx.restore();

            });
            vaIdx++;

        });


    }

    drawSeries(sampleList, yMin, yMax, colorString_in, bold_in){
        if(sampleList.length > 2){
            this.ctx.strokeStyle = colorString_in;
            this.ctx.lineWidth = bold_in ? 3 : 1;
            this.ctx.beginPath();
            var x_px = this.timeToX_px(sampleList[0].time);
            var y_px = this.valToY_px(sampleList[0].value, yMin, yMax);
            this.ctx.moveTo(x_px, y_px);
            for(var sampIdx = 1; sampIdx < sampleList.length; sampIdx++){
                x_px = this.timeToX_px(sampleList[sampIdx].time);
                //this.ctx.lineTo(x_px, y_px); //uncomment to make a step-chart
                y_px = this.valToY_px(sampleList[sampIdx].value, yMin, yMax);
                this.ctx.lineTo(x_px, y_px);
            }
            this.ctx.stroke();

            if(sampleList.length < 75 && Math.abs(sampleList[0].time - this.startTime) < 1.0){
                //Draw individual data point markers
                this.ctx.fillStyle = colorString_in;
                for(var sampIdx = 0; sampIdx < sampleList.length; sampIdx++){
                    x_px = this.timeToX_px(sampleList[sampIdx].time);
                    y_px = this.valToY_px(sampleList[sampIdx].value, yMin, yMax);
                    this.ctx.beginPath();
                    this.ctx.arc(x_px, y_px, this.dataMarkerCircleRadius * (bold_in ? 3.0 : 1.0), 0, 2 * Math.PI);
                    this.ctx.fill();
                }
            }

        }
    }

    ///////////////////////////////////
    // Mouse-over cursor Handlers
    mouseoverHandler(e){
        if(this.mouseoverAtTimeCallback != null){
            var time = this.xPxToTime(e.x);
            if(time > this.startTime && time < this.endTime){
                this.mouseoverAtTimeCallback(time);
            } else {
                this.mouseoverAtTimeCallback(null);
            }
        }
    }

    mouseleaveHandler(e){
        if(this.mouseoverAtTimeCallback != null){
            this.mouseoverAtTimeCallback(null);
        }
        
        this.resetZoomRangeHandlers();

    }

    ///////////////////////////////////
    // Click-to-Zoom Handlers
    mousedownHandler(e){
        this.resetZoomRangeHandlers();

        //Save off the time where the user clicked down 
        var time = this.xPxToTime(e.x);
        if(time > this.startTime && time < this.endTime){
            this.zoomRangeDn = time;
        } 
    }

    mouseupHandler(e){
        //Save off the time where the user released
        var time = this.xPxToTime(e.x);
        if(time > this.startTime && time < this.endTime){
            this.zoomRangeUp = time;
        } else {
            this.zoomRangeUp = null;
        }

        if(this.zoomRangeDn != null && this.zoomRangeUp != null && this.zoomRangeDn != this.zoomRangeUp){
            //If this mouse-up produced a valid zoom range...
            var newZoomTimeStart = 0;
            var newZoomTimeEnd = 0;

            //order the up/down click points properly to range start/end
            if(this.zoomRangeDn < this.zoomRangeUp){
                newZoomTimeEnd = this.zoomRangeUp;
                newZoomTimeStart = this.zoomRangeDn;
            } else {
                newZoomTimeEnd = this.zoomRangeDn;
                newZoomTimeStart = this.zoomRangeUp;
            }

            //Execute callback to update charts as needed.
            if(this.zoomRangeUpdateCallback != null){
                this.zoomRangeUpdateCallback(newZoomTimeStart, newZoomTimeEnd);
            }
        }
        
        this.resetZoomRangeHandlers();

    }

    resetZoomRangeHandlers(){
        this.zoomRangeUp = null;
        this.zoomRangeDn = null;
    }

    ///////////////////////////////////
    // scroll-to-zoom handlers
    mousewheelHandler(e) {
        // cross-browser wheel delta
        var e = window.event || e;
        var delta = Math.max(-1, Math.min(1, (e.wheelDelta || -e.detail)));
        

        //We should adjust the view extents based on the read-in mouse wheel scroll action

        //Calculate the zoom action center (presumed to be center of chart if not yet set)
        var center = (this.endTime + this.startTime)/2;
        if(this.cursorTime != null){
            center = this.cursorTime ;
        } 
        
        //Calculate the above/below center widths
        var old_right_size = this.endTime - center;
        var old_left_size = center - this.startTime;
        
        //Calculate a multiplicative factor (1.0 for no change, > 1.0 for more zoom, < 1.0 for less zoom)
        var scaler = (1-delta*0.1);
        
        //Apply the factor to the above/below center widths
        var new_right_size = old_right_size * scaler;
        var new_left_size = old_left_size * scaler;
        
        //calculate the new extents
        var newEndTime = center + new_right_size;
        var newStartTime = center - new_left_size;
        
        this.zoomRangeUpdateCallback(newStartTime, newEndTime);

    
        //Return false to prevent this mouse event we're handling here from scrolling the page
        if (e.preventDefault)e.preventDefault();
        return false;
    }


    ///////////////////////////////////
    // Pixel/time/value/units 
    xPxToTime(x_px_in){
        var frac = (x_px_in - this.plotOriginX_px)/(this.canvas.width- this.plotOriginX_px);
        return this.startTime + (this.endTime - this.startTime) * frac;
    }

    timeToX_px(time_in){
        var frac = (time_in - this.startTime)/(this.endTime - this.startTime);
        return this.plotOriginX_px + this.xAxisLen_px * frac;
    }

    valToY_px(val_in, yMin, yMax){
        var frac = (val_in - yMin)/(yMax - yMin);
        return this.plotOriginY_px - this.yAxisLen_px * frac; //Negative produces coordinate transform to pixel space
    }


  }