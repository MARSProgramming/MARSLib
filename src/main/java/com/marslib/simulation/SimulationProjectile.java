package com.marslib.simulation;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation3d;

/**
 * A lightweight object tracking the 3D kinematics of a launched game piece outside the 2D dyn4j
 * boundaries.
 *
 * <p>Uses the 2.5D illusion where purely mathematical gravity and velocity vectors govern flight,
 * allowing AdvantageScope to visualize realistic 3D trajectories until the piece hits the ground
 * and is converted back into a dyn4j rigid body.
 */
public class SimulationProjectile {
  private static final double GRAVITY_M_PER_S2 = -9.81;
  private static final double PIECE_RADIUS_METERS = 0.075; // 7.5cm fuel radius

  private Translation3d currentPosition;
  private Rotation3d currentRotation;
  private double velocityX;
  private double velocityY;
  private double velocityZ;

  /**
   * Constructs a new SimulationProjectile.
   *
   * @param initialPose The initial 3D pose of the projectile.
   * @param velocityX The initial X-axis velocity in meters per second.
   * @param velocityY The initial Y-axis velocity in meters per second.
   * @param velocityZ The initial Z-axis velocity in meters per second.
   */
  public SimulationProjectile(
      Pose3d initialPose, double velocityX, double velocityY, double velocityZ) {
    this.currentPosition = initialPose.getTranslation();
    this.currentRotation = initialPose.getRotation();
    this.velocityX = velocityX;
    this.velocityY = velocityY;
    this.velocityZ = velocityZ;
  }

  /**
   * Progresses the kinematics of the projectile by the time step.
   *
   * @param dtSeconds The time elapsed in seconds.
   */
  public void update(double dtSeconds) {
    // Integrate Z velocity with gravity
    velocityZ += GRAVITY_M_PER_S2 * dtSeconds;

    // Linearly project position based on velocities
    double newX = currentPosition.getX() + (velocityX * dtSeconds);
    double newY = currentPosition.getY() + (velocityY * dtSeconds);
    double newZ = currentPosition.getZ() + (velocityZ * dtSeconds);

    currentPosition = new Translation3d(newX, newY, newZ);
  }

  /**
   * Determines if the projectile has fallen and hit the ground playing surface.
   *
   * @return true if the piece has reached or sunk below its radius above Z=0.
   */
  public boolean isGrounded() {
    return currentPosition.getZ() <= PIECE_RADIUS_METERS;
  }

  /**
   * Gets the active 3D pose of this projectile for visualization or distance checks.
   *
   * @return A Pose3d representing the current location.
   */
  public Pose3d getPose3d() {
    return new Pose3d(currentPosition, currentRotation);
  }

  public double getVelocityX() {
    return velocityX;
  }

  public double getVelocityY() {
    return velocityY;
  }

  public double getVelocityZ() {
    return velocityZ;
  }
}
