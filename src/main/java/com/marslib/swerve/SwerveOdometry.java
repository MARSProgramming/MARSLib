/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.swerve;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import org.littletonrobotics.junction.Logger;

/**
 * Encapsulates high-frequency (250Hz) odometry tracking and vision fusion. Drains timestamps
 * natively from hardware threads to prevent main-loop discretization error.
 */
public class SwerveOdometry {
  private final SwerveDrivePoseEstimator poseEstimator;
  private final SwerveDriveKinematics kinematics;
  private final SwerveConfig config;

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
  private Pose3d pose3dCache = new Pose3d();

  public SwerveOdometry(
      SwerveDriveKinematics kinematics,
      SwerveConfig config,
      SwerveModulePosition... initialPositions) {
    this.kinematics = kinematics;
    this.config = config;
    this.poseEstimator =
        new SwerveDrivePoseEstimator(kinematics, new Rotation2d(), initialPositions, new Pose2d());
  }

  /**
   * Performs the high-frequency synchronous drain of the cached module vectors, updating the pose
   * estimator with sub-loop accuracy.
   */
  public void updateOdometry(SwerveModule[] modules, GyroIOInputsAutoLogged gyroInputs) {
    double odometryTrust = 1.0;
    if (gyroInputs.connected) {
      double cosTilt =
          edu.wpi.first.math.MathUtil.clamp(
              Math.cos(gyroInputs.pitchPositionRad) * Math.cos(gyroInputs.rollPositionRad),
              -1.0,
              1.0);
      double tiltRadians = Math.acos(cosTilt);
      odometryTrust =
          1.0
              - edu.wpi.first.math.MathUtil.inverseInterpolate(
                  0, Math.toRadians(25.0), Math.abs(tiltRadians));
      odometryTrust = edu.wpi.first.math.MathUtil.clamp(odometryTrust, 0.0, 1.0);
    }
    Logger.recordOutput("SwerveDrive/OdometryTrustMultiplier", odometryTrust);

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

        // GC-free: mutate pre-allocated position objects
        positionsForFrame[m].distanceMeters = scaledPositions[m].distanceMeters;
        positionsForFrame[m].angle = scaledPositions[m].angle;
      }
      isFirstOdometryDrain = false;

      double frameYawRad;
      if (gyroInputs.connected && gyroInputs.odometryYawPositions.length > 0) {
        frameYawRad =
            gyroInputs
                .odometryYawPositions[Math.min(i, gyroInputs.odometryYawPositions.length - 1)];
      } else if (gyroInputs.connected) {
        frameYawRad = gyroInputs.yawPositionRad;
      } else {
        // Dead-reckoning fallback
        ChassisSpeeds wheelSpeeds =
            kinematics.toChassisSpeeds(
                modules[0].getLatestState(), modules[1].getLatestState(),
                modules[2].getLatestState(), modules[3].getLatestState());
        double dt = config.loopPeriodSecs();
        frameYawRad = frameYawCache[0].getRadians() + wheelSpeeds.omegaRadiansPerSecond * dt;
      }
      if (Math.abs(frameYawRad - frameYawCache[0].getRadians()) > 1e-6) {
        frameYawCache[0] = Rotation2d.fromRadians(frameYawRad);
      }

      double timestamp =
          (timestamps.length > i) ? timestamps[i] : edu.wpi.first.wpilibj.Timer.getFPGATimestamp();

      poseEstimator.updateWithTime(timestamp, frameYawCache[0], positionsForFrame);
    }
  }

  public Pose2d getPose() {
    return poseEstimator.getEstimatedPosition();
  }

  public Pose3d getPose3d(GyroIOInputsAutoLogged gyroInputs) {
    Pose2d pose2d = getPose();
    if (pose2d.getX() != pose3dCache.getX()
        || pose2d.getY() != pose3dCache.getY()
        || pose2d.getRotation().getRadians() != pose3dCache.getRotation().getZ()
        || gyroInputs.rollPositionRad != pose3dCache.getRotation().getX()
        || gyroInputs.pitchPositionRad != pose3dCache.getRotation().getY()) {
      pose3dCache =
          new Pose3d(
              pose2d.getX(),
              pose2d.getY(),
              0.0,
              new Rotation3d(
                  gyroInputs.rollPositionRad,
                  gyroInputs.pitchPositionRad,
                  pose2d.getRotation().getRadians()));
    }
    return pose3dCache;
  }

  public void resetPose(Pose2d pose, GyroIOInputsAutoLogged gyroInputs, SwerveModule... modules) {
    for (int i = 0; i < 4; i++) {
      SwerveModulePosition raw = modules[i].getLatestPosition();
      scaledPositions[i].distanceMeters = raw.distanceMeters;
      scaledPositions[i].angle = raw.angle;
      lastRawDistances[i] = raw.distanceMeters;
    }
    isFirstOdometryDrain = false;

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

  public void addVisionMeasurement(
      Pose2d visionRobotPoseMeters,
      double timestampSeconds,
      Matrix<N3, N1> visionMeasurementStdDevs) {
    poseEstimator.addVisionMeasurement(
        visionRobotPoseMeters, timestampSeconds, visionMeasurementStdDevs);
  }
}
