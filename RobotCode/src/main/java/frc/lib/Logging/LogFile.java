package frc.lib.Logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.json.JSONObject;

/**
 * LogFile - class to describe one log file the user could interact with
 */
public class LogFile {

    Path filePath;
    long size_bytes;
    String shortName;

    public LogFile(String fpath) {
        filePath = Path.of(fpath);
        try {
            size_bytes = Files.size(filePath);
        } catch (IOException e) {
            size_bytes = 0;
        }
        shortName = filePath.getFileName().toString();
    }

    public JSONObject getJSON(){
        JSONObject retObj = new JSONObject();

        retObj.put("filePath", filePath.toString());
        retObj.put("size_bytes", Long.toString(size_bytes));
        retObj.put("shortName", shortName);

        return retObj;
    }
    
}
