
export class Sound {

    //////////////////////////////////////
    // Public Class methods
    //////////////////////////////////////
    constructor(name_in, filePath_in, looping_in) { 
        this.filePath = filePath_in;
        this.name = name_in;
        this.looping = looping_in; // True: Playback whenever active, looping. False: Single shot, full-file playback on rising edge.
        
        // State Variable Defaults
        this.reportNoData();
        this.playbackActive = false;


        try{
            this.audio = new Audio(this.filePath);
            this.audio.load();
            this.audio.loop = this.looping;
        } catch(e){
            console.error("Error while attempting to load audio file" + this.filePath + " for " + this.name);
            console.error(e);
        }
        

    }

    // Call this when NT is disconnected, or data is otherwise not available
    reportNoData(){
        this.hasData = false;
        this.playbackActiveCmd = false;
        this.playbackActiveCmdPrev = false;
    }

    // Call this whenever a new state for the widget is available.
    setVal(playbackActive_in) { 
        this.hasData = true;
        this.playbackActiveCmd = playbackActive_in;

    }

    //Call once per render loop to update and redraw the...ummm...never mind, this makes a sound.
    //Yes, "render" the sound into your ears. Yes.
    render() {
        var curCmd = this.playbackActiveCmd;
        var prevCmd = this.playbackActiveCmdPrev;
        // try to play a sound, but don't try too hard.
        if(this.audio != null){
            if(this.looping){
                if(curCmd == true && this.playbackActive == false){
                    //Just got a command to start
                    this.safePlay();
                    this.playbackActive = true;
                } else if(curCmd == false && this.playbackActive == true) {
                    //Just got command to stop
                    this.safePause();
                    this.playbackActive = false;
                } else {
                    //maintain same playback state
                }
            } else {
                if(curCmd == true && prevCmd == false){
                    this.audio.currentTime = 0;
                    this.safePlay();
                }
            }

        }

        this.playbackActiveCmdPrev = curCmd;
    }

    //////////////////////////////////////
    // Private, Helper methods
    //////////////////////////////////////

    safePlay(){
        try{
            this.audio.play();
        } catch (e) {
            console.log(e);
        }
    }

    safePause(){
        try{
            this.audio.pause();
        } catch (e) {
            console.log(e);
        }
    }


  }