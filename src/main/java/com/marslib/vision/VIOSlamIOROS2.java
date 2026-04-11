package com.marslib.vision;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StructSubscriber;
import edu.wpi.first.networktables.TimestampedObject;

/**
 * Real hardware implementation of {@link VIOSlamIO} sourcing poses from a ROS2 coprocessor.
 *
 * <p>Subscribes to a {@link Pose3d} struct topic published by a Jetson/Pi running a ROS2 SLAM node.
 * Timestamps are converted from NT4 microseconds to WPILib seconds for fusion compatibility.
 */
public class VIOSlamIOROS2 implements VIOSlamIO {

  private final StructSubscriber<Pose3d> poseSub;

  private static final int MAX_RESULTS = 8;
  private final Pose3d[][] poseCaches = new Pose3d[MAX_RESULTS + 1][];
  private final double[][] timestampCaches = new double[MAX_RESULTS + 1][];

  public VIOSlamIOROS2(String tableName, String topicName) {
    poseSub =
        NetworkTableInstance.getDefault()
            .getTable(tableName)
            .getStructTopic(topicName, Pose3d.struct)
            .subscribe(new Pose3d());

    for (int i = 0; i <= MAX_RESULTS; i++) {
      poseCaches[i] = new Pose3d[i];
      timestampCaches[i] = new double[i];
    }
  }

  @Override
  public void updateInputs(VIOSlamIOInputs inputs) {
    // NT4 Struct fetching guarantees no array mismatch issues
    TimestampedObject<Pose3d>[] updates = poseSub.readQueue();

    int validCount = Math.min(updates.length, MAX_RESULTS);

    if (validCount > 0) {
      Pose3d[] pCache = poseCaches[validCount];
      double[] tCache = timestampCaches[validCount];

      for (int i = 0; i < validCount; i++) {
        TimestampedObject<Pose3d> update = updates[i];
        pCache[i] = update.value;
        tCache[i] = update.timestamp / 1_000_000.0;
      }

      inputs.estimatedPoses = pCache;
      inputs.timestamps = tCache;
    } else {
      inputs.estimatedPoses = new Pose3d[0];
      inputs.timestamps = new double[0];
    }
  }
}
