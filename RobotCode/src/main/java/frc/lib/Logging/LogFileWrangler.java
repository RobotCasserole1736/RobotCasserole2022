package frc.lib.Logging;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import frc.robot.Robot;

public class LogFileWrangler {

    /** Full set of all registered calibrations on this robot */
    public ArrayList<LogFile> allLogFiles = new ArrayList<LogFile>(0);

    final String OUTPUT_DIR_RIO = "/U/data_captures/"; // USB drive is mounted to /U on roboRIO
    final String OUTPUT_DIR_LOCAL = "./sim_data_captures/"; // local directory for log files in sim.

    // path where we expect all our log files to live at.
    public Path logFilePath;

    /* Singleton infrastructure */
    private static LogFileWrangler instance;

    public static LogFileWrangler getInstance() {
        if (instance == null) {
            instance = new LogFileWrangler();
        }
        return instance;
    }

    private LogFileWrangler() {
        if (Robot.isReal()) {
            logFilePath = Path.of(OUTPUT_DIR_RIO);
        } else {
            logFilePath = Path.of(OUTPUT_DIR_LOCAL);
        }
    }

    /**
     * Return a list or something of all log files presently on disk and
     * available for manipulation.
     */
    public List<LogFile> getLogFileListing() {
        File logDir = logFilePath.toFile();
        ArrayList<LogFile> retList = new ArrayList<LogFile>();

        // list all the files
        try {
            File[] files = logDir.listFiles();
            for (File file : files) {
                if (file.isFile()) {
                    retList.add(new LogFile(file.getAbsolutePath()));
                }
            }
        } catch (Exception e) {
            e.getStackTrace();
        }

        return retList;

    }

    /**
     * prepare a zip file of all log files stored in the log directory
     */
    public Path createZip() {
        Path zipFolder = Path.of(logFilePath.toString(), "archive");
        Path zipPath = Path.of(zipFolder.toString(), "logs.zip");

        File dir = zipFolder.toFile();
        if (!dir.exists())
            dir.mkdirs();

        ZipUtils zip = new ZipUtils(logFilePath.toString());
        zip.generateFileList(logFilePath.toFile());
        zip.zipIt(zipPath.toString());

        return zipPath; 
    }

    public void deleteLog(Path fileToDelete) {
        fileToDelete.toFile().delete();
    }

    public void deleteAllLogs() {
        File logDir = logFilePath.toFile();

        // remove all the files
        try {
            File[] files = logDir.listFiles();
            for (File file : files) {
                if (file.isFile()) {
                    file.delete();
                }
            }
        } catch (Exception e) {
            e.getStackTrace();
        }

    }
}
