package com.marslib.util;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import org.junit.jupiter.api.Test;

public class EliteShooterMathTest {

  @Test
  public void testStaticShot() {
    EliteShooterMath.EliteShooterSetpoint setpoint = new EliteShooterMath.EliteShooterSetpoint();

    // Robot at (0,0), Target at (5,0,2)
    Pose2d robotPose = new Pose2d();
    Translation3d target = new Translation3d(5.0, 0.0, 2.0);
    ChassisSpeeds speeds = new ChassisSpeeds(); // Static

    // Fire with 15 m/s, release height 0.5m
    EliteShooterMath.calculateShotOnTheMove(
        robotPose, speeds, target, 0.5, 15.0, -9.81, 0.0, setpoint);

    assertTrue(setpoint.isValid);
    assertEquals(0.0, setpoint.robotAimYawRadians, 0.01);
    assertTrue(setpoint.hoodRadians > 0);
  }

  @Test
  public void testMovingShot() {
    EliteShooterMath.EliteShooterSetpoint setpoint = new EliteShooterMath.EliteShooterSetpoint();

    Pose2d robotPose = new Pose2d();
    Translation3d target = new Translation3d(5.0, 0.0, 2.0);
    // Robot strafing Y at 2 m/s relative to field
    ChassisSpeeds speeds = new ChassisSpeeds(0.0, 2.0, 0.0);

    EliteShooterMath.calculateShotOnTheMove(
        robotPose, speeds, target, 0.5, 15.0, -9.81, 0.0, setpoint);

    assertTrue(setpoint.isValid);
    // Since we are moving positive Y, we need to aim negative Y (into the strafe)
    assertTrue(setpoint.robotAimYawRadians < 0.0);
  }

  @Test
  public void testEpsilonQuadratic() {
    EliteShooterMath.EliteShooterSetpoint setpoint = new EliteShooterMath.EliteShooterSetpoint();

    Pose2d robotPose = new Pose2d();
    Translation3d target = new Translation3d(10.0, 0.0, 2.0);
    // If vx^2 + vy^2 == vShot^2, a = 0. We trigger epsilon protection
    ChassisSpeeds speeds = new ChassisSpeeds(15.0, 0.0, 0.0);

    EliteShooterMath.calculateShotOnTheMove(
        robotPose, speeds, target, 0.5, 15.0, -9.81, 0.0, setpoint);

    assertTrue(setpoint.isValid);
  }

  @Test
  public void testImpossibleShot() {
    EliteShooterMath.EliteShooterSetpoint setpoint = new EliteShooterMath.EliteShooterSetpoint();

    Pose2d robotPose = new Pose2d();
    Translation3d target = new Translation3d(100.0, 0.0, 10.0);
    ChassisSpeeds speeds = new ChassisSpeeds();

    // Shoot very slow against high gravity, it will be physically impossible -> t <= 0 or imaginary
    // root handled
    EliteShooterMath.calculateShotOnTheMove(
        robotPose, speeds, target, 0.5, 1.0, -9.81, 0.0, setpoint);

    assertFalse(setpoint.isValid);
  }
}
