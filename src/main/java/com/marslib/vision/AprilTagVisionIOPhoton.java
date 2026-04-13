/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.vision;

import edu.wpi.first.math.geometry.Pose3d;
import java.util.Optional;
import org.photonvision.EstimatedRobotPose;
import org.photonvision.PhotonCamera;
import org.photonvision.PhotonPoseEstimator;

/**
 * PhotonVision hardware IO implementation for AprilTag tracking.
 *
 * <p>Automatically streams estimated poses and timestamp information into the AdvantageKit log so
 * you can precisely back-calculate vision odometry updates.
 */
public class AprilTagVisionIOPhoton implements AprilTagVisionIO {

  private final PhotonCamera camera;
  private final PhotonPoseEstimator poseEstimator;
  private static final int MAX_RESULTS = 8;
  private final Pose3d[][] poseCaches = new Pose3d[MAX_RESULTS + 1][];
  private final double[][] timestampCaches = new double[MAX_RESULTS + 1][];
  private final int[][] tagCountCaches = new int[MAX_RESULTS + 1][];
  private final double[][] distanceCaches = new double[MAX_RESULTS + 1][];
  private final double[][] ambiguityCaches = new double[MAX_RESULTS + 1][];

  public AprilTagVisionIOPhoton(String cameraName, PhotonPoseEstimator poseEstimator) {
    this.camera = new PhotonCamera(cameraName);
    this.poseEstimator = poseEstimator;
    for (int i = 0; i <= MAX_RESULTS; i++) {
      poseCaches[i] = new Pose3d[i];
      timestampCaches[i] = new double[i];
      tagCountCaches[i] = new int[i];
      distanceCaches[i] = new double[i];
      ambiguityCaches[i] = new double[i];
    }
  }

  @SuppressWarnings("removal")
  @Override
  public void updateInputs(AprilTagVisionIOInputs inputs) {
    var results = camera.getAllUnreadResults();

    int validCount = 0;
    int maxProcess = Math.min(results.size(), MAX_RESULTS);

    if (maxProcess > 0) {
      Pose3d[] pCache = poseCaches[maxProcess];
      double[] tCache = timestampCaches[maxProcess];
      int[] tcCache = tagCountCaches[maxProcess];
      double[] dCache = distanceCaches[maxProcess];
      double[] aCache = ambiguityCaches[maxProcess];

      for (int i = 0; i < maxProcess; i++) {
        var result = results.get(i);
        if (result.hasTargets()) {
          Optional<EstimatedRobotPose> estimatedPose = poseEstimator.update(result);
          if (estimatedPose.isPresent()) {
            EstimatedRobotPose pose = estimatedPose.get();
            if (pose.targetsUsed.size() > 0) {
              pCache[validCount] = pose.estimatedPose;
              tCache[validCount] = pose.timestampSeconds;
              tcCache[validCount] = pose.targetsUsed.size();

              double totalDist = 0.0;
              for (var t : pose.targetsUsed) {
                totalDist += t.getBestCameraToTarget().getTranslation().getNorm();
              }
              dCache[validCount] = totalDist / pose.targetsUsed.size();
              aCache[validCount] = pose.targetsUsed.get(0).getPoseAmbiguity();
              validCount++;
            }
          }
        }
      }

      if (validCount > 0) {
        inputs.estimatedPoses = poseCaches[validCount];
        inputs.timestamps = timestampCaches[validCount];
        inputs.tagCounts = tagCountCaches[validCount];
        inputs.averageDistancesMeters = distanceCaches[validCount];
        inputs.ambiguities = ambiguityCaches[validCount];
        return;
      }
    }

    inputs.estimatedPoses = new Pose3d[0];
    inputs.timestamps = new double[0];
    inputs.tagCounts = new int[0];
    inputs.averageDistancesMeters = new double[0];
    inputs.ambiguities = new double[0];
  }
}
