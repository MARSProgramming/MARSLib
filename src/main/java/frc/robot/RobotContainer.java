package frc.robot;

import com.marslib.hmi.LEDManager;
import com.marslib.mechanisms.FlywheelIO;
import com.marslib.mechanisms.FlywheelIOSim;
import com.marslib.mechanisms.FlywheelIOTalonFX;
import com.marslib.mechanisms.LinearMechanismIO;
import com.marslib.mechanisms.LinearMechanismIOSim;
import com.marslib.mechanisms.LinearMechanismIOTalonFX;
import com.marslib.mechanisms.RotaryMechanismIO;
import com.marslib.mechanisms.RotaryMechanismIOSim;
import com.marslib.mechanisms.RotaryMechanismIOTalonFX;
import com.marslib.power.MARSPowerManager;
import com.marslib.power.PowerConfig;
import com.marslib.power.PowerIOReal;
import com.marslib.power.PowerIOSim;
import com.marslib.swerve.GyroIO;
import com.marslib.swerve.GyroIOPigeon2;
import com.marslib.swerve.GyroIOSim;
import com.marslib.swerve.SwerveConfig;
import com.marslib.swerve.SwerveDrive;
import com.marslib.swerve.SwerveModule;
import com.marslib.swerve.SwerveModuleIO;
import com.marslib.swerve.SwerveModuleIOSim;
import com.marslib.swerve.SwerveModuleIOTalonFX;
import com.marslib.vision.AprilTagVisionIOLimelight;
import com.marslib.vision.AprilTagVisionIOSim;
import com.marslib.vision.MARSVision;
import com.marslib.vision.VisionConfig;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.constants.AutoConstants;
import frc.robot.constants.ClimberConstants;
import frc.robot.constants.CowlConstants;
import frc.robot.constants.DriveConstants;
import frc.robot.constants.FieldConstants;
import frc.robot.constants.IntakeConstants;
import frc.robot.constants.LEDConstants;
import frc.robot.constants.ModeConstants;
import frc.robot.constants.PowerConstants;
import frc.robot.constants.ShooterConstants;
import frc.robot.constants.VisionConstants;
import frc.robot.subsystems.MARSClimber;
import frc.robot.subsystems.MARSCowl;
import frc.robot.subsystems.MARSIntakePivot;
import frc.robot.subsystems.MARSShooter;
import frc.robot.subsystems.MARSSuperstructure;
import frc.robot.subsystems.OperatorInterface;
import org.littletonrobotics.junction.networktables.LoggedDashboardChooser;

/**
 * Central Dependency Injection container for the robot. All subsystem instantiation and mode-based
 * hardware selection (REAL vs SIM vs REPLAY) happens here.
 *
 * <p>Students: This is the only file you should need to modify when changing CAN IDs, adding new
 * subsystems, or switching between hardware and simulation modes.
 */
public class RobotContainer {
  private final MARSPowerManager powerManager;
  private final OperatorInterface operatorInterface;

  private final SwerveDrive swerveDrive;
  private final MARSClimber climber;
  private final MARSCowl cowl;
  private final MARSIntakePivot intakePivot;
  private final MARSShooter floorIntake;
  private final MARSShooter shooter;
  private final MARSShooter feeder;
  private final MARSSuperstructure superstructure;

  @SuppressWarnings({"PMD.UnusedPrivateField", "unused"})
  private final LEDManager ledManager;

  private final MARSVision vision;

  private final LoggedDashboardChooser<Command> autoChooser;

  public RobotContainer() {
    this(true);
  }

  public RobotContainer(boolean buildDashboards) {
    // 0. Initialize Library Configurations from App Constants
    final SwerveConfig swerveConfig =
        new SwerveConfig(
            SwerveConstants.MODULE_LOCATIONS,
            SwerveConstants.MAX_LINEAR_SPEED_MPS,
            SwerveConstants.MAX_ANGULAR_SPEED_RAD_PER_SEC,
            SwerveConstants.WHEEL_RADIUS_METERS,
            SwerveConstants.TURN_KP,
            PowerConstants.NOMINAL_VOLTAGE,
            PowerConstants.WARNING_VOLTAGE,
            PowerConstants.CRITICAL_VOLTAGE,
            SwerveConstants.DRIVE_GEAR_RATIO,
            SwerveConstants.DRIVE_STATOR_CURRENT_LIMIT,
            SwerveConstants.ROBOT_MASS_KG,
            SwerveConstants.ROBOT_MOI_KG_M2,
            SwerveConstants.BUMPER_LENGTH_METERS,
            SwerveConstants.BUMPER_WIDTH_METERS,
            SwerveConstants.WHEELBASE_METERS,
            SwerveConstants.TRACK_WIDTH_METERS,
            SwerveConstants.WHEEL_COF_STATIC,
            ModeConstants.LOOP_PERIOD_SECS,
            DriveConstants.TELEOP_LINEAR_ACCEL_LIMIT,
            DriveConstants.TELEOP_OMEGA_ACCEL_LIMIT,
            DriveConstants.HEADING_KP,
            SwerveConstants.AUTO_TRANSLATION_KP,
            SwerveConstants.AUTO_TRANSLATION_KD,
            SwerveConstants.AUTO_ROTATION_KP,
            SwerveConstants.AUTO_ROTATION_KD,
            AutoConstants.ALIGN_TRANSLATION_KP,
            AutoConstants.ALIGN_TRANSLATION_IZONE_METERS,
            AutoConstants.ALIGN_THETA_KP,
            AutoConstants.ALIGN_THETA_IZONE_RAD,
            DriveConstants.TELEMETRY_HZ,
            DriveConstants.ODOMETRY_HZ,
            SwerveConstants.TURN_GEAR_RATIO,
            SwerveConstants.TURN_STATOR_CURRENT_LIMIT);

    final VisionConfig visionConfig =
        new VisionConfig(
            VisionConstants.MAX_Z_HEIGHT::get,
            FieldConstants.FIELD_LENGTH_METERS,
            FieldConstants.FIELD_WIDTH_METERS,
            VisionConstants.FIELD_MARGIN_METERS::get,
            VisionConstants.MAX_TILT_DEG::get,
            VisionConstants.MAX_ANGULAR_ACCEL_DEG_PER_SEC2::get,
            VisionConstants.MAX_AMBIGUITY::get,
            VisionConstants.TAG_STD_BASE::get,
            VisionConstants.MULTI_TAG_STD_MULTIPLIER::get,
            VisionConstants.ANGULAR_STD_MULTIPLIER::get,
            VisionConstants.LINEAR_VELOCITY_STD_MULTIPLIER::get,
            VisionConstants.ANGULAR_VELOCITY_STD_MULTIPLIER::get,
            VisionConstants.SLAM_STD_DEV::get,
            VisionConstants.SLAM_ANGULAR_STD_DEV::get,
            ModeConstants.LOOP_PERIOD_SECS);

    final PowerConfig powerConfig =
        new PowerConfig(
            PowerConstants.NOMINAL_VOLTAGE,
            PowerConstants.WARNING_VOLTAGE,
            PowerConstants.CRITICAL_VOLTAGE);

    // 1. Dependency Injection based on Current Mode
    switch (ModeConstants.CURRENT_MODE) {
      case SIM:
        {
          powerManager = new MARSPowerManager(new PowerIOSim(powerConfig), powerConfig);
          GyroIOSim gyroSim = new GyroIOSim();

          swerveDrive =
              new SwerveDrive(
                  new SwerveModule[] {
                    new SwerveModule(0, new SwerveModuleIOSim(0), swerveConfig),
                    new SwerveModule(1, new SwerveModuleIOSim(1), swerveConfig),
                    new SwerveModule(2, new SwerveModuleIOSim(2), swerveConfig),
                    new SwerveModule(3, new SwerveModuleIOSim(3), swerveConfig)
                  },
                  gyroSim,
                  powerManager,
                  swerveConfig);

          climber =
              new MARSClimber(
                  new LinearMechanismIOSim("Climber", ClimberConstants.FAST_GEAR_RATIO, 0.05, 5.0),
                  powerManager);

          cowl =
              new MARSCowl(
                  new RotaryMechanismIOSim("Cowl", CowlConstants.GEAR_RATIO, 0.5, 0.5),
                  powerManager);
          intakePivot =
              new MARSIntakePivot(
                  new RotaryMechanismIOSim(
                      "IntakePivot", IntakeConstants.PIVOT_GEAR_RATIO, 0.5, 0.5),
                  powerManager);

          floorIntake =
              new MARSShooter(
                  "FloorIntake",
                  new FlywheelIOSim(
                      edu.wpi.first.math.system.plant.DCMotor.getFalcon500(1), 1.0, 0.025),
                  powerManager);
          shooter =
              new MARSShooter(
                  "Shooter",
                  new FlywheelIOSim(
                      edu.wpi.first.math.system.plant.DCMotor.getFalcon500(4), 1.0, 0.05),
                  powerManager);
          feeder =
              new MARSShooter(
                  "Feeder",
                  new FlywheelIOSim(
                      edu.wpi.first.math.system.plant.DCMotor.getFalcon500(1),
                      ShooterConstants.FEEDER_GEAR_RATIO,
                      0.025),
                  powerManager);

          ledManager =
              new LEDManager(
                  new com.marslib.hmi.LEDIOCANdle(
                      LEDConstants.CANDLE_ID, LEDConstants.CANBUS, LEDConstants.LENGTH),
                  powerManager);

          vision =
              new MARSVision(
                  swerveDrive,
                  java.util.List.of(
                      new AprilTagVisionIOSim(
                          "limelight-front",
                          new Transform3d(
                              new Translation3d(0.3, 0.0, 0.5), new Rotation3d(0, 0, 0)),
                          swerveDrive::getSimPose3d),
                      new AprilTagVisionIOSim(
                          "limelight-back",
                          new Transform3d(
                              new Translation3d(-0.3, 0.0, 0.5), new Rotation3d(0, 0, Math.PI)),
                          swerveDrive::getSimPose3d)),
                  java.util.List.of(),
                  visionConfig);
          break;
        }
      case REAL:
        {
          powerManager = new MARSPowerManager(new PowerIOReal(), powerConfig);

          swerveDrive =
              new SwerveDrive(
                  new SwerveModule[] {
                    new SwerveModule(
                        0,
                        new SwerveModuleIOTalonFX(
                            DriveConstants.FL_DRIVE_ID,
                            DriveConstants.FL_TURN_ID,
                            DriveConstants.CANBUS,
                            swerveConfig),
                        swerveConfig),
                    new SwerveModule(
                        1,
                        new SwerveModuleIOTalonFX(
                            DriveConstants.FR_DRIVE_ID,
                            DriveConstants.FR_TURN_ID,
                            DriveConstants.CANBUS,
                            swerveConfig),
                        swerveConfig),
                    new SwerveModule(
                        2,
                        new SwerveModuleIOTalonFX(
                            DriveConstants.BL_DRIVE_ID,
                            DriveConstants.BL_TURN_ID,
                            DriveConstants.CANBUS,
                            swerveConfig),
                        swerveConfig),
                    new SwerveModule(
                        3,
                        new SwerveModuleIOTalonFX(
                            DriveConstants.BR_DRIVE_ID,
                            DriveConstants.BR_TURN_ID,
                            DriveConstants.CANBUS,
                            swerveConfig),
                        swerveConfig)
                  },
                  new GyroIOPigeon2(DriveConstants.PIGEON2_ID, DriveConstants.CANBUS, swerveConfig),
                  powerManager,
                  swerveConfig);

          climber =
              new MARSClimber(
                  new LinearMechanismIOTalonFX(
                      ClimberConstants.FAST_MOTOR_ID,
                      ClimberConstants.CANBUS,
                      ClimberConstants.FAST_GEAR_RATIO,
                      0.05,
                      false),
                  powerManager);

          cowl =
              new MARSCowl(
                  new RotaryMechanismIOTalonFX(
                      CowlConstants.MOTOR_ID,
                      CowlConstants.CANBUS,
                      CowlConstants.GEAR_RATIO,
                      CowlConstants.INVERTED),
                  powerManager);
          intakePivot =
              new MARSIntakePivot(
                  new RotaryMechanismIOTalonFX(
                      IntakeConstants.PIVOT_MOTOR_ID,
                      IntakeConstants.CANBUS,
                      IntakeConstants.PIVOT_GEAR_RATIO,
                      false),
                  powerManager);

          floorIntake =
              new MARSShooter(
                  "FloorIntake",
                  new FlywheelIOTalonFX(
                      IntakeConstants.FLOOR_MOTOR_ID, IntakeConstants.CANBUS, false),
                  powerManager);

          shooter =
              new MARSShooter(
                  "Shooter",
                  new FlywheelIOTalonFX(
                      ShooterConstants.LM_MOTOR_ID,
                      new int[] {
                        ShooterConstants.LF_MOTOR_ID,
                        ShooterConstants.RM_MOTOR_ID,
                        ShooterConstants.RF_MOTOR_ID
                      },
                      new boolean[] {false, false, false},
                      ShooterConstants.CANBUS,
                      false),
                  powerManager);
          feeder =
              new MARSShooter(
                  "Feeder",
                  new FlywheelIOTalonFX(
                      ShooterConstants.FEEDER_MOTOR_ID, ShooterConstants.CANBUS, false),
                  powerManager);

          ledManager =
              new LEDManager(
                  new com.marslib.hmi.LEDIOCANdle(
                      LEDConstants.CANDLE_ID, LEDConstants.CANBUS, LEDConstants.LENGTH),
                  powerManager);

          vision =
              new MARSVision(
                  swerveDrive,
                  java.util.List.of(
                      new AprilTagVisionIOLimelight("limelight-front"),
                      new AprilTagVisionIOLimelight("limelight-back")),
                  java.util.List.of(),
                  visionConfig);
          break;
        }
      case REPLAY:
      default:
        {
          // REPLAY mode: no-op IO implementations to allow
          // deterministic log replay without hardware or physics dependencies.
          powerManager = new MARSPowerManager(new PowerIOSim(powerConfig), powerConfig);

          swerveDrive =
              new SwerveDrive(
                  new SwerveModule[] {
                    new SwerveModule(
                        0,
                        com.marslib.util.ReplayIOFactory.createProxy(SwerveModuleIO.class),
                        swerveConfig),
                    new SwerveModule(
                        1,
                        com.marslib.util.ReplayIOFactory.createProxy(SwerveModuleIO.class),
                        swerveConfig),
                    new SwerveModule(
                        2,
                        com.marslib.util.ReplayIOFactory.createProxy(SwerveModuleIO.class),
                        swerveConfig),
                    new SwerveModule(
                        3,
                        com.marslib.util.ReplayIOFactory.createProxy(SwerveModuleIO.class),
                        swerveConfig)
                  },
                  com.marslib.util.ReplayIOFactory.createProxy(GyroIO.class),
                  powerManager,
                  swerveConfig);

          climber =
              new MARSClimber(
                  com.marslib.util.ReplayIOFactory.createProxy(LinearMechanismIO.class),
                  powerManager);
          cowl =
              new MARSCowl(
                  com.marslib.util.ReplayIOFactory.createProxy(RotaryMechanismIO.class),
                  powerManager);
          intakePivot =
              new MARSIntakePivot(
                  com.marslib.util.ReplayIOFactory.createProxy(RotaryMechanismIO.class),
                  powerManager);
          floorIntake =
              new MARSShooter(
                  "FloorIntake",
                  com.marslib.util.ReplayIOFactory.createProxy(FlywheelIO.class),
                  powerManager);
          shooter =
              new MARSShooter(
                  "Shooter",
                  com.marslib.util.ReplayIOFactory.createProxy(FlywheelIO.class),
                  powerManager);
          feeder =
              new MARSShooter(
                  "Feeder",
                  com.marslib.util.ReplayIOFactory.createProxy(FlywheelIO.class),
                  powerManager);
          ledManager =
              new LEDManager(
                  com.marslib.util.ReplayIOFactory.createProxy(com.marslib.hmi.LEDIO.class),
                  powerManager);

          vision =
              new MARSVision(swerveDrive, java.util.List.of(), java.util.List.of(), visionConfig);
          break;
        }
    }

    operatorInterface = new OperatorInterface(0, powerManager);

    superstructure =
        new MARSSuperstructure(
            cowl,
            intakePivot,
            floorIntake,
            shooter,
            feeder,
            swerveDrive::getPose,
            vision::getBestTargetTranslation,
            () -> {
              com.marslib.swerve.GyroIO.GyroIOInputs gyro = swerveDrive.getGyroInputs();
              return Math.acos(Math.cos(gyro.pitchPositionRad) * Math.cos(gyro.rollPositionRad));
            });

    // Configure PathPlanner AutoBuilder AFTER construction — composition root owns this
    swerveDrive.configurePathPlanner();

    if (ModeConstants.CURRENT_MODE == ModeConstants.Mode.SIM) {
      boolean isRed =
          edu.wpi.first.wpilibj.DriverStation.getAlliance()
                  .orElse(edu.wpi.first.wpilibj.DriverStation.Alliance.Blue)
              == edu.wpi.first.wpilibj.DriverStation.Alliance.Red;
      // Default starting position away from walls to prevent physics collision
      swerveDrive.resetPose(
          new edu.wpi.first.math.geometry.Pose2d(
              isRed ? 14.54 : 2.0, 2.0, edu.wpi.first.math.geometry.Rotation2d.fromDegrees(0)));
    }

    // Initialize the Auto Chooser
    autoChooser =
        new LoggedDashboardChooser<>(
            "Auto Chooser", com.pathplanner.lib.auto.AutoBuilder.buildAutoChooser());

    // Task 1: Asynchronously pre-load all PathPlanner trajectories to prevent match-start CPU
    // stutter
    Thread trajectoryPreloader =
        new Thread(
            () -> {
              for (String autoName : com.pathplanner.lib.auto.AutoBuilder.getAllAutoNames()) {
                try {
                  new com.pathplanner.lib.commands.PathPlannerAuto(autoName);
                } catch (Exception e) {
                  new com.marslib.faults.Alert(
                          "RobotContainer: Failed to cache trajectory: " + autoName,
                          com.marslib.faults.Alert.AlertType.WARNING)
                      .set(true);
                }
              }
            });
    trajectoryPreloader.setDaemon(true);
    trajectoryPreloader.setName("PathPlannerPreloader");
    trajectoryPreloader.start();

    // Expose utility commands directly on SmartDashboard for generic access
    edu.wpi.first.wpilibj.smartdashboard.SmartDashboard.putData(
        "Dump Tunables", com.marslib.util.LoggedTunableNumber.getDumpCommand());
    edu.wpi.first.wpilibj.smartdashboard.SmartDashboard.putData(
        "Offload Logs to USB", com.marslib.util.LogUploader.getUsbOffloadCommand());
    RobotBindings.configureBindings(
        operatorInterface,
        swerveDrive,
        swerveConfig,
        superstructure,
        climber,
        cowl,
        shooter,
        feeder,
        floorIntake);

    if (buildDashboards) {
      configureCompetitionDashboard();
      configurePracticeDashboard();
      com.marslib.util.LoggedTunableNumber.buildTuningDashboard();
    }
  }

  public Command getAutonomousCommand() {
    return autoChooser.get();
  }

  /**
   * Scaffolds an explicitly lightweight WPILib Native Dashboard (Shuffleboard/Glass) specifically
   * designed for FMS-tethered matches where 3D 60FPS renders drop DriveStation CPU bandwidth.
   */
  private void configureCompetitionDashboard() {
    edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab matchTab =
        edu.wpi.first.wpilibj.shuffleboard.Shuffleboard.getTab("Match");

    // 1. Prominent Auto Chooser
    matchTab
        .add("Auto Routine", autoChooser.getSendableChooser())
        .withWidget(edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets.kComboBoxChooser)
        .withSize(3, 1)
        .withPosition(0, 0);

    // 2. Match Info & System Telemetry
    matchTab
        .addString(
            "Match Time",
            () -> {
              int remaining = (int) edu.wpi.first.wpilibj.Timer.getMatchTime();
              return (remaining < 0 || !edu.wpi.first.wpilibj.DriverStation.isFMSAttached())
                  ? "N/A"
                  : remaining + " s";
            })
        .withSize(2, 2)
        .withPosition(0, 1);

    matchTab
        .addString(
            "FMS Alliance",
            () ->
                edu.wpi.first.wpilibj.DriverStation.getAlliance()
                    .map(edu.wpi.first.wpilibj.DriverStation.Alliance::toString)
                    .orElse("UNCALIBRATED"))
        .withSize(2, 1)
        .withPosition(3, 0);

    matchTab
        .addBoolean(
            "Gyro Connected",
            () -> {
              com.marslib.swerve.GyroIOInputsAutoLogged gyro = swerveDrive.getGyroInputs();
              return gyro.connected;
            })
        .withWidget(edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets.kBooleanBox)
        .withSize(1, 1)
        .withPosition(3, 1);

    // 3. Superstructure Faults or Status
    matchTab
        .addString(
            "Superstructure State",
            () -> superstructure != null ? superstructure.getCurrentState().toString() : "BOOTING")
        .withSize(3, 1)
        .withPosition(5, 0);

    // 4. Utility Actions
    matchTab
        .add("Emergency USB Offload", com.marslib.util.LogUploader.getUsbOffloadCommand())
        .withSize(2, 1)
        .withPosition(5, 1);
  }

  /**
   * Extends the UI for practice matches and un-tethered development where manual sequence
   * triggering, module zeroing, and detailed odometry overrides sit alongside the generic Match
   * widgets.
   */
  private void configurePracticeDashboard() {
    edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab practiceTab =
        edu.wpi.first.wpilibj.shuffleboard.Shuffleboard.getTab("Practice");

    practiceTab
        .add("Auto Routine Override", autoChooser.getSendableChooser())
        .withWidget(edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets.kComboBoxChooser)
        .withSize(3, 1)
        .withPosition(0, 0);

    practiceTab
        .add(
            "Full System Check",
            new com.marslib.diagnostics.SystemCheckCommand(
                powerManager::getVoltage, climber, cowl, shooter, intakePivot, swerveDrive))
        .withPosition(3, 0)
        .withSize(2, 1);

    practiceTab
        .addString(
            "FMS Alliance",
            () ->
                edu.wpi.first.wpilibj.DriverStation.getAlliance()
                    .map(edu.wpi.first.wpilibj.DriverStation.Alliance::toString)
                    .orElse("UNCALIBRATED"))
        .withSize(2, 1)
        .withPosition(5, 0);

    practiceTab
        .addBoolean(
            "Swerve Odometry Synchronized",
            () -> {
              com.marslib.swerve.GyroIOInputsAutoLogged gyro = swerveDrive.getGyroInputs();
              return gyro.connected;
            })
        .withSize(2, 1)
        .withPosition(3, 1);
  }

  public MARSVision getVision() {
    return vision;
  }

  public SwerveDrive getSwerveDrive() {
    return swerveDrive;
  }
}
