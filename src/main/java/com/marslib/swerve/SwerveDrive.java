/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.swerve;

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

  private final com.marslib.simulation.SwerveChassisPhysics simChassis;
  private final com.marslib.simulation.LidarIOSim lidarSim;

  private final SwerveSetpointGenerator setpointGenerator;
  private SwerveSetpointGenerator.SwerveSetpoint prevSetpoint;
  private final SwerveSetpointGenerator.KinematicLimits kinematicLimits;

  private final SwerveOdometry odometry;
  private final SwerveDiagnostics diagnostics;

  // Caches for zero-allocation performance in hot loop
  private final ChassisSpeeds scaledSpeedsCache = new ChassisSpeeds();
  private final ChassisSpeeds discretizedSpeedsCache = new ChassisSpeeds();
  private Pose2d lastPoseCache = new Pose2d();
  private Pose2d lastSimPoseCache = new Pose2d();
  private final double[] lastPose3dLogCache = new double[] {0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0};
  private double lastRollCache = 0.0;
  private double lastPitchCache = 0.0;

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
      simChassis =
          new com.marslib.simulation.SwerveChassisPhysics(
              config.robotMassKg(),
              config.bumperWidthMeters(),
              config.bumperLengthMeters(),
              config.wheelCOFStatic(),
              config.moduleLocations());
      simChassis.setPose(odometry.getPose());

      com.marslib.simulation.MARSPhysicsWorld.getInstance()
          .registerMechanismBody("SwerveDrive", simChassis.getBody());

      lidarSim = new com.marslib.simulation.LidarIOSim();

      gyroIOSim.setSwerveChassisPhysics(simChassis);
    } else {
      simChassis = null;
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
  private final double[] measuredStatesLogCache = new double[8];
  private final double[] desiredStatesLogCache = new double[8];

  public void configurePathPlanner() {
    SwerveAutoBuilder.configure(this);
  }

  @Override
  public void periodic() {
    gyroIO.updateInputs(gyroInputs);
    Logger.processInputs("SwerveDrive/Gyro", gyroInputs);

    gyroAlert.set(!gyroInputs.connected);

    for (int i = 0; i < 4; i++) {
      modules[i].periodic();
    }

    if (simChassis != null) {
      Pose2d simBoundedPose = simChassis.getPose();
      if (simBoundedPose.getX() != lastSimPoseCache.getX()
          || simBoundedPose.getY() != lastSimPoseCache.getY()
          || simBoundedPose.getRotation().getRadians()
              != lastSimPoseCache.getRotation().getRadians()) {
        lastSimPoseCache = simBoundedPose;
      }
      Logger.recordOutput("DriveTrain/SimPose", lastSimPoseCache);
      if (lidarSim != null) lidarSim.updateInputs(lastSimPoseCache);
    }

    odometry.updateOdometry(modules, gyroInputs);

    Pose2d currentPose = odometry.getPose();
    if (currentPose.getX() != lastPoseCache.getX()
        || currentPose.getY() != lastPoseCache.getY()
        || currentPose.getRotation().getRadians() != lastPoseCache.getRotation().getRadians()) {
      lastPoseCache = currentPose;
    }

    Logger.recordOutput("SwerveDrive/Pose", lastPoseCache);
    Logger.recordOutput("Robot/Pose", lastPoseCache);

    if (lastPose3dLogCache[0] != lastPoseCache.getX()
        || lastPose3dLogCache[1] != lastPoseCache.getY()
        || lastRollCache != gyroInputs.rollPositionRad
        || lastPitchCache != gyroInputs.pitchPositionRad) {

      double yaw = lastPoseCache.getRotation().getRadians();
      double roll = gyroInputs.rollPositionRad;
      double pitch = gyroInputs.pitchPositionRad;

      double cr = Math.cos(roll * 0.5);
      double sr = Math.sin(roll * 0.5);
      double cp = Math.cos(pitch * 0.5);
      double sp = Math.sin(pitch * 0.5);
      double cy = Math.cos(yaw * 0.5);
      double sy = Math.sin(yaw * 0.5);

      lastPose3dLogCache[0] = lastPoseCache.getX();
      lastPose3dLogCache[1] = lastPoseCache.getY();
      lastPose3dLogCache[2] = 0.0;
      lastPose3dLogCache[3] = cr * cp * cy + sr * sp * sy;
      lastPose3dLogCache[4] = sr * cp * cy - cr * sp * sy;
      lastPose3dLogCache[5] = cr * sp * cy + sr * cp * sy;
      lastPose3dLogCache[6] = cr * cp * sy - sr * sp * cy;

      lastRollCache = roll;
      lastPitchCache = pitch;
    }
    Logger.recordOutput("Robot/Pose3d", lastPose3dLogCache);

    odometry.updateOdometry(modules, gyroInputs);

    measuredStatesCache[0] = modules[0].getLatestState();
    measuredStatesCache[1] = modules[1].getLatestState();
    measuredStatesCache[2] = modules[2].getLatestState();
    measuredStatesCache[3] = modules[3].getLatestState();

    for (int i = 0; i < 4; i++) {
      measuredStatesLogCache[i * 2] = measuredStatesCache[i].angle.getRadians();
      measuredStatesLogCache[i * 2 + 1] = measuredStatesCache[i].speedMetersPerSecond;
    }
    Logger.recordOutput("SwerveDrive/MeasuredStates", measuredStatesLogCache);

    double currentVelocity = measuredStatesCache[0].speedMetersPerSecond;
    double currentAccel = (currentVelocity - lastDriveVelocityForSysId) / config.loopPeriodSecs();
    lastDriveVelocityForSysId = currentVelocity;

    driveFeedforwardEstimator.addMeasurement(
        modules[0].getDriveAppliedVoltage(), currentVelocity, currentAccel);
  }

  public void runVelocity(ChassisSpeeds speeds) {
    // Manual discretization to avoid ChassisSpeeds allocation
    double dtSeconds = config.loopPeriodSecs() / 2.0;
    double theta = speeds.omegaRadiansPerSecond * dtSeconds;
    double cos = Math.cos(theta);
    double sin = Math.sin(theta);

    discretizedSpeedsCache.vxMetersPerSecond =
        speeds.vxMetersPerSecond * cos - speeds.vyMetersPerSecond * sin;
    discretizedSpeedsCache.vyMetersPerSecond =
        speeds.vxMetersPerSecond * sin + speeds.vyMetersPerSecond * cos;
    discretizedSpeedsCache.omegaRadiansPerSecond = speeds.omegaRadiansPerSecond;

    double voltageScale = powerManager.calculateSheddingFactor();

    scaledSpeedsCache.vxMetersPerSecond = discretizedSpeedsCache.vxMetersPerSecond * voltageScale;
    scaledSpeedsCache.vyMetersPerSecond = discretizedSpeedsCache.vyMetersPerSecond * voltageScale;
    scaledSpeedsCache.omegaRadiansPerSecond =
        discretizedSpeedsCache.omegaRadiansPerSecond * voltageScale;

    SwerveSetpointGenerator.SwerveSetpoint newestSetpoint =
        setpointGenerator.generateSetpoint(
            kinematicLimits, prevSetpoint, scaledSpeedsCache, config.loopPeriodSecs());

    // Manual copy to avoid SwerveSetpoint allocation
    prevSetpoint.chassisSpeeds.vxMetersPerSecond = newestSetpoint.chassisSpeeds.vxMetersPerSecond;
    prevSetpoint.chassisSpeeds.vyMetersPerSecond = newestSetpoint.chassisSpeeds.vyMetersPerSecond;
    prevSetpoint.chassisSpeeds.omegaRadiansPerSecond =
        newestSetpoint.chassisSpeeds.omegaRadiansPerSecond;

    for (int i = 0; i < 4; i++) {
      prevSetpoint.moduleStates[i].speedMetersPerSecond =
          newestSetpoint.moduleStates[i].speedMetersPerSecond;
      prevSetpoint.moduleStates[i].angle = newestSetpoint.moduleStates[i].angle;
    }

    SwerveModuleState[] states = prevSetpoint.moduleStates;

    for (int i = 0; i < 4; i++) {
      modules[i].setDesiredState(states[i]);
      desiredStatesLogCache[i * 2] = states[i].angle.getRadians();
      desiredStatesLogCache[i * 2 + 1] = states[i].speedMetersPerSecond;
    }
    Logger.recordOutput("SwerveDrive/DesiredStates", desiredStatesLogCache);

    if (simChassis != null) {
      simChassis.applyKinematicSpeeds(prevSetpoint.chassisSpeeds, config.loopPeriodSecs());
    }
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
    Pose2d pose2d = simChassis != null ? simChassis.getPose() : getPose();
    return new Pose3d(
        pose2d.getX(),
        pose2d.getY(),
        0.0,
        new edu.wpi.first.math.geometry.Rotation3d(
            gyroInputs.rollPositionRad, gyroInputs.pitchPositionRad, gyroInputs.yawPositionRad));
  }

  public void resetPose(Pose2d pose) {
    if (simChassis != null) simChassis.setPose(pose);
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
