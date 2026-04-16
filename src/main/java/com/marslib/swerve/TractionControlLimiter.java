/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.swerve;

import org.littletonrobotics.junction.Logger;

/**
 * Advanced 2D Slew Rate Limiter to prevent carpet slippage and Odometry drift.
 *
 * <p>Standard WPILib SlewRateLimiters operate independently on X and Y axes. If both max out, the
 * robot accelerates diagonally at 1.41x the limit, instantly breaking carpet friction. This class
 * ensures the total 2D acceleration vector never exceeds the maximum static friction coefficient of
 * the FRC carpet (usually ~1.1g or 10.78 m/s^2).
 */
public class TractionControlLimiter {

  private final double maxAccelMetersPerSecSq;
  private double lastVx = 0.0;
  private double lastVy = 0.0;
  private double lastTime = Logger.getTimestamp();

  /**
   * Initializes the 2D Traction Control limit.
   *
   * @param maxAccelMetersPerSecSq Maximum physical acceleration before tire slip occurs.
   */
  public TractionControlLimiter(double maxAccelMetersPerSecSq) {
    this.maxAccelMetersPerSecSq = maxAccelMetersPerSecSq;
  }

  /**
   * Resets the limiter to the specified velocity and current timestamp. This prevents elapsed time
   * drift from creating large calculation deltas across command scheduling boundaries.
   *
   * @param currentVx Field relative X velocity in meters per second.
   * @param currentVy Field relative Y velocity in meters per second.
   */
  public void reset(double currentVx, double currentVy) {
    this.lastVx = currentVx;
    this.lastVy = currentVy;
    this.lastTime = (double) Logger.getTimestamp();
  }

  /**
   * Calculates the max achievable velocity vector without slipping tires.
   *
   * @param targetVx Target requested X velocity.
   * @param targetVy Target requested Y velocity.
   * @param outputSpeeds The ChassisSpeeds object to mutate with the safe velocities.
   */
  public void calculate(
      double targetVx, double targetVy, edu.wpi.first.math.kinematics.ChassisSpeeds outputSpeeds) {
    double currentTime = (double) Logger.getTimestamp();
    double dt =
        (currentTime - lastTime) / 1000000.0; // Convert AdvantageKit microseconds to seconds
    lastTime = currentTime;

    // Prevent divide-by-zero on first loop
    if (dt <= 0.0) {
      outputSpeeds.vxMetersPerSecond = lastVx;
      outputSpeeds.vyMetersPerSecond = lastVy;
      return;
    }

    // Calculate requested velocity change
    double dVx = targetVx - lastVx;
    double dVy = targetVy - lastVy;

    // Total acceleration required to achieve this change
    double currentAccel = Math.hypot(dVx, dVy) / dt;

    if (currentAccel > maxAccelMetersPerSecSq) {
      // Scale down the change to exactly match peak acceleration
      double scalar = maxAccelMetersPerSecSq / currentAccel;
      dVx *= scalar;
      dVy *= scalar;
    }

    lastVx += dVx;
    lastVy += dVy;

    // Snap to zero when nearly stopped to avoid floating-point drift
    if (Math.hypot(targetVx, targetVy) == 0.0 && Math.hypot(lastVx, lastVy) < 0.05) {
      lastVx = 0.0;
      lastVy = 0.0;
    }

    outputSpeeds.vxMetersPerSecond = lastVx;
    outputSpeeds.vyMetersPerSecond = lastVy;
  }
}
