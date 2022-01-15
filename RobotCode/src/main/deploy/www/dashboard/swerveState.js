
//Array index lookups. Must be kept in sync with .java definitions
// Allows for single-read accesses into the different state variables
var AZMTH_ACT_IDX = 0;
var AZMTH_DES_IDX = 1;
var SPEED_ACT_IDX = 2;
var SPEED_DES_IDX = 3;

var MODFL_IDX = 0;
var MODFR_IDX = 1;
var MODBL_IDX = 2;
var MODBR_IDX = 3;

class ModuleState {
    constructor(azmthAct_in, azmthDes_in, speedAct_in, speedDes_in){
        this.stateList = new Array(4);
        this.stateList[AZMTH_ACT_IDX] = azmthAct_in;
        this.stateList[AZMTH_DES_IDX] = azmthDes_in;
        this.stateList[SPEED_ACT_IDX] = speedAct_in;
        this.stateList[SPEED_DES_IDX] = speedDes_in;
    }
}

class Module {
    constructor(canvas_in, drawXFrac_in, drawYFrac_in){
        this.canvas = canvas_in;
        this.ctx = this.canvas.getContext("2d");
        this.drawXFrac = drawXFrac_in;
        this.drawYFrac = drawYFrac_in;
        this.curState = new ModuleState(0,0,0,0);

        this.ACT_OBJ_COLOR = '#FF2D00';
        this.DES_OBJ_COLOR = '#004CFF';

        this.recalcDrawConstants();

    }


    recalcDrawConstants(){

        //Drawing configurations
        this.arrowHeadLen = Math.ceil(this.canvas.width * 0.02);
        this.moduleRadiusPx = Math.ceil(this.canvas.width * 0.15);

        this.drawX = Math.ceil(this.drawXFrac * this.canvas.width);
        this.drawY = Math.ceil(this.drawYFrac * this.canvas.height);

        this.circleOffsetMargin = Math.ceil(this.canvas.width * 0.02);
        this.arrowOffsetMargin = Math.ceil(this.canvas.width * 0.01);
        this.wheelMarkerWidth = Math.ceil(this.canvas.width * 0.03);
    }

    setVal(typeIdx, value){
        this.curState.stateList[typeIdx] = value;
    }


    draw(){
        this.draw_int(this.curState.stateList[AZMTH_ACT_IDX], this.curState.stateList[SPEED_ACT_IDX], true);
        this.draw_int(this.curState.stateList[AZMTH_DES_IDX], this.curState.stateList[SPEED_DES_IDX], false);
    }


    draw_int(rotation_deg, speed_frac, isActual) {
    
        var color = this.DES_OBJ_COLOR;
        var radius = this.moduleRadiusPx + this.circleOffsetMargin;
    
        if(isActual){
            color = this.ACT_OBJ_COLOR;
            radius = this.moduleRadiusPx - this.circleOffsetMargin;
        }
    
        //Tweak rotation to match the javascript canvas draw angle convention
        rotation_deg *= -1;
    
        //Rotate to module reference frame
        this.ctx.translate(this.drawX, this.drawY);
        this.ctx.rotate(rotation_deg * Math.PI / 180);
    
        //Solid filled in red robot is for Actual
        this.ctx.beginPath();
        this.ctx.strokeStyle = color;
        this.ctx.lineWidth = "1";
        this.ctx.arc(0, 0, radius, 0, 2 * Math.PI);
        this.ctx.stroke()
    
        this.ctx.beginPath()
        this.ctx.strokeStyle = color;
        this.ctx.lineWidth = "3";
        this.ctx.moveTo(0,-radius)
        this.ctx.lineTo(0,-radius+this.circleOffsetMargin*2)
        this.ctx.stroke()
    
        var arrowLen = Math.abs(speed_frac* this.moduleRadiusPx * 0.75);
    
        var arrowOffset = this.arrowOffsetMargin;
        if(isActual)
            arrowOffset *= -1;
    
        var speedIsNegative = (speed_frac < 0);
    
        if(speedIsNegative){
            this.ctx.rotate(Math.PI);
            arrowOffset *= -1;
        }
    
        if(Math.abs(speed_frac) > 0.1)
            this.drawArrow(arrowOffset, 0, arrowOffset, -1.0 * (arrowLen));
    
        if(speedIsNegative)
            this.ctx.rotate(-Math.PI);
    
    
        if(isActual){
            //wheel center marker
            this.ctx.beginPath();
            this.ctx.strokeStyle = color;
            this.ctx.lineWidth = "6";
            this.ctx.moveTo(-this.wheelMarkerWidth, 0);
            this.ctx.lineTo(this.wheelMarkerWidth, 0);
            this.ctx.closePath();
            this.ctx.stroke(); 
        }
    
        //Undo rotation/translation
        this.ctx.rotate(-1 * rotation_deg * Math.PI / 180);
        this.ctx.translate(-this.drawX, -this.drawY);
    
    }
    
    drawArrow(fromx, fromy, tox, toy) {
        var dx = tox - fromx;
        var dy = toy - fromy;
        var angle = Math.atan2(dy, dx);
        this.ctx.moveTo(fromx, fromy);
        this.ctx.lineTo(tox, toy);
        this.ctx.lineTo(tox - this.arrowHeadLen * Math.cos(angle - Math.PI / 6), toy - this.arrowHeadLen * Math.sin(angle - Math.PI / 6));
        this.ctx.moveTo(tox, toy);
        this.ctx.lineTo(tox - this.arrowHeadLen * Math.cos(angle + Math.PI / 6), toy - this.arrowHeadLen * Math.sin(angle + Math.PI / 6));
        this.ctx.stroke();
    }

}


export class SwerveState {
    //////////////////////////////////////
    // Public Class methods
    //////////////////////////////////////
    constructor(draw_div_id, title) { 
        this.draw_div_id = draw_div_id;
        this.title = title;


        // State Variable Defaults
        this.hasData = false;


        // Animation - intermediate drawn fill percentage
        // to keep stuff feeling smooth and high quality
        this.animatedFlValue = 0;
        this.prevTime = performance.now();

        // Set up drawing canvas within provided div
        this.canvas = document.createElement('canvas');
        this.docElem = document.getElementById(this.draw_div_id );
        this.canvas.id     = this.draw_div_id + "_canvas";
        this.docElem.appendChild(this.canvas);
        this.ctx = this.canvas.getContext("2d");
        
        //Individual Drawn modules
        this.modList = new Array(4);
        this.modList[MODFL_IDX] = new Module(this.canvas, 0.25, 0.25);
        this.modList[MODFR_IDX] = new Module(this.canvas, 0.75, 0.25);
        this.modList[MODBL_IDX] = new Module(this.canvas, 0.25, 0.75);
        this.modList[MODBR_IDX] = new Module(this.canvas, 0.75, 0.75);
    }

    // Call this when NT is disconnected, or data is otherwise not available
    reportNoData(){
        this.hasData = false;
    }

    // Call this whenever a new value for the widget is available.
    setVal(modIdx, typeIdx, value) { 
        this.modList[modIdx].setVal(typeIdx, value);
        this.hasData = true;
    }

    //Call once per render loop to redraw the gauge
    render() {

        var curTime = performance.now();
        var deltaTime = curTime - this.prevTime;

        this.recalcDrawConstants();

        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

        //Robot outline
        this.ctx.beginPath();
        this.ctx.lineWidth = "1";
        this.ctx.strokeStyle = "#FFFFFF";
        this.ctx.rect(this.botMargin, this.botMargin, this.canvas.width - this.botMargin*2 , this.canvas.height - this.botMargin*2 );
        this.ctx.stroke();

        if(this.hasData){
            this.modList.forEach(mod => mod.draw());
        }

        this.prevTime = curTime;

    }

    //////////////////////////////////////
    // Private, Helper methods
    //////////////////////////////////////

    recalcDrawConstants(){
        this.canvas.width  = this.docElem.offsetWidth;
        this.canvas.height = this.docElem.offsetHeight;

        this.botMargin = this.canvas.width * 0.03;

        this.modList.forEach(mod => mod.recalcDrawConstants());
    }


    


  }