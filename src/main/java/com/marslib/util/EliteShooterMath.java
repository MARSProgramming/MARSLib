package com.marslib.util;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;

/**
 * Advanced Shot-On-The-Move mathematical solver ingested from Team 254 (2024). This utility
 * calculates exact trajectory kinematics, solving the quadratic equation for time-of-flight while
 * applying gravity and lift compensation.
 */
public class EliteShooterMath {

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
   * @param liftCoefficient Aerodynamic lift coefficient of the game piece
   * @return Computed EliteShooterSetpoint with exact angles and feedforwards
   */
  public static EliteShooterSetpoint calculateShotOnTheMove(
      Pose2d robotPose,
      ChassisSpeeds fieldRelativeSpeeds,
      Translation3d targetTranslation,
      double releaseHeightZ,
      double nominalShotSpeedMetersPerSec,
      double gravity,
      double liftCoefficient) {

    EliteShooterSetpoint setpoint = new EliteShooterSetpoint();

    double tx = targetTranslation.getX() - robotPose.getX();
    double ty = targetTranslation.getY() - robotPose.getY();
    double tz = targetTranslation.getZ() - releaseHeightZ;

    double vx = fieldRelativeSpeeds.vxMetersPerSecond;
    double vy = fieldRelativeSpeeds.vyMetersPerSecond;

    double vShot = nominalShotSpeedMetersPerSec;

    // Solve quadratic equation to obtain time of flight of game piece.
    // a = vx^2 + vy^2 - vShot^2
    // b = -2 * (tx * vx + ty * vy)
    // c = tx^2 + ty^2 + tz^2
    double a = vx * vx + vy * vy - vShot * vShot;

    if (Math.abs(a) < 1e-6) {
      // Cheat slightly to avoid division by zero / non-quadratic states
      vShot = 1.01 * vShot;
      a = vx * vx + vy * vy - vShot * vShot;
    }

    double b = -2.0 * (tx * vx + ty * vy);
    double c = tx * tx + ty * ty + tz * tz;

    double discriminant = b * b - 4.0 * a * c;
    if (discriminant < 0.0) {
      discriminant = 0.0;
    }

    // Solve for time of flight (t)
    double t = (-b - Math.sqrt(discriminant)) / (2.0 * a);

    if (t <= 0) {
      setpoint.isValid = false;
      return setpoint;
    }

    double virtualShotX = (tx - vx * t) / t;
    double virtualShotY = (ty - vy * t) / t;

    double virtualTargetYawRad = Math.atan2(virtualShotY, virtualShotX);
    double xyVel = Math.sqrt(virtualShotX * virtualShotX + virtualShotY * virtualShotY);

    // Apply gravity and lift compensation
    double drop = 0.5 * t * t * gravity;
    drop += 0.5 * liftCoefficient * c;

    double virtualDeltaZ = tz - drop;
    double pitchAngleRads = Math.atan2(virtualDeltaZ / t, xyVel);
    double adjustedVShot = Math.sqrt((virtualDeltaZ * virtualDeltaZ) / (t * t) + xyVel * xyVel);

    // Compute Chassis Aim and Feedforward
    double distanceToTargetSq = tx * tx + ty * ty;

    double chassisAngularFF = (ty * vx - tx * vy) / distanceToTargetSq;

    // Rotate field speeds to target frame natively: cos * vx + sin * vy
    double cosYaw = Math.cos(virtualTargetYawRad);
    double sinYaw = Math.sin(virtualTargetYawRad);
    double targetFrameVx = vx * cosYaw + vy * sinYaw;

    double hoodFF = targetFrameVx * -tz / (distanceToTargetSq + tz * tz);

    setpoint.robotAimYawRadians = virtualTargetYawRad;
    setpoint.chassisAngularFeedforward = chassisAngularFF;
    setpoint.hoodRadians = pitchAngleRads;
    setpoint.hoodFeedforward = hoodFF;
    setpoint.launchSpeedMetersPerSec = adjustedVShot;
    setpoint.isValid = true;

    return setpoint;
  }
}
