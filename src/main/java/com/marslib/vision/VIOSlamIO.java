/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.vision;

import edu.wpi.first.math.geometry.Pose3d;
import org.littletonrobotics.junction.AutoLog;

/** Hardware abstraction interface for Visual Inertial Odometry (VIO) SLAM sensors. */
public interface VIOSlamIO {

  @AutoLog
  public static class VIOSlamIOInputs {
    public Pose3d[] estimatedPoses = new Pose3d[0];
    public double[] timestamps = new double[0];
  }

  public default void updateInputs(VIOSlamIOInputs inputs) {}
}
