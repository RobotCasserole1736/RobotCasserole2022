package frc.lib.Webserver2.LogFiles;

/*
 *******************************************************************************************
 * Copyright (C) FRC Team 1736 Robot Casserole - www.robotcasserole.org
 *******************************************************************************************
 *
 * This software is released under the MIT Licence - see the license.txt
 *  file in the root of this repo.
 *
 * Non-legally-binding statement from Team 1736:
 *  Thank you for taking the time to read through our software! We hope you
 *   find it educational and informative! 
 *  Please feel free to snag our software for your own use in whatever project
 *   you have going on right now! We'd love to be able to help out! Shoot us 
 *   any questions you may have, all our contact info should be on our website
 *   (listed above).
 *  If you happen to end up using our software to make money, that is wonderful!
 *   Robot Casserole is always looking for more sponsors, so we'd be very appreciative
 *   if you would consider donating to our club to help further STEM education.
 */

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.Timer;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import frc.lib.Logging.LogFile;
import frc.lib.Logging.LogFileWrangler;
import frc.lib.Logging.SignalFileLogger;
import frc.lib.Signal.SignalWrangler;

/**
 * DESCRIPTION: <br>
 * Private socket definition class that Jetty wants me to make public even
 * though it doesn't actually have to be. Don't use this for anything unless you
 * know preciisely what you are doing.
 */

public class LogFileStreamerSocket extends WebSocketAdapter {

    private Timer updater = null;


    @Override
    public void onWebSocketText(String messageStr) {
        if (isConnected()) {

            JSONObject msg = new JSONObject(messageStr);
            String cmd = "";
            try{
                cmd = msg.get("cmd").toString();
            } catch (JSONException e){
                System.out.println("Malformed jSON - no cmd");
            }

            if(cmd.equals("deleteAll")){
                System.out.println("Deleting All Log Files");
                LogFileWrangler.getInstance().deleteAllLogs();

            } else if(cmd.equals("delete")) {
                String fileName = msg.get("file").toString();
                Path fileToDelete = Path.of(fileName);
                System.out.println("Deleting Log File");
                LogFileWrangler.getInstance().deleteLog(fileToDelete);

            } else if(cmd.equals("downloadAll")) {
                System.out.println("Zipping up log files...");
                Path zipPath = LogFileWrangler.getInstance().createZip();
                reportZipAvailable(zipPath);

            } else {
                System.out.println("Malformed jSON - cmd \"" + cmd + "\" unrecognized.");
            }

            sendCurrentLogFileList();
        }
    }

    @Override
    public void onWebSocketConnect(Session sess) {
        super.onWebSocketConnect(sess);
        sendCurrentLogFileList();

        updater = new java.util.Timer("Log File Streamer Periodic Update for " + sess.getRemoteAddress().toString());
        updater.scheduleAtFixedRate(new PeriodicUpdateTask(), 0, 500);
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        updater.cancel();
        super.onWebSocketClose(statusCode, reason);
    }

    /**
     * Send current list of files and data to client
     */
    public void sendCurrentLogFileList() {
        if (isConnected()) {
            try {
                JSONObject full_obj = new JSONObject();
                JSONArray data_array = new JSONArray();
                ArrayList<JSONObject> logFileJsonObjs = new ArrayList<JSONObject>();

                for(LogFile lf : LogFileWrangler.getInstance().getLogFileListing()){
                    logFileJsonObjs.add(lf.getJSON());
                }
                data_array.putAll(logFileJsonObjs);
                full_obj.put("type", "new_log_file_list");
                full_obj.put("files", data_array);
                getRemote().sendString(full_obj.toString());

            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
    }

    /**
     * Reports the zipping of all files is complete, and the client should attempt to download it.
     */
    public void reportZipAvailable(Path zipPath) {
        if (isConnected()) {
            try {
                JSONObject full_obj = new JSONObject();
                full_obj.put("type", "zip_ready");
                full_obj.put("path", LogFileWrangler.getInstance().logFilePath.relativize(zipPath));
                getRemote().sendString(full_obj.toString());
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
    }

    /**
     * Send a new status string about current logger state.
     */
    public void sendStatusString(String status) {
        if (isConnected()) {
            try {
                JSONObject full_obj = new JSONObject();
                full_obj.put("type", "status");
                full_obj.put("string", status);
                getRemote().sendString(full_obj.toString());
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
    }


    private class PeriodicUpdateTask extends TimerTask {

        boolean wasLogging = false;

        @Override
        public void run() {
            String curStatus = "Idle";
            SignalFileLogger logger = SignalWrangler.getInstance().logger;
            Path curLogFile = logger.curLogFile;
            if(curLogFile != null && logger.loggingActive){
                curStatus = "Writing to " + curLogFile.getFileName().toString();
                wasLogging = true;
            } else {
                if(wasLogging){
                    sendCurrentLogFileList();
                    wasLogging = false;
                }
            }
            sendStatusString(curStatus);
        }
        
    }
}
