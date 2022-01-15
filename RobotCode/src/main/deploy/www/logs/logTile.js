export class LogTile {

    constructor(drawDiv_in, name_in, size_in, path_in, issueCommandCallback_in) { 

        this.name = name_in;
        this.size_kb = size_in/1024.0;
        this.absolutePath = path_in;
        this.drawDiv = drawDiv_in;
        this.issueCommandCallback = issueCommandCallback_in;

        this._addColumn(this.name);
        this._addColumn(this.size_kb);
        this._addButtons(this.deleteCallback.bind(this), this.downloadCallback.bind(this), this.stripchartCallback.bind(this));
    }

    show(){
        this.drawDiv.style.display = "block";
    }

    hide(){
        this.drawDiv.style.display = "none";
    }

    downloadCallback(){
        window.open("http://" + window.location.hostname + ":" + window.location.port + "/" + this.name);
    }

    stripchartCallback(){
        window.open("http://" + window.location.hostname + ":" + window.location.port + "/stripchart/stripchart.html?fname=" + this.name);
    }

    deleteCallback(){
        var cmdMap = {
            cmd:"delete",
            file: this.absolutePath,
        };
        this.issueCommandCallback(cmdMap);    
    }

    _addColumn(text_in){
        var new_td = document.createElement("td");
        new_td.innerHTML = text_in;
        new_td.classList.add("tableText");
        this.drawDiv.appendChild(new_td);
        return new_td;
    }

    _addButtons(deleteCallback, downloadCallback, stripchartCallback){
        var deleteButton = document.createElement("button");
        deleteButton.setAttribute("type", "button");
        deleteButton.onclick = deleteCallback;
        deleteButton.innerHTML = "Delete";

        var downloadButton = document.createElement("button");
        downloadButton.setAttribute("type", "button");
        downloadButton.onclick = downloadCallback;
        downloadButton.innerHTML = "Download";

        var stripchartButton = document.createElement("button");
        stripchartButton.setAttribute("type", "button");
        stripchartButton.onclick = stripchartCallback;
        stripchartButton.innerHTML = "Open";

        var buttonDiv = document.createElement("div");
        var new_td = document.createElement("td");

        buttonDiv.appendChild(deleteButton);
        buttonDiv.appendChild(downloadButton);
        buttonDiv.appendChild(stripchartButton);
        new_td.appendChild(buttonDiv);
        this.drawDiv.appendChild(new_td);
        return new_td;
    }

}