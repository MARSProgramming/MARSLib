/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.simulation;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation3d;
import org.dyn4j.geometry.Ray;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.DetectFilter;
import org.dyn4j.world.World;
import org.dyn4j.world.result.RaycastResult;
import org.littletonrobotics.junction.Logger;

/**
 * Simulates a 360-degree LiDAR or Time-of-Flight sensor array using dyn4j raycasting. Shoots rays
 * outward from the robot chassis to detect walls and game pieces.
 */
public class LidarIOSim {
  private static final int NUM_RAYS = 16;
  private static final double MAX_RAY_DISTANCE_METERS = 3.0; // Max Range

  private static final DetectFilter<org.dyn4j.dynamics.Body, org.dyn4j.dynamics.BodyFixture>
      DETECT_FILTER = new DetectFilter<>(true, true, null);

  // E-03 Fix: Pre-allocated arrays to eliminate per-tick heap allocations
  private final MARSPhysicsWorld physicsWorld;
  private final Vector2 origin = new Vector2();
  private final Vector2[] directions = new Vector2[NUM_RAYS];
  private final Ray[] rays = new Ray[NUM_RAYS];
  private final Translation3d[] hitBuffer = new Translation3d[NUM_RAYS];
  private static final double RENDER_HEIGHT_METERS = 0.2;

  public LidarIOSim() {
    this.physicsWorld = MARSPhysicsWorld.getInstance();

    // Pre-allocate all ray geometry objects once
    for (int i = 0; i < NUM_RAYS; i++) {
      directions[i] = new Vector2(1, 0);
      rays[i] = new Ray(origin, directions[i]);
      hitBuffer[i] = new Translation3d();
    }
  }

  /**
   * Performs the raycast sweep around the robot and logs the hits as a 3D Point Cloud.
   *
   * @param robotPose The current field-relative pose of the robot.
   */
  public void updateInputs(Pose2d robotPose) {
    World<org.dyn4j.dynamics.Body> world = physicsWorld.getDyn4jWorld();

    // Safety check if the user hasn't successfully initialized a world.
    if (world == null) return;

    origin.x = robotPose.getX();
    origin.y = robotPose.getY();

    int hitCount = 0;

    for (int i = 0; i < NUM_RAYS; i++) {
      double angleRad = robotPose.getRotation().getRadians() + (i * ((2 * Math.PI) / NUM_RAYS));
      directions[i].x = Math.cos(angleRad);
      directions[i].y = Math.sin(angleRad);

      // Update the pre-allocated Ray's origin and direction in place
      rays[i].setStart(origin);
      rays[i].setDirection(directions[i]);

      // Perform broad-phase raycast finding the closest object
      RaycastResult<org.dyn4j.dynamics.Body, org.dyn4j.dynamics.BodyFixture> result =
          world.raycastClosest(rays[i], MAX_RAY_DISTANCE_METERS, DETECT_FILTER);

      if (result != null && result.getRaycast().getDistance() > 0.01) {
        // We hit something! Convert the distance back to an absolute field Point3d
        double distance = result.getRaycast().getDistance();
        double hitX = origin.x + (directions[i].x * distance);
        double hitY = origin.y + (directions[i].y * distance);

        hitBuffer[hitCount] =
            new Translation3d(hitX, hitY, RENDER_HEIGHT_METERS); // Render hits at bumper height
        hitCount++;
      }
    }

    // Build correctly-sized output array from pre-allocated buffer
    Translation3d[] pointCloudArray = new Translation3d[hitCount];
    System.arraycopy(hitBuffer, 0, pointCloudArray, 0, hitCount);

    // Log directly to AdvantageKit for 3D View rendering
    Logger.recordOutput("PhysicsSim/LidarPointCloud", pointCloudArray);
  }
}
