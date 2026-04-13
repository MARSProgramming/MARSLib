/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.vision;

import edu.wpi.first.math.geometry.Pose3d;
import org.littletonrobotics.junction.AutoLog;

/** Hardware abstraction interface for physical AprilTag tracking cameras. */
public interface AprilTagVisionIO {

  @AutoLog
  public static class AprilTagVisionIOInputs {
    public Pose3d[] estimatedPoses = new Pose3d[0];
    public double[] timestamps = new double[0];
    public int[] tagCounts = new int[0];
    public double[] averageDistancesMeters = new double[0];
    public double[] ambiguities = new double[0];
    public Pose3d[] cameraFrustum = new Pose3d[0];
  }

  public default void updateInputs(AprilTagVisionIOInputs inputs) {}

  /** Updates the camera configuration with the robot's current gyroscope orientation */
  public default void setRobotOrientation(
      double yaw, double yawRate, double pitch, double pitchRate, double roll, double rollRate) {}
}
