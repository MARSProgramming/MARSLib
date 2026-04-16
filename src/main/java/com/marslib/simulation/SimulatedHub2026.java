package com.marslib.simulation;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation3d;

/**
 * Handles volumetric scoring calculations for the 2026 Rebuilt Hubs. Uses 3D Cartesian distance
 * checks against active projectiles.
 */
public class SimulatedHub2026 {

  public static final Translation3d BLUE_HUB_POSE = new Translation3d(4.5974, 4.034536, 1.5748);
  public static final Translation3d RED_HUB_POSE = new Translation3d(11.938, 4.034536, 1.5748);
  public static final double GOAL_RADIUS_METERS = 0.5969; // ~23.5 inches

  /**
   * Checks if a projectile exists inside the volumetric radius of the Blue Hub.
   *
   * @param projectile The simulation projectile to evaluate.
   * @return True if intersecting the goal volume, triggering a score.
   */
  public static boolean checkScoredBlue(SimulationProjectile projectile) {
    return checkCollision(projectile, BLUE_HUB_POSE);
  }

  /**
   * Checks if a projectile exists inside the volumetric radius of the Red Hub.
   *
   * @param projectile The simulation projectile to evaluate.
   * @return True if intersecting the goal volume, triggering a score.
   */
  public static boolean checkScoredRed(SimulationProjectile projectile) {
    return checkCollision(projectile, RED_HUB_POSE);
  }

  private static boolean checkCollision(SimulationProjectile projectile, Translation3d hubPose) {
    Pose3d piecePose = projectile.getPose3d();
    double distSq =
        Math.pow(piecePose.getX() - hubPose.getX(), 2)
            + Math.pow(piecePose.getY() - hubPose.getY(), 2)
            + Math.pow(piecePose.getZ() - hubPose.getZ(), 2);
    return distSq < Math.pow(GOAL_RADIUS_METERS, 2);
  }

  /**
   * Generates a "spray" projectile to simulate a scored ball re-entering the field organically out
   * the bottom of the hub.
   *
   * @param hubPose The center of the hub where it was scored.
   * @return A new SimulationProjectile flying downward and outward.
   */
  public static SimulationProjectile generatePostScoreProjectile(Translation3d hubPose) {
    // Rebuilt 2026 fuel spits out from underneath the hub somewhat randomly.
    // Base pose is shifted down to about 0.5 meters above ground
    Pose3d startPose = new Pose3d(hubPose.plus(new Translation3d(0, 0, -1.0)), new Rotation3d());

    // Random outward velocity (roughly 1-2 m/s mostly horizontal but slightly downward)
    double angle = Math.random() * 2 * Math.PI;
    double speedXY = 1.0 + Math.random();
    double vx = Math.cos(angle) * speedXY;
    double vy = Math.sin(angle) * speedXY;
    double vz = -1.0; // Pushed straight down

    return new SimulationProjectile(startPose, vx, vy, vz);
  }
}
