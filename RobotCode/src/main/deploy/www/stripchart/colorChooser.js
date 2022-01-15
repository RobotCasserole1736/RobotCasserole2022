

export class ColorChooser {

    constructor(parentDiv_in, initialHue){
        this.parentDiv = parentDiv_in;
        this.drawDiv = document.createElement("div");
        this.drawDiv.classList.add("jsColorChooser");
        
        this.canvas = document.createElement('canvas');
        this.canvas.id     = this.drawDiv.id + "_canvas";
        this.drawDiv.appendChild(this.canvas);
        this.ctx = this.canvas.getContext("2d");
     
        this.hidetimeout = null;
        
        this.canvas.addEventListener("mousemove", this.onmousemove.bind(this));
        this.canvas.addEventListener("mousedown", this.onmousedown.bind(this));
        this.canvas.addEventListener("mouseup", this.onmouseup.bind(this));
        this.canvas.addEventListener("mouseover", this.onmouseover.bind(this));
        this.canvas.addEventListener("mouseout", this.onmouseout.bind(this));

        this.desHue = initialHue;
        this.updateCurColorString();
        
        this.selecting = false;

        this.parentDiv.appendChild(this.drawDiv);
        this.hide();
    }

    updateCurColorString(){
        this.color = this.hslToHex(this.desHue, 100, 50);
    }

    show(x_pos, y_pos){
        this.drawDiv.classList.remove("hidden");
        this.drawDiv.style.left = x_pos.toString() + "px";
        this.drawDiv.style.top = y_pos.toString() + "px";
        this.canvas.width  = this.drawDiv.clientWidth;
        this.canvas.height = this.drawDiv.clientHeight;
        this.drawBackground();
        this.drawMarker();
    }

    hide(){
        this.drawDiv.classList.add("hidden");
    }

    getCurColor(){
        return this.color;
    }

    drawBackground(){
        var grad = this.ctx.createLinearGradient(0,0,this.canvas.width,0);
        var NUM_STOPS = 30.0;
        for(var idx = 0.0; idx <= NUM_STOPS; idx++){
            var frac = idx/NUM_STOPS;
            grad.addColorStop(frac, this.hslToHex(frac*360, 100, 50));
        }
        this.ctx.fillStyle = grad;
        this.ctx.fillRect(0,0,this.canvas.width, this.canvas.height);
    }

    drawMarker(){
        var xPos = this.desHue / 360.0 * this.canvas.width;

        this.ctx.strokeStyle = 'black';
        this.ctx.lineWidth = 5;
        this.ctx.beginPath();
        this.ctx.moveTo(xPos, 0);
        this.ctx.lineTo(xPos, this.canvas.height);
        this.ctx.stroke();

        this.ctx.strokeStyle = 'white';
        this.ctx.lineWidth = 2;
        this.ctx.beginPath();
        this.ctx.moveTo(xPos, 0);
        this.ctx.lineTo(xPos, this.canvas.height);
        this.ctx.stroke();
    }

    onmouseup(e){
        this.selecting = false;
        e.preventDefault();
        e.stopPropagation();
    }

    onmousedown(e){
        this.selecting = true;
        this.setColorAtMousePos(e.offsetX);
        e.preventDefault();
        e.stopPropagation();
    }

    onmousemove(e){
        this.setColorAtMousePos(e.offsetX);
        e.preventDefault();
        e.stopPropagation();
    }

    onmouseover(e){
        if(this.hidetimeout){
            clearTimeout(this.hidetimeout);
        }
    }

    onmouseout(e){
        this.timeout = setTimeout(this.hide.bind(this), 500);
    }

    setColorAtMousePos(mouseX){

        if(this.selecting){
            this.desHue = Math.round(mouseX/this.canvas.width * 360);
            this.updateCurColorString();
            this.drawBackground();
            this.drawMarker();
        }
    }

    hslToHex(h, s, l) {
        l /= 100;
        const a = s * Math.min(l, 1 - l) / 100;
        const f = n => {
          const k = (n + h / 30) % 12;
          const color = l - a * Math.max(Math.min(k - 3, 9 - k, 1), -1);
          return Math.round(255 * color).toString(16).padStart(2, '0');   // convert to Hex and prefix "0" if needed
        };
        return `#${f(0)}${f(8)}${f(4)}`;
      }


}