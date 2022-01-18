var POSE_TYPE_DES_IDX = 0;
var POSE_TYPE_EST_IDX = 1;
var POSE_TYPE_ACT_IDX = 2;

var POSE_ELEM_X_IDX = 0;
var POSE_ELEM_Y_IDX = 1;
var POSE_ELEM_R_IDX = 2;

var FIELD_X_M = 16.4592;
var FIELD_Y_M = FIELD_X_M/2.0;

var STYLE_FIELD = 0;
var STYLE_BOT_DES = 1;
var STYLE_BOT_ACT = 2;
var STYLE_BOT_EST = 3;

var BOT_X_LEN_M = 0.75;
var BOT_Y_LEN_M = 0.5;

class RobotPose {
    constructor(){
        this.comp = Array(3);
        this.comp[POSE_ELEM_X_IDX]  = 0;
        this.comp[POSE_ELEM_Y_IDX]  = 0;
        this.comp[POSE_ELEM_R_IDX]  = 0;
    }
}

export class FieldPose {
    //////////////////////////////////////
    // Public Class methods
    //////////////////////////////////////
    constructor(draw_div_id, title) { 
        this.draw_div_id = draw_div_id;
        this.title = title;

        this.poses = Array(3);
        this.poses[POSE_TYPE_DES_IDX] = new RobotPose();
        this.poses[POSE_TYPE_EST_IDX] = new RobotPose();
        this.poses[POSE_TYPE_ACT_IDX] = new RobotPose();

        // Drawing Constants
        this.margin_px = 5;

        // State Variable Defaults
        this.hasData = false;

        // Animation - intermediate drawn fill percentage
        // to keep stuff feeling smooth and high quality
        this.animatedCurValue = 0;
        this.prevTime = performance.now();

        // Set up drawing canvas within provided div
        this.canvas = document.createElement('canvas');
        this.canvas.style.backgroundImage = "url('fields/2022.png')";
        this.canvas.style.backgroundSize = "cover";
        this.docElem = document.getElementById(this.draw_div_id );
        this.canvas.id = this.draw_div_id + "_canvas";
        this.docElem.appendChild(this.canvas);
        this.ctx = this.canvas.getContext("2d");

    }

    // Call this when NT is disconnected, or data is otherwise not available
    reportNoData(){
        this.hasData = false;
    }

    // Call this whenever a new value for the widget is available.
    setVal(typeIdx, elemIdx, val) { 
        this.hasData = true;
        this.poses[typeIdx].comp[elemIdx] = val;
    }

    //Call once per render loop to redraw the gauge
    render() {

        this.recalcDrawConstants();
        
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

        this.drawObj(STYLE_FIELD, FIELD_X_M/2.0, FIELD_Y_M/2.0, FIELD_X_M, FIELD_Y_M, 0.0);

        if(this.hasData){
            this.drawObj(STYLE_BOT_DES, this.poses[POSE_TYPE_DES_IDX].comp[POSE_ELEM_X_IDX], this.poses[POSE_TYPE_DES_IDX].comp[POSE_ELEM_Y_IDX], BOT_X_LEN_M, BOT_Y_LEN_M, this.poses[POSE_TYPE_DES_IDX].comp[POSE_ELEM_R_IDX]);
            this.drawObj(STYLE_BOT_EST, this.poses[POSE_TYPE_EST_IDX].comp[POSE_ELEM_X_IDX], this.poses[POSE_TYPE_EST_IDX].comp[POSE_ELEM_Y_IDX], BOT_X_LEN_M, BOT_Y_LEN_M, this.poses[POSE_TYPE_EST_IDX].comp[POSE_ELEM_R_IDX]);
            this.drawObj(STYLE_BOT_ACT, this.poses[POSE_TYPE_ACT_IDX].comp[POSE_ELEM_X_IDX], this.poses[POSE_TYPE_ACT_IDX].comp[POSE_ELEM_Y_IDX], BOT_X_LEN_M, BOT_Y_LEN_M, this.poses[POSE_TYPE_ACT_IDX].comp[POSE_ELEM_R_IDX]);
        }


    }

    //////////////////////////////////////
    // Private, Helper methods
    //////////////////////////////////////

    recalcDrawConstants(){
        this.canvas.width  = this.docElem.offsetWidth;
        this.canvas.height = this.docElem.offsetHeight;
    }

    drawObj(type, center_x_m, center_y_m, len_x_m, len_y_m, rotation_rad){

        var xLen_px = this.mToPx(len_x_m);
        var yLen_px = this.mToPx(len_y_m);
        var center_x_px = this.mToPx_X(center_x_m);
        var center_y_px = this.mToPx_Y(center_y_m);


        this.ctx.save();
        //Rotate to module reference frame
        this.ctx.translate(center_x_px, center_y_px);
        this.ctx.rotate(-1.0 * rotation_rad);

        switch(type){
            case STYLE_FIELD:
                this.ctx.beginPath();
                this.ctx.lineWidth = "1";
                this.ctx.strokeStyle = "#FFFFFF";
                this.ctx.rect(-xLen_px/2, -yLen_px/2, xLen_px, yLen_px);
                this.ctx.stroke();
                break;
            case STYLE_BOT_DES:
                this.ctx.beginPath();
                this.ctx.lineWidth = "2";
                this.ctx.strokeStyle = "#000000";
                this.ctx.fillStyle = "#FF0000";
                this.ctx.rect(-xLen_px/2, -yLen_px/2, xLen_px, yLen_px);
                this.ctx.fill();
                this.ctx.stroke();


                this.ctx.fillStyle = "#000000";
                this.ctx.beginPath();
                this.ctx.moveTo(-xLen_px/3, -yLen_px/3);
                this.ctx.lineTo(-xLen_px/3, yLen_px/3);
                this.ctx.lineTo(xLen_px/2, 0);
                this.ctx.closePath();
                this.ctx.fill();

                break;
            case STYLE_BOT_ACT:
                this.ctx.beginPath();
                this.ctx.lineWidth = "2";
                this.ctx.strokeStyle = "#000000";
                this.ctx.fillStyle = "#00FF00";
                this.ctx.rect(-xLen_px/2, -yLen_px/2, xLen_px, yLen_px);
                this.ctx.fill();
                this.ctx.stroke();


                this.ctx.fillStyle = "#000000";
                this.ctx.beginPath();
                this.ctx.moveTo(-xLen_px/3, -yLen_px/3);
                this.ctx.lineTo(-xLen_px/3, yLen_px/3);
                this.ctx.lineTo(xLen_px/2, 0);
                this.ctx.closePath();
                this.ctx.fill();
                

                break;
            case STYLE_BOT_EST:
                this.ctx.beginPath();
                this.ctx.lineWidth = "2";
                this.ctx.strokeStyle = "#000000";
                this.ctx.fillStyle = "#00FFFF";
                this.ctx.rect(-xLen_px/2, -yLen_px/2, xLen_px, yLen_px);
                this.ctx.fill();
                this.ctx.stroke();

                this.ctx.fillStyle = "#000000";
                this.ctx.beginPath();
                this.ctx.moveTo(-xLen_px/3, -yLen_px/3);
                this.ctx.lineTo(-xLen_px/3, yLen_px/3);
                this.ctx.lineTo(xLen_px/2, 0);
                this.ctx.closePath();
                this.ctx.fill();

                break;
        }

        this.ctx.restore();
    }

    mToPx(m_in){
        return (this.canvas.width - this.margin_px*2)/FIELD_X_M * m_in;
    }

    mToPx_X(m_in){
        return this.mToPx(m_in) + this.margin_px;
    }

    mToPx_Y(m_in){
        return this.canvas.height - this.mToPx(m_in) - this.margin_px;
    }
    
}