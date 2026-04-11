package com.marslib.swerve;

import static edu.wpi.first.units.Units.Kilograms;
import static edu.wpi.first.units.Units.Meters;
import static edu.wpi.first.units.Units.Volts;

import com.marslib.power.MARSPowerManager;
import com.marslib.util.OnlineFeedforwardEstimator;
import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.config.PIDConstants;
import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.controllers.PPHolonomicDriveController;
import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import frc.robot.SwerveConstants;
import frc.robot.constants.ModeConstants;
import org.ironmaple.simulation.drivesims.COTS;
import org.ironmaple.simulation.drivesims.SwerveDriveSimulation;
import org.ironmaple.simulation.drivesims.configs.DriveTrainSimulationConfig;
import org.littletonrobotics.junction.Logger;

/**
 * The core MARSLib SwerveDrive subsystem responsible for managing holonomic kinematics,
 * high-frequency odometry, dynamic PathPlanner autonomous trajectories, and active power shedding.
 *
 * <p><b>Architecture</b>
 *
 * <ul>
 *   <li><b>Odometry:</b> Powered by a background {@code PhoenixOdometryThread} that drains TalonFX
 *       Signals at 250Hz natively without waiting for the primary 50Hz RoboRIO loop. This provides
 *       unprecedented PathPlanner pathing determinism by capturing inter-tick jitter.
 *   <li><b>Physics-Linked Power Shedding:</b> Native feedback loops constantly poll the {@link
 *       MARSPowerManager}. If the RoboRIO battery hits 7.0V boundaries, Stator current limits are
 *       dynamically clamped via CANbus updates to aggressively protect from robot Brownouts during
 *       pushing matches.
 * </ul>
 *
 * <p>Students: This subsystem hooks directly into AdvantageKit. All structural data is natively
 * logged to the network through the periodic loop. The Swerve modules themselves process inputs
 * through their respective hardware IO interfaces (e.g. TalonFX layers).
 */
public class SwerveDrive extends SubsystemBase {
  private final SwerveModule[] modules;
  private final GyroIO gyroIO;
  private final GyroIOInputsAutoLogged gyroInputs = new GyroIOInputsAutoLogged();
  private final GyroIOSim gyroIOSim; // Null if not in sim mode
  private final SwerveDriveKinematics kinematics;
  private final SwerveDrivePoseEstimator poseEstimator;
  private final MARSPowerManager powerManager;
  private final SysIdRoutine sysIdRoutine;
  private final OnlineFeedforwardEstimator driveFeedforwardEstimator;

  private double lastDriveVelocityForSysId = 0.0;

  private final SwerveDriveSimulation simDrive;
  private final com.marslib.simulation.LidarIOSim lidarSim;

  private final SwerveSetpointGenerator setpointGenerator;
  private SwerveSetpointGenerator.SwerveSetpoint prevSetpoint;
  private final SwerveSetpointGenerator.KinematicLimits kinematicLimits;

  /**
   * Constructs a new SwerveDrive instance.
   *
   * @param modules Array of 4 SwerveModules (FL, FR, BL, BR) wrapping hardware/simulation logic.
   * @param gyroIO The gyro IO layer (Pigeon2 or Sim) providing heading data.
   * @param powerManager The active MARSPowerManager for querying bus voltages for load shedding.
   */
  @SuppressWarnings("PMD.NullAssignment")
  public SwerveDrive(SwerveModule[] modules, GyroIO gyroIO, MARSPowerManager powerManager) {
    this.modules = java.util.Arrays.copyOf(modules, modules.length);
    this.gyroIO = gyroIO;
    this.powerManager = powerManager;

    // Store sim reference for kinematics-derived yaw updates
    this.gyroIOSim = (gyroIO instanceof GyroIOSim) ? (GyroIOSim) gyroIO : null;

    this.kinematics = new SwerveDriveKinematics(SwerveConstants.MODULE_LOCATIONS);

    SwerveModulePosition[] initialPositions =
        new SwerveModulePosition[] {
          modules[0].getLatestPosition(), modules[1].getLatestPosition(),
          modules[2].getLatestPosition(), modules[3].getLatestPosition()
        };

    this.poseEstimator =
        new SwerveDrivePoseEstimator(kinematics, new Rotation2d(), initialPositions, new Pose2d());

    if (frc.robot.Robot.isSimulation()) {
      DriveTrainSimulationConfig driveSimConfig =
          DriveTrainSimulationConfig.Default()
              .withRobotMass(Kilograms.of(SwerveConstants.ROBOT_MASS_KG))
              .withBumperSize(
                  Meters.of(SwerveConstants.BUMPER_LENGTH_METERS),
                  Meters.of(SwerveConstants.BUMPER_WIDTH_METERS))
              .withTrackLengthTrackWidth(
                  Meters.of(SwerveConstants.WHEELBASE_METERS),
                  Meters.of(SwerveConstants.TRACK_WIDTH_METERS))
              .withSwerveModule(
                  COTS.ofMark4(
                      edu.wpi.first.math.system.plant.DCMotor.getKrakenX60Foc(1),
                      edu.wpi.first.math.system.plant.DCMotor.getKrakenX60Foc(1),
                      SwerveConstants.WHEEL_COF_STATIC,
                      2));

      simDrive = new SwerveDriveSimulation(driveSimConfig, poseEstimator.getEstimatedPosition());
      com.marslib.simulation.MARSPhysicsWorld.getInstance()
          .getArena()
          .addDriveTrainSimulation(simDrive);

      lidarSim = new com.marslib.simulation.LidarIOSim();

      // Inject the individual module simulations into the IO layers.
      for (int i = 0; i < modules.length; i++) {
        modules[i].injectModuleSimulation(simDrive.getModules()[i]);
      }

      if (gyroIOSim != null) {
        gyroIOSim.setGyroSimulation(simDrive.getGyroSimulation());
        gyroIOSim.setSwerveDriveSimulation(simDrive);
      }
    } else {
      simDrive = null;
      lidarSim = null;
    }

    // Native SysId Configuration hooked dynamically into this layer
    this.sysIdRoutine =
        new SysIdRoutine(
            new SysIdRoutine.Config(
                null,
                null,
                null,
                (state) -> Logger.recordOutput("SysIdTestState", state.toString())),
            new SysIdRoutine.Mechanism(
                (edu.wpi.first.units.measure.Voltage volts) -> {
                  for (SwerveModule mod : modules) {
                    mod.setDriveVoltage(volts.in(Volts));
                    mod.setTurnVoltage(0.0);
                  }
                },
                null, // Log is handled implicitly via AdvantageKit's @AutoLog IO capturing the
                // voltages & velocities natively
                this));

    this.driveFeedforwardEstimator = new OnlineFeedforwardEstimator("SwerveDrive", 500, 0.0);

    this.setpointGenerator = new SwerveSetpointGenerator(this.kinematics);
    this.kinematicLimits = new SwerveSetpointGenerator.KinematicLimits();
    this.kinematicLimits.maxDriveVelocity = SwerveConstants.MAX_LINEAR_SPEED_MPS;
    // a = mu * g
    this.kinematicLimits.maxDriveAcceleration = SwerveConstants.WHEEL_COF_STATIC * 9.81;
    this.kinematicLimits.maxSteeringVelocity = SwerveConstants.MAX_ANGULAR_SPEED_RAD_PER_SEC;

    this.prevSetpoint =
        new SwerveSetpointGenerator.SwerveSetpoint(
            new ChassisSpeeds(),
            new SwerveModuleState[] {
              new SwerveModuleState(),
              new SwerveModuleState(),
              new SwerveModuleState(),
              new SwerveModuleState()
            });
  }

  // Reusable GC-free arrays for periodic loop to prevent massive RoboRIO heap churn
  private final SwerveModulePosition[] positionsForFrame =
      new SwerveModulePosition[] {
        new SwerveModulePosition(),
        new SwerveModulePosition(),
        new SwerveModulePosition(),
        new SwerveModulePosition()
      };
  private final SwerveModulePosition[] scaledPositions =
      new SwerveModulePosition[] {
        new SwerveModulePosition(),
        new SwerveModulePosition(),
        new SwerveModulePosition(),
        new SwerveModulePosition()
      };
  private final double[] lastRawDistances = new double[4];
  private boolean isFirstOdometryDrain = true;
  private final Rotation2d[] frameYawCache = new Rotation2d[] {new Rotation2d()};
  private final SwerveModuleState[] measuredStatesCache = new SwerveModuleState[4];

  /**
   * Configures PathPlanner's AutoBuilder for autonomous path following. Must be called exactly once
   * by {@code RobotContainer} after construction.
   *
   * <p>This is intentionally separated from the constructor to decouple the drivetrain from
   * PathPlanner, allowing unit tests to construct SwerveDrive without triggering AutoBuilder's
   * static singleton.
   */
  public void configurePathPlanner() {
    try {
      edu.wpi.first.math.system.plant.DCMotor gearbox =
          edu.wpi.first.math.system.plant.DCMotor.getKrakenX60(1)
              .withReduction(SwerveConstants.DRIVE_GEAR_RATIO);

      com.pathplanner.lib.config.ModuleConfig moduleConfig =
          new com.pathplanner.lib.config.ModuleConfig(
              SwerveConstants.WHEEL_RADIUS_METERS,
              SwerveConstants.MAX_LINEAR_SPEED_MPS,
              SwerveConstants.WHEEL_COF_STATIC,
              gearbox,
              SwerveConstants.DRIVE_STATOR_CURRENT_LIMIT,
              1);

      RobotConfig config =
          new RobotConfig(
              SwerveConstants.ROBOT_MASS_KG,
              SwerveConstants.ROBOT_MOI_KG_M2,
              moduleConfig,
              SwerveConstants.MODULE_LOCATIONS);

      AutoBuilder.configure(
          this::getPose,
          this::resetPose,
          this::getChassisSpeeds,
          (speeds, feedforwards) -> runVelocity(speeds),
          new PPHolonomicDriveController(
              new PIDConstants(
                  SwerveConstants.AUTO_TRANSLATION_KP, 0.0, SwerveConstants.AUTO_TRANSLATION_KD),
              new PIDConstants(
                  SwerveConstants.AUTO_ROTATION_KP, 0.0, SwerveConstants.AUTO_ROTATION_KD)),
          config,
          () -> false, // Mirroring
          this // Subsystem requirement
          );
    } catch (Exception e) {
      edu.wpi.first.wpilibj.DriverStation.reportError(
          "Failed to configure AutoBuilder", e.getStackTrace());
      throw new RuntimeException("Failed to configure AutoBuilder", e);
    }
  }

  @Override
  public void periodic() {
    // Update gyro inputs
    gyroIO.updateInputs(gyroInputs);
    Logger.processInputs("SwerveDrive/Gyro", gyroInputs);

    for (SwerveModule module : modules) {
      module.periodic();
    }

    // Active Load Shedding is no longer executed over CAN; it's handled via static HW supply limits
    // and scaled chassis velocity vectors natively.
    // Use real gyro yaw for pose estimation
    // Rotation2d yaw = ... (moved into the drain loop for high frequency accuracy)

    // Calculate tilt-based trust multiplier for odometry (filters out wheel slip when airborne on
    // bumps)
    double odometryTrust = 1.0;
    if (gyroInputs.connected) {
      double tiltRadians =
          Math.acos(Math.cos(gyroInputs.pitchPositionRad) * Math.cos(gyroInputs.rollPositionRad));
      odometryTrust =
          1.0
              - edu.wpi.first.math.MathUtil.inverseInterpolate(
                  0, Math.toRadians(25.0), Math.abs(tiltRadians));
      odometryTrust = edu.wpi.first.math.MathUtil.clamp(odometryTrust, 0.0, 1.0);
    }
    Logger.recordOutput("SwerveDrive/OdometryTrustMultiplier", odometryTrust);

    // Synchronous Pose Estimator Drain
    int sampleCount = modules[0].getDeltaCount();
    double[] timestamps = modules[0].getOdometryTimestamps();

    for (int i = 0; i < sampleCount; i++) {
      for (int m = 0; m < 4; m++) {
        SwerveModulePosition rawPos = modules[m].getCachedDelta(i);

        if (isFirstOdometryDrain) {
          lastRawDistances[m] = rawPos.distanceMeters;
          scaledPositions[m].distanceMeters = rawPos.distanceMeters;
        }

        double delta = rawPos.distanceMeters - lastRawDistances[m];
        lastRawDistances[m] = rawPos.distanceMeters;

        scaledPositions[m].distanceMeters += delta * odometryTrust;
        scaledPositions[m].angle = rawPos.angle;

        // GC-free: mutate pre-allocated position objects instead of new-ing
        positionsForFrame[m].distanceMeters = scaledPositions[m].distanceMeters;
        positionsForFrame[m].angle = scaledPositions[m].angle;
      }
      isFirstOdometryDrain = false;

      // GC-free: reuse single Rotation2d via static factory (returns cached instances for common
      // values)
      double frameYawRad;
      if (gyroInputs.connected && gyroInputs.odometryYawPositions.length > 0) {
        frameYawRad =
            gyroInputs
                .odometryYawPositions[Math.min(i, gyroInputs.odometryYawPositions.length - 1)];
      } else {
        frameYawRad = gyroInputs.yawPositionRad;
      }
      frameYawCache[0] = Rotation2d.fromRadians(frameYawRad);

      double timestamp =
          (timestamps.length > i) ? timestamps[i] : edu.wpi.first.wpilibj.Timer.getFPGATimestamp();

      poseEstimator.updateWithTime(timestamp, frameYawCache[0], positionsForFrame);
    }

    // Log final Pose
    Pose2d currentPose = poseEstimator.getEstimatedPosition();

    // Update our LiDAR point cloud based on true simulation bounding frames
    if (simDrive != null) {
      Pose2d simBoundedPose = simDrive.getSimulatedDriveTrainPose();
      Logger.recordOutput("DriveTrain/SimPose", simBoundedPose);

      if (lidarSim != null) {
        lidarSim.updateInputs(simBoundedPose);
      }
    }

    Logger.recordOutput("SwerveDrive/Pose", currentPose);
    Logger.recordOutput("Odometry/RobotPose", currentPose);
    Logger.recordOutput("Robot/Pose", currentPose);
    Logger.recordOutput(
        "Robot/Pose3d", new Pose3d(currentPose.getX(), currentPose.getY(), 0.0, new Rotation3d()));

    // GC-free: reuse pre-allocated states array for logging
    measuredStatesCache[0] = modules[0].getLatestState();
    measuredStatesCache[1] = modules[1].getLatestState();
    measuredStatesCache[2] = modules[2].getLatestState();
    measuredStatesCache[3] = modules[3].getLatestState();
    Logger.recordOutput("SwerveDrive/MeasuredStates", measuredStatesCache);

    // Continuous TeleOp SysId Extraction
    double currentVelocity = measuredStatesCache[0].speedMetersPerSecond;
    double currentAccel =
        (currentVelocity - lastDriveVelocityForSysId) / ModeConstants.LOOP_PERIOD_SECS;
    lastDriveVelocityForSysId = currentVelocity;

    // We analyze the front-left module as a representative sample
    driveFeedforwardEstimator.addMeasurement(
        modules[0].getDriveAppliedVoltage(), currentVelocity, currentAccel);
  }

  /**
   * Drives the robot at the given velocity natively.
   *
   * <p>This is commonly triggered dynamically via PathPlanner trajectory followings or Teleop
   * joysticks. Modules are optimized for minimal rotation before applying voltages.
   *
   * @param speeds The requested translational and rotational velocities in m/s and rad/s.
   */
  public void runVelocity(ChassisSpeeds speeds) {
    // Determine dynamic battery stability modifier
    double voltageScale =
        powerManager.calculateVoltageScaleFactor(
            frc.robot.constants.PowerConstants.NOMINAL_VOLTAGE,
            frc.robot.constants.PowerConstants.CRITICAL_VOLTAGE);

    // Scale user requests to avoid pulling massive transients during brownouts
    speeds.vxMetersPerSecond *= voltageScale;
    speeds.vyMetersPerSecond *= voltageScale;
    speeds.omegaRadiansPerSecond *= voltageScale;

    ChassisSpeeds discretizedSpeeds =
        ChassisSpeeds.discretize(speeds, ModeConstants.LOOP_PERIOD_SECS);

    this.prevSetpoint =
        setpointGenerator.generateSetpoint(
            kinematicLimits, prevSetpoint, discretizedSpeeds, ModeConstants.LOOP_PERIOD_SECS);

    SwerveModuleState[] states = prevSetpoint.moduleStates;

    for (int i = 0; i < 4; i++) {
      modules[i].setDesiredState(states[i]);
    }
    Logger.recordOutput("SwerveDrive/DesiredStates", states);
  }

  /**
   * Directly commands the swerve modules to the specified states. Useful for hardware testing and
   * SystemCheck diagnostics.
   *
   * @param states Array of 4 SwerveModuleStates.
   */
  public void setModuleStates(SwerveModuleState... states) {
    SwerveDriveKinematics.desaturateWheelSpeeds(states, SwerveConstants.MAX_LINEAR_SPEED_MPS);
    for (int i = 0; i < 4; i++) {
      modules[i].setDesiredState(states[i]);
    }
    Logger.recordOutput("SwerveDrive/DesiredStates", states);
  }

  /**
   * Generates a SysId Quasistatic characterization command.
   *
   * @param direction The direction of the quasistatic routine (Forward/Reverse).
   * @return The SysId Command to execute.
   */
  public Command sysIdQuasistatic(SysIdRoutine.Direction direction) {
    return sysIdRoutine.quasistatic(direction);
  }

  /**
   * Generates a SysId Dynamic characterization command.
   *
   * @param direction The direction of the dynamic routine (Forward/Reverse).
   * @return The SysId Command to execute.
   */
  public Command sysIdDynamic(SysIdRoutine.Direction direction) {
    return sysIdRoutine.dynamic(direction);
  }

  /**
   * Returns the current highly-filtered pose from the SwerveDrivePoseEstimator.
   *
   * @return The current Pose2d of the robot on the field in meters.
   */
  public Pose2d getPose() {
    return poseEstimator.getEstimatedPosition();
  }

  /**
   * Resets the absolute pose of the robot. Commonly called at the start of autonomous routines.
   *
   * @param pose The new Pose2d coordinate mapped in standard WPILib field units.
   */
  public void resetPose(Pose2d pose) {
    if (simDrive != null) {
      simDrive.setSimulationWorldPose(pose);
    }

    for (int i = 0; i < 4; i++) {
      SwerveModulePosition raw = modules[i].getLatestPosition();
      scaledPositions[i].distanceMeters = raw.distanceMeters;
      scaledPositions[i].angle = raw.angle;
      lastRawDistances[i] = raw.distanceMeters;
    }
    isFirstOdometryDrain = false;

    // Reuse pre-allocated array to keep consistent with GC-free patterns
    for (int i = 0; i < 4; i++) {
      positionsForFrame[i].distanceMeters = scaledPositions[i].distanceMeters;
      positionsForFrame[i].angle = scaledPositions[i].angle;
    }

    poseEstimator.resetPosition(
        gyroInputs.connected
            ? Rotation2d.fromRadians(gyroInputs.yawPositionRad)
            : pose.getRotation(),
        positionsForFrame,
        pose);
  }

  /**
   * Measures the current overall chassis speeds based on the actual measured module states.
   *
   * @return Measured ChassisSpeeds in (m/s) and (rad/s).
   */
  public ChassisSpeeds getChassisSpeeds() {
    return kinematics.toChassisSpeeds(
        modules[0].getLatestState(), modules[1].getLatestState(),
        modules[2].getLatestState(), modules[3].getLatestState());
  }

  /**
   * Directly injects vision/SLAM data into the PoseEstimator pipeline. This is generally managed
   * natively by the MARSVision layer filtering logic.
   *
   * @param visionRobotPoseMeters The calculated field pose from the vision target (meters).
   * @param timestampSeconds The precise FPGA timestamp the image frame was captured (seconds).
   * @param visionMeasurementStdDevs The confidence matrix assigned to this measurement.
   */
  public void addVisionMeasurement(
      Pose2d visionRobotPoseMeters,
      double timestampSeconds,
      Matrix<N3, N1> visionMeasurementStdDevs) {

    poseEstimator.addVisionMeasurement(
        visionRobotPoseMeters, timestampSeconds, visionMeasurementStdDevs);
  }

  /** Gets the current hardware or simulated Gyro inputs. */
  public GyroIOInputsAutoLogged getGyroInputs() {
    return gyroInputs;
  }

  /**
   * Generates a command that automatically paths the robot to the specified target utilizing
   * PathPlanner's physics-constrained solver to avoid field obstacles dynamically.
   *
   * @param target A supplier of the absolute field pose to navigate to.
   * @return A Command orchestrating the autonomous drive.
   */
  public Command alignToPoint(java.util.function.Supplier<Pose2d> target) {
    return edu.wpi.first.wpilibj2.command.Commands.defer(
        () -> {
          return AutoBuilder.pathfindToPose(
              target.get(),
              new com.pathplanner.lib.path.PathConstraints(
                  SwerveConstants.MAX_LINEAR_SPEED_MPS,
                  SwerveConstants.MAX_LINEAR_SPEED_MPS,
                  SwerveConstants.MAX_ANGULAR_SPEED_RAD_PER_SEC,
                  SwerveConstants.MAX_ANGULAR_SPEED_RAD_PER_SEC),
              0.0 // goal end velocity
              );
        },
        java.util.Set.of(this));
  }

  /**
   * Open-loop dead-reckoning sequence designed to precisely lock the robot onto the climb chain
   * using predefined translational bump limits.
   *
   * @return A Command sequencing forced chassis movement.
   */
  public Command finalClimbLineupCommand() {
    return edu.wpi.first.wpilibj2.command.Commands.sequence(
            edu.wpi.first.wpilibj2.command.Commands.run(
                    () -> runVelocity(new ChassisSpeeds(0.0, -0.5, 0.0)), this)
                .withTimeout(0.5),
            edu.wpi.first.wpilibj2.command.Commands.run(
                    () -> runVelocity(new ChassisSpeeds(0.5, 0.0, 0.0)), this)
                .withTimeout(0.5))
        .finallyDo(() -> runVelocity(new ChassisSpeeds()));
  }
}
