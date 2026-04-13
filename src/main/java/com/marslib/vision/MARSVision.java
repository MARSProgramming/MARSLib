/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.vision;

import com.marslib.swerve.GyroIOInputsAutoLogged;
import com.marslib.swerve.SwerveDrive;
import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.constants.FieldConstants;
import frc.robot.constants.VisionConstants;
import java.util.List;
import java.util.Optional;
import org.littletonrobotics.junction.Logger;

/**
 * Central aggregator for all visual and SLAM external odometry integrations.
 *
 * <p>Students: This subsystem actively pools array data from unlimited AprilTag and VIO SLAM
 * pipelines. It analyzes the physical ambiguity of every target, scales standard deviations
 * dynamically, and executes strict multi-factor 254-style rejection conditions before passing
 * coordinates into the SwerveDrive Pose Estimator.
 */
public class MARSVision extends SubsystemBase {
  private final SwerveDrive swerveDrive;
  private final List<AprilTagVisionIO> aprilTagIOs;
  private final AprilTagVisionIOInputsAutoLogged[] aprilTagInputs;
  private Optional<Translation2d> latestTargetTranslation = Optional.empty();

  private double lastYawVelocity = 0.0;
  private double lastPitchVelocity = 0.0;
  private double lastRollVelocity = 0.0;

  private double lastPoseX = 0.0;
  private double lastPoseY = 0.0;

  private final List<VIOSlamIO> slamIOs;
  private final VIOSlamIOInputsAutoLogged[] slamInputs;

  /**
   * Constructs the absolute Vision mapping structure.
   *
   * @param swerveDrive The primary SwerveDrive subsystem reference for data injection.
   * @param aprilTagIOs A list of all active AprilTag IO architectures (Limelight, Photon).
   * @param slamIOs A list of all active VIO SLAM IO architectures (QuestNav, ROS2).
   */
  public MARSVision(
      SwerveDrive swerveDrive, List<AprilTagVisionIO> aprilTagIOs, List<VIOSlamIO> slamIOs) {
    this.swerveDrive = swerveDrive;
    this.aprilTagIOs = aprilTagIOs;
    this.aprilTagInputs = new AprilTagVisionIOInputsAutoLogged[aprilTagIOs.size()];
    for (int i = 0; i < aprilTagInputs.length; i++) {
      aprilTagInputs[i] = new AprilTagVisionIOInputsAutoLogged();
    }

    this.slamIOs = slamIOs;
    this.slamInputs = new VIOSlamIOInputsAutoLogged[slamIOs.size()];
    for (int i = 0; i < slamInputs.length; i++) {
      slamInputs[i] = new VIOSlamIOInputsAutoLogged();
    }
  }

  /**
   * The active periodic layer. Scans through all mapped interfaces exactly once per loop. Executes
   * advanced field constraints, motion blur rejection, and ambiguity thresholding natively.
   */
  @Override
  public void periodic() {
    latestTargetTranslation = Optional.empty(); // Reset each loop unless a target is found

    GyroIOInputsAutoLogged gyro = swerveDrive.getGyroInputs();

    double dt = frc.robot.constants.ModeConstants.LOOP_PERIOD_SECS;
    double maxAngularAccel =
        Math.max(
            Math.abs((gyro.yawVelocityRadPerSec - lastYawVelocity) / dt),
            Math.max(
                Math.abs((gyro.pitchVelocityRadPerSec - lastPitchVelocity) / dt),
                Math.abs((gyro.rollVelocityRadPerSec - lastRollVelocity) / dt)));

    lastYawVelocity = gyro.yawVelocityRadPerSec;
    lastPitchVelocity = gyro.pitchVelocityRadPerSec;
    lastRollVelocity = gyro.rollVelocityRadPerSec;

    boolean isImpactShock =
        Math.toDegrees(maxAngularAccel) > VisionConstants.MAX_ANGULAR_ACCEL_DEG_PER_SEC2.get();

    Pose2d currentFilteredPose = swerveDrive.getPose();
    double dx = currentFilteredPose.getX() - lastPoseX;
    double dy = currentFilteredPose.getY() - lastPoseY;
    double smoothLinearVelocity = Math.hypot(dx, dy) / dt;

    lastPoseX = currentFilteredPose.getX();
    lastPoseY = currentFilteredPose.getY();

    double continuousVelocityMultiplier =
        1.0
            + (smoothLinearVelocity * VisionConstants.LINEAR_VELOCITY_STD_MULTIPLIER.get())
            + (Math.toDegrees(Math.abs(gyro.yawVelocityRadPerSec))
                * VisionConstants.ANGULAR_VELOCITY_STD_MULTIPLIER.get());

    // Process AprilTags
    for (int i = 0; i < aprilTagIOs.size(); i++) {

      if (gyro.connected) {
        aprilTagIOs
            .get(i)
            .setRobotOrientation(
                gyro.yawPositionRad, gyro.yawVelocityRadPerSec,
                gyro.pitchPositionRad, gyro.pitchVelocityRadPerSec,
                gyro.rollPositionRad, gyro.rollVelocityRadPerSec);
      }

      aprilTagIOs.get(i).updateInputs(aprilTagInputs[i]);
      Logger.processInputs("Vision/AprilTag/" + i, aprilTagInputs[i]);

      int acceptedCount = 0;
      boolean rejectedTilt = false;
      boolean rejectedOOB = false;
      boolean rejectedZHeight = false;
      boolean rejectedAmbiguity = false;
      boolean rejectedImpactShock = false;

      for (int f = 0; f < aprilTagInputs[i].estimatedPoses.length; f++) {
        Pose3d pose3d = aprilTagInputs[i].estimatedPoses[f];
        int tagCount = aprilTagInputs[i].tagCounts[f];
        double ambiguity = aprilTagInputs[i].ambiguities[f];
        double avgDist = aprilTagInputs[i].averageDistancesMeters[f];
        double timestamp = aprilTagInputs[i].timestamps[f];

        Pose2d pose2d = pose3d.toPose2d();

        // Check 1: Z-Height Hallucination
        if (Math.abs(pose3d.getZ()) > VisionConstants.MAX_Z_HEIGHT.get()) {
          rejectedZHeight = true;
          continue;
        }

        // Check 2: Field Bounds
        double margin = VisionConstants.FIELD_MARGIN_METERS.get();
        if (pose2d.getX() < -margin
            || pose2d.getX() > FieldConstants.FIELD_LENGTH_METERS + margin
            || pose2d.getY() < -margin
            || pose2d.getY() > FieldConstants.FIELD_WIDTH_METERS + margin) {
          rejectedOOB = true;
          continue;
        }

        // Check 3: Beached / Tilt
        double maxTilt = Math.max(Math.abs(gyro.pitchPositionRad), Math.abs(gyro.rollPositionRad));
        if (Math.toDegrees(maxTilt) > VisionConstants.MAX_TILT_DEG.get()) {
          rejectedTilt = true;
          continue;
        }

        // Check 4: Impact Shock / Jerk (Vibrational blur)
        if (isImpactShock) {
          rejectedImpactShock = true;
          continue;
        }

        // Check 5: Ambiguity (For PhotonVision single-tag. Limelight defaults to 0.0)
        if (tagCount == 1 && ambiguity > VisionConstants.MAX_AMBIGUITY.get()) {
          rejectedAmbiguity = true;
          continue;
        }

        acceptedCount++;

        // Calculate standard deviations (Quadratic distance scaling)
        double linearStdDev = VisionConstants.TAG_STD_BASE.get() * Math.pow(avgDist, 2);

        // MegaTag2 Boost: Dramatically tighten bounds when multiple tags are visible
        if (tagCount > 1) {
          linearStdDev *= VisionConstants.MULTI_TAG_STD_MULTIPLIER.get();
        }

        double angularStdDev = linearStdDev * VisionConstants.ANGULAR_STD_MULTIPLIER.get();

        // Continuous Velocity Scaling: smoothly blur out vision trust at max speeds without hard
        // cutoffs
        linearStdDev *= continuousVelocityMultiplier;
        angularStdDev *= continuousVelocityMultiplier;

        Matrix<N3, N1> stdDevs = VecBuilder.fill(linearStdDev, linearStdDev, angularStdDev);

        swerveDrive.addVisionMeasurement(pose2d, timestamp, stdDevs);
        Logger.recordOutput("Vision/ValidPoses/" + i, pose2d);
        latestTargetTranslation = Optional.of(pose2d.getTranslation());
      }

      // Log rejection telemetry
      Logger.recordOutput("Vision/Rejected/Tilt/" + i, rejectedTilt);
      Logger.recordOutput("Vision/Rejected/OutOfBounds/" + i, rejectedOOB);
      Logger.recordOutput("Vision/Rejected/ZHeight/" + i, rejectedZHeight);
      Logger.recordOutput("Vision/Rejected/Ambiguity/" + i, rejectedAmbiguity);
      Logger.recordOutput("Vision/Rejected/ImpactShock/" + i, rejectedImpactShock);
      Logger.recordOutput("Vision/AcceptedCount/" + i, acceptedCount);

      Logger.recordOutput("Vision/CameraFrustums/" + i, aprilTagInputs[i].cameraFrustum);
    }

    // Process SLAM
    for (int i = 0; i < slamIOs.size(); i++) {
      slamIOs.get(i).updateInputs(slamInputs[i]);
      Logger.processInputs("Vision/SLAM/" + i, slamInputs[i]);

      for (int f = 0; f < slamInputs[i].estimatedPoses.length; f++) {
        Pose3d pose3d = slamInputs[i].estimatedPoses[f];
        double timestamp = slamInputs[i].timestamps[f];

        // Tight static covariance for reliable VIO odometry
        Matrix<N3, N1> stdDevs =
            VecBuilder.fill(
                VisionConstants.SLAM_STD_DEV.get(),
                VisionConstants.SLAM_STD_DEV.get(),
                VisionConstants.SLAM_ANGULAR_STD_DEV.get());
        Pose2d pose2d = pose3d.toPose2d();

        swerveDrive.addVisionMeasurement(pose2d, timestamp, stdDevs);
        Logger.recordOutput("Vision/SlamPoses/" + i, pose2d);
      }
    }
  }

  /**
   * Retrieves the latest, rigorously vetted vision-based target translation. This provides a direct
   * fallback for SOTM and aiming loops if odometry is drifting.
   */
  public Optional<Translation2d> getBestTargetTranslation() {
    return latestTargetTranslation;
  }
}
