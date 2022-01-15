
export class Camera {

    //////////////////////////////////////
    // Public Class methods
    //////////////////////////////////////
    constructor(draw_div_id, name, url) {
        this.draw_div_id = draw_div_id;
        this.name = name;
        this.url = url;

        this.docElem = document.getElementById(draw_div_id);

        this.docElem.innerHTML = "<img class=\"camera\" src=\"" + this.url + "\" alt=\"Could not connect to camera!\">"

        this.docElem.setAttribute("data-tooltip", this.name);

        this.docElem.onclick = this.handleImageClick.bind(this); //Bind mouse clicks to request browser fullscreen

        this.isFullscreened = false; //Flag to help us determine whether this camera was requested for fullscreen (since event handler is global)

        this.docElem.addEventListener("fullscreenchange", this.handleFullscreenChange.bind(this));
    }

    //Toggle fullscreen
    handleImageClick() {
        if (this.isFullscreened){
            this.closeFullscreen();
        } else {
            this.isFullscreened = true;
            this.openFullscreen();
        }
    }


    /* View in fullscreen */
    openFullscreen() {
        if (this.docElem.requestFullscreen) {
            this.docElem.requestFullscreen();
        } else if (this.docElem.webkitRequestFullscreen) { /* Safari */
            this.docElem.webkitRequestFullscreen();
        } else if (this.docElem.msRequestFullscreen) { /* IE11 */
            this.docElem.msRequestFullscreen();
        }
    }

    /* Close fullscreen */
    closeFullscreen() {
        if (document.exitFullscreen) {
            document.exitFullscreen();
        } else if (document.webkitExitFullscreen) { /* Safari */
            document.webkitExitFullscreen();
        } else if (document.msExitFullscreen) { /* IE11 */
            document.msExitFullscreen();
        }
    }

    handleFullscreenChange() {
        var inFullScreen = null != document.fullscreenElement || /* Standard syntax */
            null != document.webkitFullscreenElement || /* Chrome, Safari and Opera syntax */
            null != document.mozFullScreenElement ||/* Firefox syntax */
            null != document.msFullscreenElement; /* IE/Edge syntax */

        if (this.isFullscreened) {
            if (inFullScreen) {
                this.docElem.classList.add("fullscreenCamera");
            } else {
                this.docElem.classList.remove("fullscreenCamera");
                this.isFullscreened = false;
            }
        }
    }

}