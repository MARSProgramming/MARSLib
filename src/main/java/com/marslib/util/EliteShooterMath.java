/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.util;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;

/**
 * Advanced Shot-On-The-Move mathematical solver ingested from Team 254 (2024). This utility
 * calculates exact trajectory kinematics, solving the quadratic equation for time-of-flight while
 * applying gravity and lift compensation.
 *
 * <p>Reference: Kinematics and Projectile Motion Equations are derived from <i>"Classical
 * Mechanics" (John R. Taylor) Section 2.4 - Projectile Motion with Air Resistance</i> and adapted
 * for FRC by Team 254 (2024 Whitepaper: "Hitting the Target on the Fly").
 */
public class EliteShooterMath {

  private static final double QUADRATIC_EPSILON = 1e-6;
  private static final double VELOCITY_SAFE_BUMP = 1.01;

  /**
   * Data class for holding calculated Elite Shooter parameters. Fully compatible with AdvantageKit
   * logging.
   */
  public static class EliteShooterSetpoint {
    public double robotAimYawRadians;
    public double chassisAngularFeedforward;
    public double hoodRadians;
    public double hoodFeedforward;
    public double launchSpeedMetersPerSec;
    public boolean isValid;
  }

  /**
   * Mathematically solves the exact shot state needed to hit a 3D target given current robot speeds
   * and constraints.
   *
   * @param robotPose Current field-relative robot pose
   * @param fieldRelativeSpeeds Current field-relative speeds of the chassis
   * @param targetTranslation Exact 3D field coordinates of the target
   * @param releaseHeightZ Height of the robot's shooter mechanism from the floor
   * @param nominalShotSpeedMetersPerSec Base shot velocity output limit
   * @param gravity Gravity constant (typically -9.81)
   * @param liftCoefficient Aerodynamic Magnus lift coefficient (1/m). Relates lift acceleration to
   *     velocity squared: a_lift = liftCoefficient × v². Typical values: 0.05–0.15 for FRC game
   *     pieces. Set to 0.0 to disable lift compensation.
   * @param setpoint Reference to a pre-allocated EliteShooterSetpoint to mutate and return
   * @return The same setpoint instance populated with computed values
   */
  public static EliteShooterSetpoint calculateShotOnTheMove(
      Pose2d robotPose,
      ChassisSpeeds fieldRelativeSpeeds,
      Translation3d targetTranslation,
      double releaseHeightZ,
      double nominalShotSpeedMetersPerSec,
      double gravity,
      double liftCoefficient,
      EliteShooterSetpoint setpoint) {

    double tx = targetTranslation.getX() - robotPose.getX();
    double ty = targetTranslation.getY() - robotPose.getY();
    double tz = targetTranslation.getZ() - releaseHeightZ;

    double vx = fieldRelativeSpeeds.vxMetersPerSecond;
    double vy = fieldRelativeSpeeds.vyMetersPerSecond;

    double vShot = nominalShotSpeedMetersPerSec;

    // Solve quadratic equation to obtain time of flight of game piece.
    // Derived from 1D kinematic equation: Δx = v_0 * t + 0.5 * a * t^2
    // Extended into 3D space vectors where the shooter velocity magnitude is known (vShot).
    // a = vx^2 + vy^2 - vShot^2
    // b = -2 * (tx * vx + ty * vy)
    // c = tx^2 + ty^2 + tz^2
    double quadraticA = vx * vx + vy * vy - vShot * vShot;

    if (Math.abs(quadraticA) < QUADRATIC_EPSILON) {
      // Cheat slightly to avoid division by zero / non-quadratic states
      vShot = VELOCITY_SAFE_BUMP * vShot;
      quadraticA = vx * vx + vy * vy - vShot * vShot;
    }

    double quadraticB = -2.0 * (tx * vx + ty * vy);
    double quadraticC = tx * tx + ty * ty + tz * tz;

    double discriminant = quadraticB * quadraticB - 4.0 * quadraticA * quadraticC;
    if (discriminant < 0.0) {
      discriminant = 0.0;
    }

    // Solve for time of flight
    double timeOfFlightSeconds = (-quadraticB - Math.sqrt(discriminant)) / (2.0 * quadraticA);

    if (timeOfFlightSeconds <= 0) {
      setpoint.isValid = false;
      return setpoint;
    }

    double virtualShotX = (tx - vx * timeOfFlightSeconds) / timeOfFlightSeconds;
    double virtualShotY = (ty - vy * timeOfFlightSeconds) / timeOfFlightSeconds;

    double virtualTargetYawRad = Math.atan2(virtualShotY, virtualShotX);
    double xyVel = Math.sqrt(virtualShotX * virtualShotX + virtualShotY * virtualShotY);

    // Apply gravity and lift compensation
    // Gravity contributes downward displacement: Δz_gravity = 0.5 * g * t²
    // Magnus lift on a spinning game piece creates upward acceleration proportional to v²:
    //   a_lift = liftCoefficient * v²  [units: liftCoefficient is 1/m]
    //   Δz_lift = 0.5 * a_lift * t² = 0.5 * liftCoefficient * vShot² * t²
    double drop = 0.5 * gravity * timeOfFlightSeconds * timeOfFlightSeconds;
    drop += 0.5 * liftCoefficient * vShot * vShot * timeOfFlightSeconds * timeOfFlightSeconds;

    double virtualDeltaZ = tz - drop;
    double pitchAngleRads = Math.atan2(virtualDeltaZ / timeOfFlightSeconds, xyVel);
    double adjustedVShot =
        Math.sqrt(
            (virtualDeltaZ * virtualDeltaZ) / (timeOfFlightSeconds * timeOfFlightSeconds)
                + xyVel * xyVel);

    // Compute Chassis Aim and Feedforward
    double distanceToTargetSq = tx * tx + ty * ty;

    double chassisAngularFF = (ty * vx - tx * vy) / distanceToTargetSq;

    // Rotate field speeds to target frame natively: cos * vx + sin * vy
    double cosYaw = Math.cos(virtualTargetYawRad);
    double sinYaw = Math.sin(virtualTargetYawRad);
    double targetFrameVx = vx * cosYaw + vy * sinYaw;

    double hoodFF = targetFrameVx * tz / (distanceToTargetSq + tz * tz);

    setpoint.robotAimYawRadians = virtualTargetYawRad;
    setpoint.chassisAngularFeedforward = chassisAngularFF;
    setpoint.hoodRadians = pitchAngleRads;
    setpoint.hoodFeedforward = hoodFF;
    setpoint.launchSpeedMetersPerSec = adjustedVShot;

    // The shot is only valid if the required velocity is reasonably close to our nominal
    // capability.
    // If the approximation delta is > 25%, the shot is physically impossible at this speed.
    setpoint.isValid = adjustedVShot <= (nominalShotSpeedMetersPerSec * 1.25);

    return setpoint;
  }
}
