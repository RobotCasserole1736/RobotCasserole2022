package frc.lib.PathPlanner;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Parses a Pathweaver-like .json for its pose objects, and then runs the wpilib path generation on
 * the RIO to generate custom trajectories.
 */
public class PoseListGenerator {

  LinkedList<Pose2d> outputWaypoints = new LinkedList<Pose2d>();

  final String resourceBaseLocal = "./src/main/deploy/pathData";
  final String resourceBaseRIO = "/home/lvuser/deploy/pathData";
  String resourceBase = resourceBaseRIO; // default to roboRIO

  public PoseListGenerator(String sourceFile) {

    // Check if the path for resources expected on the roboRIO exists.
    if (Files.exists(Paths.get(resourceBaseRIO))) {
      // If RIO path takes priority (aka we're running on a roborio) this path takes
      // priority
      resourceBase = resourceBaseRIO;
    } else {
      // Otherwise use a local path, like we're running on a local machine.
      resourceBase = resourceBaseLocal;
    }

    parse(Path.of(resourceBase, sourceFile));
  }

  private void parse(Path fpath) {

    System.out.println("Starting path import from " + fpath.toString());
    int pointCounter = 0;
    JSONParser parser = new JSONParser();
    Reader reader;
    try {
      reader = new FileReader(fpath.toString());

      JSONArray pointsArr = (JSONArray) parser.parse(reader);

      JSONObject thisStep = null;
      boolean skipped = false;
      Iterator<JSONObject> it = pointsArr.iterator();
      while (it.hasNext()) {
        thisStep = it.next();
        double curvature = Math.abs((Double) thisStep.get("curvature"));

        int adaptiveDecFactor = calcDecimationFactor(curvature);

        if (pointCounter % adaptiveDecFactor == 0) {
          outputWaypoints.add(jsonDataToPose2d(thisStep, (pointCounter == 0)));
          skipped = false;
        } else {
          skipped = true;
        }

        pointCounter++;
      }

      // Ensure the very last point is always added.
      if (skipped) {
        outputWaypoints.add(jsonDataToPose2d(thisStep, false));
      }

      reader.close();

    } catch (IOException | ParseException e) {
      e.printStackTrace();
    }

    System.out.println("Import complete!");
  }

  Pose2d jsonDataToPose2d(JSONObject thisStep, boolean isInit) {

    JSONObject pose = (JSONObject) thisStep.get("pose");
    // System.out.println("point = " + pose.toString());

    JSONObject rotation = (JSONObject) pose.get("rotation");
    JSONObject translation = (JSONObject) pose.get("translation");

    // Transform to wpilib's reference frame
    double inputRotRad = ((Double) rotation.get("radians"));
    double inputTransX = ((Double) translation.get("x"));
    double inputTransY = ((Double) translation.get("y"));

    // System.out.println(curvature);
    var trans = new Translation2d(inputTransX, inputTransY);
    var rot = new Rotation2d(inputRotRad);
    return new Pose2d(trans, rot);
  }

  /**
   * Calculates the number of points to skip based on the provided curvature Tight curves mean we
   * need lots of points.
   *
   * @param curvature
   * @return
   */
  int calcDecimationFactor(double curvature) {
    int adaptiveDecFactor = 0;
    if (curvature > 0.2) {
      adaptiveDecFactor = 10;
    } else if (curvature > 0.05) {
      adaptiveDecFactor = 40;
    } else if (curvature < 0.01) {
      adaptiveDecFactor = 99990; // effectively skip all points
    } else {
      adaptiveDecFactor = 80; // default
    }
    return Math.max(1, adaptiveDecFactor);
  }

  public List<Pose2d> getPoseList() {
    return outputWaypoints;
  }
}
