
export class Icon {

    static kOFF = 0;
    static kON  = 1;
    static kBLINK_FAST  = 2;
    static kBLINK_SLOW  = 3;

    static blinkSlowPeriodLoops = 60;
    static blinkFastPeriodLoops = 32;
    static blinkDC = 0.6;

    //////////////////////////////////////
    // Public Class methods
    //////////////////////////////////////
    constructor(draw_div_id, name, color_on, symbolPath) { 
        this.draw_div_id = draw_div_id;
        this.name = name;
        this.color_on = color_on;
        this.symbolPath = symbolPath;
        
        // State Variable Defaults
        this.hasData = false;
        this.curState = 0;
        this.blinkCounter = 0;

        // Animation - intermedeate color scalar
        // to keep stuff feeling smooth and high quality
        this.animatedBrightnessVal = 0;
        this.prevTime = performance.now();

        this.docElem = document.getElementById(draw_div_id);
        this.docElem.setAttribute("data-tooltip", this.name);
    }

    // Call this when NT is disconnected, or data is otherwise not available
    reportNoData(){
        this.hasData = false;
        this.curState = Icon.kOFF;
    }

    // Call this whenever a new state for the widget is available.
    setVal(state) { 
        this.curState = state;
        this.hasData = true;
    }

    //Call once per render loop to redraw the gauge
    render() {

        var curTime = performance.now();
        var deltaTime = curTime - this.prevTime;

        // Pick the color based on the current state of the icon.
        var desBrigness = 0;
  
        if(this.curState == Icon.kBLINK_SLOW){
            //Pick on state for high enough counter values
            if(this.blinkCounter > (Icon.blinkSlowPeriodLoops * (1.0-Icon.blinkDC))){
                desBrigness = 1.0;
            } 

            //Reset counter at period
            if(this.blinkCounter > Icon.blinkSlowPeriodLoops){
                this.blinkCounter = 0;
            } else {
                this.blinkCounter++;
            }
        } else if(this.curState == Icon.kBLINK_FAST){
            //Pick on state for high enough counter values
            if(this.blinkCounter > (Icon.blinkFastPeriodLoops * (1.0-Icon.blinkDC))){
                desBrigness = 1.0;
            } 

            //Reset counter at period
            if(this.blinkCounter > Icon.blinkFastPeriodLoops){
                this.blinkCounter = 0;
            } else {
                this.blinkCounter++;
            }

        } else {
            //Non-blink - just use the state to drive the icon color
            this.blinkCounter = 0;
            if(this.curState == Icon.kON){
                desBrigness = 1.0;
            }
        }

        //Animate the color smoothly
        var error = desBrigness - this.animatedBrightnessVal ;
        this.animatedBrightnessVal += 30.0 * error * (deltaTime/1000.0);
        this.animatedBrightnessVal = Math.min(this.animatedBrightnessVal, 1.0);
        this.animatedBrightnessVal = Math.max(this.animatedBrightnessVal, 0.0);

        var iconDrawColor = this.scaleBrightness(this.color_on, this.animatedBrightnessVal);
        var shadowDrawColor = this.scaleBrightness(this.color_on, this.animatedBrightnessVal*0.65);

        if(iconDrawColor != null){
            //Draw an icon with the specified color
            this.docElem.innerHTML = "<div style=\"filter: drop-shadow(0 0 0.2vw " + shadowDrawColor + "); width:100%; height:100%\">" + 
                                     "<div class=\"iconGraphic\" style=\"background-color:" + iconDrawColor + "; "+
                                     "mask:url("+ this.symbolPath +");-webkit-mask-image:url("+ this.symbolPath +"); "+
                                     "-webkit-mask-size: cover;mask-size: cover;" +
                                     "\"></div></div>";
        } else {
            //Make the area blank
            this.docElem.innerHTML = "";
        }

        this.prevTime = curTime;


    }

    //////////////////////////////////////
    // Private, Helper methods
    //////////////////////////////////////

    scaleBrightness(H, scalar) { //From https://css-tricks.com/converting-color-spaces-in-javascript/
        // Convert hex to RGB first
        let r = 0, g = 0, b = 0;
        if (H.length == 4) {
          r = "0x" + H[1] + H[1];
          g = "0x" + H[2] + H[2];
          b = "0x" + H[3] + H[3];
        } else if (H.length == 7) {
          r = "0x" + H[1] + H[2];
          g = "0x" + H[3] + H[4];
          b = "0x" + H[5] + H[6];
        }
        // Then to HSL
        r /= 255;
        g /= 255;
        b /= 255;
        let cmin = Math.min(r,g,b),
            cmax = Math.max(r,g,b),
            delta = cmax - cmin,
            h = 0,
            s = 0,
            l = 0;
      
        if (delta == 0)
          h = 0;
        else if (cmax == r)
          h = ((g - b) / delta) % 6;
        else if (cmax == g)
          h = (b - r) / delta + 2;
        else
          h = (r - g) / delta + 4;
      
        h = Math.round(h * 60);
      
        if (h < 0)
          h += 360;
      
        l = (cmax + cmin) / 2;
        s = delta == 0 ? 0 : delta / (1 - Math.abs(2 * l - 1));
        s = +(s * 100).toFixed(1);
        l = +(l * 100).toFixed(1);
      
        var maxL = l;
        var minL = 15.0;
        var maxS = s;
        var minS = 10.0;

        l = minL + scalar * (maxL - minL);
        s = minS + scalar * (maxS - minS);

        s /= 100;
        l /= 100;
      
        let c = (1 - Math.abs(2 * l - 1)) * s,
            x = c * (1 - Math.abs((h / 60) % 2 - 1)),
            m = l - c/2;
      
        if (0 <= h && h < 60) {
          r = c; g = x; b = 0;
        } else if (60 <= h && h < 120) {
          r = x; g = c; b = 0;
        } else if (120 <= h && h < 180) {
          r = 0; g = c; b = x;
        } else if (180 <= h && h < 240) {
          r = 0; g = x; b = c;
        } else if (240 <= h && h < 300) {
          r = x; g = 0; b = c;
        } else if (300 <= h && h < 360) {
          r = c; g = 0; b = x;
        }
        // Having obtained RGB, convert channels to hex
        r = Math.round((r + m) * 255).toString(16);
        g = Math.round((g + m) * 255).toString(16);
        b = Math.round((b + m) * 255).toString(16);
      
        // Prepend 0s, if necessary
        if (r.length == 1)
          r = "0" + r;
        if (g.length == 1)
          g = "0" + g;
        if (b.length == 1)
          b = "0" + b;
      
        return "#" + r + g + b;

      }

  }