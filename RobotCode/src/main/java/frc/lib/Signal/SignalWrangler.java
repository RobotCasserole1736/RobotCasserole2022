package frc.lib.Signal;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import frc.lib.Logging.SignalFileLogger;
import frc.lib.miniNT4.LocalClient;
import frc.lib.miniNT4.samples.TimestampedValue;
import frc.lib.miniNT4.topics.Topic;

public class SignalWrangler extends LocalClient{

    /* Singleton infrastructure */
    private static SignalWrangler instance;

    public static SignalWrangler getInstance() {
        if (instance == null) {
            instance = new SignalWrangler();
        }
        return instance;
    }

    /** Full set of all registered signals on this robot */
    public ArrayList<Signal> registeredSignals = new ArrayList<Signal>(0);

    // File logger for signals
    public SignalFileLogger logger;

    private SignalWrangler() {
        logger = new SignalFileLogger();
    }

    public int register(Signal sig_in) {
        int ret_val = 0;
        if (registeredSignals.contains(sig_in)) {
            DriverStation.reportWarning("[SignalWrangler] WARNING: " + sig_in.name
                    + " has already been added to the signal wrangler. Nothing done.", false);
            ret_val = -1;
        } else {
            registeredSignals.add(sig_in);
            ret_val = 0;
        }
        return ret_val;
    }

    public Signal getSignalFromName(String name_in) {
        for (Signal sig : registeredSignals) {
            if (sig.name.equals(name_in)) {
                return sig;
            }
        }
        return null;
    }

    public List<Signal> getAllSignals() {
        return registeredSignals;
    }

    /** Set of all auto-discovered signals from @Signal annotations */
    Set<AutoDiscoveredSignal> autoSig;
    /**
     * Set of all java Reflection objects that we've analyzed for whether they
     * have @Signal annotation or not In the event that two objects reference each
     * other, this should help break an infinite-recursion case, since each unique
     * object only needs to be checked once.
     */
    Set<Object> checkedObjects;

    /**
     * Recursively-called signals-finding function. Starting at some object `root`
     * it traverses each field (ie, variable or object or whatever) declared withing
     * the class of that object, finds the objects of each of those fields, and does
     * none of two things: 1) Check if it's annotated to be a signal 2) Otherwise,
     * call the function recursively on the new "child" object. Recursion stops if
     * we hit #1, or if the object's class is not "frc.robot" - this should help
     * keep search time reasonable, since this happens at runtime at robot init.
     * Note this should NOT be called at periodic runtime.... because a) it's not
     * tested that way and b) recursion.
     * 
     * @param root
     * @param prefix
     */
    void findAllAnnotatedSignals(Object root, String prefix) {
        Class rootClass = root.getClass();
        Package rootPkg = rootClass.getPackage();

        if(rootPkg == null){
            return; //Stop, no more tree to traverse
        }

        if (rootPkg.toString().contains("frc.sim") || rootPkg.toString().contains("frc.robot") || rootPkg.toString().contains("frc.wrappers")) {
            // If we've got a valid package name inside of our FRC code, go through all the
            // fields in the associated class.
            for (Field field : rootClass.getDeclaredFields()) {

                // As we recurse, keep track of the full-name for the object as a "."-separated
                // path of sorts.
                String newName = prefix + (prefix.length() > 0 ? "." : "") + field.getName();

                if (field.isAnnotationPresent(frc.lib.Signal.Annotations.Signal.class)) {
                    // Case #1 - we found a @signal annotation - create a new AUtoDiscoveredSignal
                    frc.lib.Signal.Annotations.Signal ann = field
                            .getAnnotation(frc.lib.Signal.Annotations.Signal.class);

                    String nameToUse = newName;
                    if(ann.name().length() > 0){
                        nameToUse = ann.name();
                    }

                    autoSig.add(new AutoDiscoveredSignal(field, root, nameToUse, ann.units()));

                } else {
                    // No signal annotation - we should see if we can recurs on the object
                    // associated with this field
                    // First attempt to get the object and make it accessable.
                    Object childObj = null;
                    try {
                        field.setAccessible(true);
                        childObj = field.get(root);
                    } catch (IllegalAccessException e) {
                        // Not 100% sure how this could get thrown. If so, print a warning, but move on
                        // without error.
                        System.out.println("WARNING: skipping " + field.getName());
                        System.out.println(e);
                    }

                    if (childObj != null && !checkedObjects.contains(childObj)) {
                        checkedObjects.add(childObj);
                        findAllAnnotatedSignals(childObj, newName);
                    } // else, we either couldn't get a reference to the object, or we already checked
                      // it - stop recursion
                }
            } // End FOR

        } // else, rootPkg wasn't in frc.robot - stop recursion
    }

    /**
     * Special thanks to oblarg and his oblog for help on impelmenting this.
     * Entrypoint to set up required variable sets, and traverse rootContainer and
     * its children to find all @Signal-annottated fields, and add a new signal for each of them.
     * Call this in robotInit(), and after all classes which contain @Signals have been instantiated.
     * @param rootContainer Object to start the traversal on. Usually just "this" for when called in Robot.java. 
     */
    public void registerSignals(Object rootContainer) {

        System.out.println("======================================");
        System.out.println("== Registering Signals...");

        autoSig = new HashSet<>();
        checkedObjects = new HashSet<>();
        findAllAnnotatedSignals(rootContainer, "");
        System.out.println("[Data Server]: Registered " + Integer.toString(autoSig.size()) + " signals from annotations");

        System.out.println("== ... Done!");
        System.out.println("======================================");
    }

    /**
     * Periodic call function to sample a single value from all annotation-created Signals
     * Should be called at the end of each periodic function.
     */
    public void sampleAllSignals(){
        sampleAllSignals(Timer.getFPGATimestamp());
    }

    /**
     * Periodic call function to sample a single value from all annotation-created Signals
     * Should be called at the end of each periodic function.
     */
    public void sampleAllSignals(double sampleTime){
        for(AutoDiscoveredSignal sig : autoSig){
            sig.addSample(sampleTime);
        }
    }

    @Override
    public void onAnnounce(Topic newTopic) {
        // DO Nothing - Signals do not care about new topic announcements
    }

    @Override
    public void onUnannounce(Topic deadTopic) {
        // DO Nothing - Signals do not care about topic unannouncments        
    }

    @Override
    public void onValueUpdate(Topic topic, TimestampedValue newVal){
        // Do Nothing - Signals do care about another entity changing their value.
    }

}
