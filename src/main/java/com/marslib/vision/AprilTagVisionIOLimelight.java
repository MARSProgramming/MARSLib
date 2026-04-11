package com.marslib.vision;

import com.limelight.LimelightHelpers;
import com.limelight.LimelightHelpers.PoseEstimate;
import edu.wpi.first.math.geometry.Pose3d;

/**
 * Real hardware implementation of {@link AprilTagVisionIO} using a Limelight camera.
 *
 * <p>Fetches MegaTag2 pose estimates via the {@link LimelightHelpers} JSON API. Ambiguity is
 * defaulted to 0.0 because Limelight does not expose per-target pose ambiguity natively; downstream
 * quality control relies on distance-based standard deviation scaling in {@link MARSVision}.
 */
public class AprilTagVisionIOLimelight implements AprilTagVisionIO {

  private final String cameraName;
  private final Pose3d[] singlePoseCache = new Pose3d[1];
  private final double[] singleTimestampCache = new double[1];
  private final int[] singleTagCountCache = new int[1];
  private final double[] singleDistanceCache = new double[1];
  private final double[] singleAmbiguityCache = new double[] {0.0};

  public AprilTagVisionIOLimelight(String cameraName) {
    this.cameraName = cameraName;
  }

  @Override
  public void setRobotOrientation(
      double yaw, double yawRate, double pitch, double pitchRate, double roll, double rollRate) {
    LimelightHelpers.SetRobotOrientation(
        cameraName,
        Math.toDegrees(yaw),
        Math.toDegrees(yawRate),
        Math.toDegrees(pitch),
        Math.toDegrees(pitchRate),
        Math.toDegrees(roll),
        Math.toDegrees(rollRate));
  }

  @Override
  public void updateInputs(AprilTagVisionIOInputs inputs) {
    // Utilize traditional MegaTag2 / BotPose wpiBlue_MegaTag2
    PoseEstimate estimate = LimelightHelpers.getBotPoseEstimate_wpiBlue_MegaTag2(cameraName);

    if (estimate != null && estimate.tagCount > 0) {
      singlePoseCache[0] = estimate.pose;
      inputs.estimatedPoses = singlePoseCache;

      singleTimestampCache[0] = estimate.timestampSeconds;
      inputs.timestamps = singleTimestampCache;

      singleTagCountCache[0] = estimate.tagCount;
      inputs.tagCounts = singleTagCountCache;

      singleDistanceCache[0] = estimate.avgTagDist;
      inputs.averageDistancesMeters = singleDistanceCache;

      inputs.ambiguities = singleAmbiguityCache;
    } else {
      inputs.estimatedPoses = new Pose3d[0];
      inputs.timestamps = new double[0];
      inputs.tagCounts = new int[0];
      inputs.averageDistancesMeters = new double[0];
      inputs.ambiguities = new double[0];
    }
  }
}
