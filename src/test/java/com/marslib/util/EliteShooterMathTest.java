package com.marslib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import org.junit.jupiter.api.Test;

public class EliteShooterMathTest {

  private static final double EPSILON = 1e-3;

  @Test
  public void testStaticShot() {
    Pose2d robotPose = new Pose2d(0, 0, new Rotation2d(0));
    ChassisSpeeds speeds = new ChassisSpeeds(0, 0, 0); // Stationary
    Translation3d target = new Translation3d(10.0, 0.0, 2.0); // 10m away, 2m high

    EliteShooterMath.EliteShooterSetpoint setpoint =
        EliteShooterMath.calculateShotOnTheMove(
            robotPose,
            speeds,
            target,
            0.0,
            20.0,
            -9.81,
            0.0,
            new EliteShooterMath.EliteShooterSetpoint());

    assertTrue(setpoint.isValid, "Shot should be valid");
    assertEquals(0.0, setpoint.robotAimYawRadians, EPSILON, "Yaw should be 0");
    assertEquals(0.0, setpoint.chassisAngularFeedforward, EPSILON, "No yaw movement required");
    assertEquals(0.0, setpoint.hoodFeedforward, EPSILON, "No hood change required");

    // Time of flight: vShot = 20. Distance = sqrt(104) = 10.198. t = 10.198/20 = 0.5099
    // Drop = 0.5 * -9.81 * 0.5099^2 = -1.275
    // Virtual Delta Z = 2.0 - (-1.275) = 3.275
    // XY Vel = 10 / 0.5099 = 19.61
    // pitch = atan2(3.275, 10.0)
    double expectedPitch = Math.atan2(3.275, 10.0);
    assertEquals(expectedPitch, setpoint.hoodRadians, 0.1, "Pitch should match projectile drop");
  }

  @Test
  public void testMovingTowardsTargetHoodFeedforward() {
    Pose2d robotPose = new Pose2d(0, 0, new Rotation2d(0));
    // Robot moving forward (towards target) at 5 m/s
    ChassisSpeeds speeds = new ChassisSpeeds(5.0, 0, 0);
    Translation3d target = new Translation3d(10.0, 0.0, 2.0);

    EliteShooterMath.EliteShooterSetpoint setpoint =
        EliteShooterMath.calculateShotOnTheMove(
            robotPose,
            speeds,
            target,
            0.0,
            20.0,
            -9.81,
            0.0,
            new EliteShooterMath.EliteShooterSetpoint());

    // When moving TOWARDS a high target, you will get closer.
    // The closer you get, the higher you must aim.
    // Therefore, hood feedforward (rate of change of elevation angle) MUST be POSITIVE.
    assertTrue(
        setpoint.hoodFeedforward > 0,
        "Hood Feedforward should be positive when approaching a high target. Value was: "
            + setpoint.hoodFeedforward);
  }

  @Test
  public void testLateralMovementYawFeedforward() {
    Pose2d robotPose = new Pose2d(0, 0, new Rotation2d(0));
    // Robot moving left (positive Y) at 5 m/s. Target is straight ahead.
    ChassisSpeeds speeds = new ChassisSpeeds(0.0, 5.0, 0);
    Translation3d target = new Translation3d(10.0, 0.0, 2.0);

    EliteShooterMath.EliteShooterSetpoint setpoint =
        EliteShooterMath.calculateShotOnTheMove(
            robotPose,
            speeds,
            target,
            0.0,
            20.0,
            -9.81,
            0.0,
            new EliteShooterMath.EliteShooterSetpoint());

    // If moving left (+Y), the target drifts to the right (-Y relative).
    // So robot must yaw negatively to keep tracking.
    assertTrue(
        setpoint.chassisAngularFeedforward < 0,
        "Chassis Angular Feedforward should be negative when moving +Y. Value was: "
            + setpoint.chassisAngularFeedforward);
  }
}
