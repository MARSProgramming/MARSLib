/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.swerve;

import static edu.wpi.first.units.Units.Kilograms;
import static edu.wpi.first.units.Units.Meters;

import com.marslib.diagnostics.SystemTestable;
import com.marslib.power.MARSPowerManager;
import com.marslib.util.OnlineFeedforwardEstimator;
import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import java.util.Arrays;
import java.util.function.Supplier;
import org.ironmaple.simulation.drivesims.COTS;
import org.ironmaple.simulation.drivesims.SwerveDriveSimulation;
import org.ironmaple.simulation.drivesims.configs.DriveTrainSimulationConfig;
import org.littletonrobotics.junction.Logger;

/**
 * The core MARSLib SwerveDrive subsystem responsible for managing holonomic kinematics, dynamic
 * PathPlanner autonomous trajectories, and active power shedding.
 */
public class SwerveDrive extends SubsystemBase implements SystemTestable {
  private final SwerveModule[] modules;
  private final GyroIO gyroIO;
  private final GyroIOInputsAutoLogged gyroInputs = new GyroIOInputsAutoLogged();
  private final SwerveDriveKinematics kinematics;
  private final MARSPowerManager powerManager;
  private final OnlineFeedforwardEstimator driveFeedforwardEstimator;
  private final SwerveConfig config;

  private final com.marslib.faults.Alert gyroAlert =
      new com.marslib.faults.Alert(
          "SwerveDrive",
          "Gyro Disconnected. Odometry Trust Lost.",
          com.marslib.faults.Alert.AlertType.CRITICAL);

  private double lastDriveVelocityForSysId = 0.0;

  private final SwerveDriveSimulation simDrive;
  private final com.marslib.simulation.LidarIOSim lidarSim;

  private final SwerveSetpointGenerator setpointGenerator;
  private SwerveSetpointGenerator.SwerveSetpoint prevSetpoint;
  private final SwerveSetpointGenerator.KinematicLimits kinematicLimits;

  // Extracted Architecture Components
  private final SwerveOdometry odometry;
  private final SwerveDiagnostics diagnostics;

  @SuppressWarnings("PMD.NullAssignment")
  public SwerveDrive(
      SwerveModule[] modules, GyroIO gyroIO, MARSPowerManager powerManager, SwerveConfig config) {
    this.modules = Arrays.copyOf(modules, modules.length);
    this.gyroIO = gyroIO;
    this.powerManager = powerManager;
    this.config = config;

    GyroIOSim gyroIOSim = (gyroIO instanceof GyroIOSim) ? (GyroIOSim) gyroIO : null;

    this.kinematics = new SwerveDriveKinematics(config.moduleLocations());

    SwerveModulePosition[] initialPositions =
        new SwerveModulePosition[] {
          modules[0].getLatestPosition(), modules[1].getLatestPosition(),
          modules[2].getLatestPosition(), modules[3].getLatestPosition()
        };

    this.odometry = new SwerveOdometry(kinematics, config, initialPositions);
    this.diagnostics = new SwerveDiagnostics(this, this.modules);

    if (gyroIOSim != null) {
      DriveTrainSimulationConfig driveSimConfig =
          DriveTrainSimulationConfig.Default()
              .withRobotMass(Kilograms.of(config.robotMassKg()))
              .withBumperSize(
                  Meters.of(config.bumperLengthMeters()), Meters.of(config.bumperWidthMeters()))
              .withTrackLengthTrackWidth(
                  Meters.of(config.wheelbaseMeters()), Meters.of(config.trackWidthMeters()))
              .withSwerveModule(
                  COTS.ofMark4(
                      edu.wpi.first.math.system.plant.DCMotor.getKrakenX60Foc(1),
                      edu.wpi.first.math.system.plant.DCMotor.getKrakenX60Foc(1),
                      config.wheelCOFStatic(),
                      2));

      simDrive = new SwerveDriveSimulation(driveSimConfig, odometry.getPose());
      com.marslib.simulation.MARSPhysicsWorld.getInstance()
          .getArena()
          .addDriveTrainSimulation(simDrive);

      lidarSim = new com.marslib.simulation.LidarIOSim();

      for (int i = 0; i < modules.length; i++) {
        modules[i].injectModuleSimulation(simDrive.getModules()[i]);
      }

      gyroIOSim.setGyroSimulation(simDrive.getGyroSimulation());
      gyroIOSim.setSwerveDriveSimulation(simDrive);
    } else {
      simDrive = null;
      lidarSim = null;
    }

    this.driveFeedforwardEstimator = new OnlineFeedforwardEstimator("SwerveDrive", 500, 0.0);

    this.setpointGenerator = new SwerveSetpointGenerator(this.kinematics);
    this.kinematicLimits = new SwerveSetpointGenerator.KinematicLimits();
    this.kinematicLimits.maxDriveVelocity = config.maxLinearSpeedMps();
    this.kinematicLimits.maxDriveAcceleration = config.wheelCOFStatic() * 9.81;
    this.kinematicLimits.maxSteeringVelocity = config.maxAngularSpeedRadPerSec();

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

  private final SwerveModuleState[] measuredStatesCache = new SwerveModuleState[4];

  public void configurePathPlanner() {
    SwerveAutoBuilder.configure(this);
  }

  @Override
  public void periodic() {
    gyroIO.updateInputs(gyroInputs);
    Logger.processInputs("SwerveDrive/Gyro", gyroInputs);

    gyroAlert.set(!gyroInputs.connected);

    for (SwerveModule module : modules) {
      module.periodic();
    }

    // High frequency synchronous drain extracted to Odometry class to save lines
    odometry.updateOdometry(modules, gyroInputs);

    if (simDrive != null) {
      Pose2d simBoundedPose = simDrive.getSimulatedDriveTrainPose();
      Logger.recordOutput("DriveTrain/SimPose", simBoundedPose);
      if (lidarSim != null) lidarSim.updateInputs(simBoundedPose);
    }

    Pose2d currentPose = odometry.getPose();
    Logger.recordOutput("SwerveDrive/Pose", currentPose);
    Logger.recordOutput("Odometry/RobotPose", currentPose);
    Logger.recordOutput("Robot/Pose", currentPose);
    Logger.recordOutput("Robot/Pose3d", getPose3d());

    measuredStatesCache[0] = modules[0].getLatestState();
    measuredStatesCache[1] = modules[1].getLatestState();
    measuredStatesCache[2] = modules[2].getLatestState();
    measuredStatesCache[3] = modules[3].getLatestState();
    Logger.recordOutput("SwerveDrive/MeasuredStates", measuredStatesCache);

    double currentVelocity = measuredStatesCache[0].speedMetersPerSecond;
    double currentAccel = (currentVelocity - lastDriveVelocityForSysId) / config.loopPeriodSecs();
    lastDriveVelocityForSysId = currentVelocity;

    driveFeedforwardEstimator.addMeasurement(
        modules[0].getDriveAppliedVoltage(), currentVelocity, currentAccel);
  }

  public void runVelocity(ChassisSpeeds speeds) {
    ChassisSpeeds discretizedSpeeds = ChassisSpeeds.discretize(speeds, config.loopPeriodSecs());

    double voltageScale = powerManager.calculateSheddingFactor();

    ChassisSpeeds scaledSpeeds =
        new ChassisSpeeds(
            discretizedSpeeds.vxMetersPerSecond * voltageScale,
            discretizedSpeeds.vyMetersPerSecond * voltageScale,
            discretizedSpeeds.omegaRadiansPerSecond * voltageScale);

    this.prevSetpoint =
        setpointGenerator.generateSetpoint(
            kinematicLimits, prevSetpoint, scaledSpeeds, config.loopPeriodSecs());

    SwerveModuleState[] states = prevSetpoint.moduleStates;

    for (int i = 0; i < 4; i++) {
      modules[i].setDesiredState(states[i]);
    }
    Logger.recordOutput("SwerveDrive/DesiredStates", states);
  }

  public void setModuleStates(SwerveModuleState... states) {
    SwerveDriveKinematics.desaturateWheelSpeeds(states, config.maxLinearSpeedMps());
    for (int i = 0; i < 4; i++) modules[i].setDesiredState(states[i]);
    Logger.recordOutput("SwerveDrive/DesiredStates", states);
  }

  public Pose2d getPose() {
    return odometry.getPose();
  }

  public Pose3d getPose3d() {
    return odometry.getPose3d(gyroInputs);
  }

  public Pose3d getSimPose3d() {
    Pose2d pose2d = simDrive != null ? simDrive.getSimulatedDriveTrainPose() : getPose();
    return new Pose3d(
        pose2d.getX(),
        pose2d.getY(),
        0.0,
        new edu.wpi.first.math.geometry.Rotation3d(
            gyroInputs.rollPositionRad, gyroInputs.pitchPositionRad, gyroInputs.yawPositionRad));
  }

  public void resetPose(Pose2d pose) {
    if (simDrive != null) simDrive.setSimulationWorldPose(pose);
    odometry.resetPose(pose, gyroInputs, modules);
  }

  public ChassisSpeeds getChassisSpeeds() {
    return kinematics.toChassisSpeeds(
        modules[0].getLatestState(), modules[1].getLatestState(),
        modules[2].getLatestState(), modules[3].getLatestState());
  }

  public void addVisionMeasurement(
      Pose2d visionRobotPoseMeters,
      double timestampSeconds,
      Matrix<N3, N1> visionMeasurementStdDevs) {
    odometry.addVisionMeasurement(
        visionRobotPoseMeters, timestampSeconds, visionMeasurementStdDevs);
  }

  public GyroIOInputsAutoLogged getGyroInputs() {
    return gyroInputs;
  }

  public Command sysIdQuasistatic(SysIdRoutine.Direction direction) {
    return diagnostics.sysIdQuasistatic(direction);
  }

  public Command sysIdDynamic(SysIdRoutine.Direction direction) {
    return diagnostics.sysIdDynamic(direction);
  }

  public Command alignToPoint(Supplier<Pose2d> target) {
    return SwerveAutoBuilder.alignToPoint(this, target);
  }

  public Command finalClimbLineupCommand() {
    return diagnostics.finalClimbLineupCommand();
  }

  @Override
  public Command getSystemCheckCommand() {
    return diagnostics.getSystemCheckCommand();
  }

  public SwerveConfig getConfig() {
    return config;
  }
}
