package frc.sim;

import frc.Constants;
import frc.lib.Signal.Annotations.Signal;
import frc.lib.Util.MapLookup2D;
import frc.sim.physics.Force2d;
import frc.sim.physics.ForceAtPose2d;
import frc.sim.physics.Vector2d;
import frc.wrappers.SimDeviceBanks;
import frc.wrappers.MotorCtrl.Sim.SimSmartMotor;
import frc.wrappers.SwerveAzmthEncoder.SimSwerveAzmthEncoder;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.util.Units;

class SwerveModuleModel{

    SimSmartMotor wheelMotorCtrl;
    SimSmartMotor azmthMotorCtrl;

    SimSwerveAzmthEncoder angleMotorEncoder;

    double curLinearSpeed_mps = 0; //Positive = in curAngle_deg, Negative = opposite of curAngle_deg
    Rotation2d curAzmthAngle = Rotation2d.fromDegrees(0); //0 = toward front, 90 = toward left, 180 = toward back, 270 = toward right

    MotorGearboxWheelSim wheelMotor;
    SimpleMotorWithMassModel azmthMotor;

    final double MODULE_NORMAL_FORCE_N = Constants.ROBOT_MASS_kg * 9.81 / Constants.NUM_MODULES; //Assume weight evenly distributed between all modules.

    // Static friction model
    final double WHEEL_TREAD_STATIC_COEF_FRIC = 1.0; 
    final double WHEEL_MAX_STATIC_FRC_FORCE_N = MODULE_NORMAL_FORCE_N*WHEEL_TREAD_STATIC_COEF_FRIC;

    // Non-linear kinetic friction model
    final double WHEEL_TREAD_KINETIC_COEF_FRIC = 0.75;
    final double WHEEL_KINETIC_FRIC_FORCE_N = MODULE_NORMAL_FORCE_N*WHEEL_TREAD_KINETIC_COEF_FRIC;
    MapLookup2D kineticFrictionScaleFactor = new MapLookup2D(); 


    final double WHEEL_GEAR_RATIO = 6.1;
    final double AZMTH_GEAR_RATIO = 150.0;

    final double AZMTH_EFFECTIVE_MOI = 0.01;

    Pose2d prevModulePose = null;
    Pose2d curModulePose  = null;

    @Signal(units = "N")
    double crossTreadFricForceMag = 0;
    @Signal(units = "mps")
    double crossTreadVelMag = 0;
    @Signal(units = "N")
    double crossTreadForceMag = 0;


    public SwerveModuleModel(int wheelMotorIdx, int azmthMotorIdx, int azmthEncIdx){
        wheelMotorCtrl = (SimSmartMotor) SimDeviceBanks.getCANDevice(wheelMotorIdx);
        azmthMotorCtrl = (SimSmartMotor) SimDeviceBanks.getCANDevice(azmthMotorIdx);

        wheelMotor = new MotorGearboxWheelSim(DCMotor.getNEO(1), WHEEL_GEAR_RATIO, Units.inchesToMeters(Constants.WHEEL_RADIUS_IN * 2), 0.01);
        azmthMotor = new SimpleMotorWithMassModel(DCMotor.getVex775Pro(1), AZMTH_GEAR_RATIO, AZMTH_EFFECTIVE_MOI);

        angleMotorEncoder = (SimSwerveAzmthEncoder) SimDeviceBanks.getDIDevice(azmthEncIdx);

        // Non-linear kinetic friction model. Helps kinda simulate how
        // carpet pile and rubber on tread can slip past each other a bit
        // but grabs and stick eventually. Also helps keep friction force smoother
        // and less discontinuous.
        kineticFrictionScaleFactor.insertNewPoint(0.0,  0.0);
        kineticFrictionScaleFactor.insertNewPoint(0.002, 0.1);
        kineticFrictionScaleFactor.insertNewPoint(0.006, 0.5);
        kineticFrictionScaleFactor.insertNewPoint(0.014, 1.0);
        kineticFrictionScaleFactor.insertNewPoint(20.0, 1.0);
    }

    public void reset(Pose2d initModulePose){
        prevModulePose = curModulePose = initModulePose;
        curLinearSpeed_mps = 0;
        curAzmthAngle = Rotation2d.fromDegrees(0);
    }

    
    public void update(boolean isDisabled, double batteryVoltage){

        wheelMotorCtrl.sim_setSupplyVoltage(batteryVoltage);
        azmthMotorCtrl.sim_setSupplyVoltage(batteryVoltage);


        double wheelVoltage = 0;
        double azmthVoltage = 0;

        if(!isDisabled){
            wheelVoltage = wheelMotorCtrl.getAppliedVoltage_V();
            azmthVoltage = azmthMotorCtrl.getAppliedVoltage_V();
        }

        motionModel(wheelVoltage, azmthVoltage, batteryVoltage); 

        angleMotorEncoder.setRawAngle(Units.rotationsToRadians(azmthMotor.getAzmthShaftPosition_Rev()));

        wheelMotorCtrl.sim_setActualPosition(Units.rotationsToRadians(wheelMotor.getMotorPosition_Rev()));
        azmthMotorCtrl.sim_setActualPosition(Units.rotationsToRadians(azmthMotor.getMotorPosition_Rev()));

    }

    /** Implements the main motion model for the module */
    private void motionModel(double wheelVoltage, double azmthVoltage, double batteryVoltage_v){

        Vector2d azimuthUnitVec = new Vector2d(1,0);
        azimuthUnitVec.rotate(curAzmthAngle.getDegrees());

        // Assume the wheel does not lose traction along its wheel direction (on-tread)
        double velocityAlongAzimuth = getModuleRelativeTranslationVelocity().dot(azimuthUnitVec);

        wheelMotor.update(velocityAlongAzimuth, wheelVoltage);
        azmthMotor.update(azmthVoltage);

        // Assume idealized azimuth control - no "twist" force at contact patch from friction or robot motion.
        curAzmthAngle = Rotation2d.fromDegrees(azmthMotor.getAzmthShaftPosition_Rev() * 360);
    }

    /** Get total current draw for the module */
    public double getCurrentDraw_A(){
        return wheelMotor.getCurrent_A() + azmthMotor.getCurrent_A();
    }

    /** Get current azimuth mechanism angle relative to module housing */
    public Rotation2d getCurAzmthAngle(){
        return curAzmthAngle;
    }
    
    /** Gets the modules on-axis (along wheel direction) force, which comes from the rotation of the motor. */
    public ForceAtPose2d getWheelMotiveForce(){
        return new ForceAtPose2d(new Force2d(wheelMotor.getGroundForce_N(), curAzmthAngle), curModulePose);
    }

    /** Get a vector of the velocity of the module's contact patch moving across the field. */
    public Vector2d getModuleRelativeTranslationVelocity(){
        double xVel = (curModulePose.getTranslation().getX() - prevModulePose.getTranslation().getX())/Constants.SIM_SAMPLE_RATE_SEC;
        double yVel = (curModulePose.getTranslation().getY() - prevModulePose.getTranslation().getY())/Constants.SIM_SAMPLE_RATE_SEC;
        Vector2d moduleTranslationVec= new Vector2d(xVel,yVel);
        moduleTranslationVec.rotate(-1.0*curModulePose.getRotation().getDegrees());
        return moduleTranslationVec;

    }

    /**
     * Given a net force on a particular module, calculate the friction force
     * generated by the tread interacting with the ground in the direction
     * perpendicular to the wheel's rotation.
     * @param netForce_in
     * @return 
     */
    public ForceAtPose2d getCrossTreadFrictionalForce(Force2d netForce_in){

        //Project net force onto cross-tread vector
        Vector2d crossTreadUnitVector = new Vector2d(0,1);
        crossTreadUnitVector.rotate(curAzmthAngle.getDegrees());
        crossTreadVelMag = getModuleRelativeTranslationVelocity().dot(crossTreadUnitVector);
        crossTreadForceMag = netForce_in.vec.dot(crossTreadUnitVector);

        Force2d fricForce = new Force2d();
        
        if(Math.abs(crossTreadForceMag) > WHEEL_MAX_STATIC_FRC_FORCE_N || Math.abs(crossTreadVelMag) > 0.001){
            // Force is great enough to overcome static friction, or we're already moving
            // In either case, use kinetic frictional model
            crossTreadFricForceMag = -1.0 * Math.signum(crossTreadVelMag) * WHEEL_KINETIC_FRIC_FORCE_N;
            crossTreadFricForceMag *= kineticFrictionScaleFactor.lookupVal(Math.abs(crossTreadVelMag));
        } else {
            // Static Friction Model
            crossTreadFricForceMag = -1.0 * crossTreadForceMag;
        }
        
        fricForce.vec = crossTreadUnitVector;
        fricForce = fricForce.times(crossTreadFricForceMag);

        return new ForceAtPose2d(fricForce, curModulePose);
    }

    /** Set the motion of each module in the field reference frame */
    public void setModulePose(Pose2d curPos){
        //Handle init'ing module position history to current on first pass
        if(prevModulePose == null){
            prevModulePose = curPos;
        } else {
            prevModulePose = curModulePose;
        }

        curModulePose = curPos;
    }

    public Pose2d getModulePose(){
        return curModulePose;
    }

}