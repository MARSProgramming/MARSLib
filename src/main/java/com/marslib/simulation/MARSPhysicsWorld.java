/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.simulation;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.wpilibj.simulation.BatterySim;
import edu.wpi.first.wpilibj.simulation.RoboRioSim;
import java.util.HashMap;
import java.util.Map;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;
import org.littletonrobotics.junction.Logger;

/**
 * Singleton 2D physics world managing all rigid-body interactions on the FRC field.
 *
 * <p>This class acts as a native Dyn4j physics registry and is responsible for:
 *
 * <ul>
 *   <li>Initializing the field boundaries and obstacles via SimulatedField2026.
 *   <li>Stepping the physics simulation and computing battery voltage sag.
 *   <li>Exporting all body poses to AdvantageKit for 3D visualization.
 * </ul>
 */
public class MARSPhysicsWorld {

  private static MARSPhysicsWorld instance;
  private static int accessCountSinceReset = 0;

  /** Maximum getInstance() calls before a stale-state warning is logged. */
  private static final int STALE_ACCESS_THRESHOLD = 500;

  private static final int STALE_BODY_THRESHOLD = 20;

  public static MARSPhysicsWorld getInstance() {
    if (instance == null) {
      instance = new MARSPhysicsWorld();
      accessCountSinceReset = 0;
    }
    accessCountSinceReset++;
    if (accessCountSinceReset == STALE_ACCESS_THRESHOLD
        && instance.getDyn4jWorld().getBodyCount() > STALE_BODY_THRESHOLD) {
      Logger.recordOutput(
          "PhysicsSim/StaleWarning",
          String.format(
              "WARNING: MARSPhysicsWorld has %d bodies after %d accesses without reset."
                  + " Did you forget MARSPhysicsWorld.resetInstance() in @BeforeEach?",
              instance.getDyn4jWorld().getBodyCount(), accessCountSinceReset));
    }
    return instance;
  }

  @SuppressWarnings("PMD.NullAssignment")
  public static void resetInstance() {
    instance = null;
    accessCountSinceReset = 0;
  }

  public World<Body> getDyn4jWorld() {
    return physicsWorld;
  }

  public int getBodyCount() {
    return physicsWorld.getBodyCount();
  }

  private final World<Body> physicsWorld;
  private final Map<String, Body> mechanismBodies;
  private final java.util.List<SimulationProjectile> projectiles = new java.util.ArrayList<>();

  private double frameCurrentDrawAmps = 0.0;
  private double simulatedVoltage = 12.0;

  @SuppressWarnings("PMD.AssignmentToNonFinalStatic")
  private MARSPhysicsWorld() {
    instance = this;

    physicsWorld = new World<>();
    // Top-down 2D simulation has zero gravity
    physicsWorld.setGravity(new Vector2(0.0, 0.0));

    mechanismBodies = new HashMap<>();

    // Populate the field with static game boundaries
    SimulatedField2026.getFieldBoundaries(true).forEach(physicsWorld::addBody);

    // Auto-populate the dynamic fuel colliders (efficiency mode true to save test compute)
    SimulatedField2026.getFuelBodies(true).forEach(physicsWorld::addBody);
  }

  public World<Body> getWorld() {
    return getDyn4jWorld();
  }

  public void registerMechanismBody(String name, Body body) {
    mechanismBodies.put(name, body);
    physicsWorld.addBody(body);
  }

  public void addProjectile(SimulationProjectile p) {
    projectiles.add(p);
  }

  public Body getOverlappingFuel(edu.wpi.first.math.geometry.Translation2d center, double radius) {
    org.dyn4j.geometry.Vector2 vCenter =
        new org.dyn4j.geometry.Vector2(center.getX(), center.getY());
    for (int i = 0; i < physicsWorld.getBodyCount(); i++) {
      Body body = physicsWorld.getBody(i);
      if ("Fuel".equals(body.getUserData())) {
        double distSq =
            Math.pow(body.getTransform().getTranslationX() - vCenter.x, 2)
                + Math.pow(body.getTransform().getTranslationY() - vCenter.y, 2);
        if (distSq <= radius * radius) {
          return body;
        }
      }
    }
    return null;
  }

  public void removeFuel(Body fuel) {
    physicsWorld.removeBody(fuel);
  }

  public void addFuel(Body fuel) {
    physicsWorld.addBody(fuel);
  }

  public void addFrameCurrentDrawAmps(double amps) {
    frameCurrentDrawAmps += amps;
  }

  public void update(double dtSeconds) {
    // Step the dyn4j environment
    physicsWorld.step(1, dtSeconds);

    // Update 3D Projectiles (2.5D logic)
    java.util.List<SimulationProjectile> spawnedProjectiles = new java.util.ArrayList<>();
    java.util.Iterator<SimulationProjectile> iter = projectiles.iterator();
    while (iter.hasNext()) {
      SimulationProjectile p = iter.next();
      p.update(dtSeconds);

      // Check for scoring first
      if (SimulatedHub2026.checkScoredBlue(p)) {
        Logger.recordOutput("PhysicsWorld/ScoringEvents", "Blue Scored!");
        iter.remove();
        // Spray a replacement downwards
        spawnedProjectiles.add(
            SimulatedHub2026.generatePostScoreProjectile(SimulatedHub2026.BLUE_HUB_POSE));
        continue; // handled, skip grounding check
      } else if (SimulatedHub2026.checkScoredRed(p)) {
        Logger.recordOutput("PhysicsWorld/ScoringEvents", "Red Scored!");
        iter.remove();
        spawnedProjectiles.add(
            SimulatedHub2026.generatePostScoreProjectile(SimulatedHub2026.RED_HUB_POSE));
        continue;
      }

      // Check for grounding
      if (p.isGrounded()) {
        iter.remove();
        // Spawns a physical dyn4j body where it landed
        Body newFuel = SimulatedField2026.createFuel(p.getPose3d().getX(), p.getPose3d().getY());
        // Transfer xy velocity for sliding
        newFuel.setLinearVelocity(p.getVelocityX(), p.getVelocityY());
        physicsWorld.addBody(newFuel);
      }
    }
    projectiles.addAll(spawnedProjectiles);

    // Compute battery voltage sag
    Logger.recordOutput("PhysicsWorld/FrameCurrentDraw_A", frameCurrentDrawAmps);
    double loadedVoltage = BatterySim.calculateDefaultBatteryLoadedVoltage(frameCurrentDrawAmps);
    loadedVoltage = Math.max(6.0, loadedVoltage);
    simulatedVoltage = loadedVoltage;
    RoboRioSim.setVInVoltage(loadedVoltage);

    Logger.recordOutput("PhysicsWorld/ComputedVoltage", loadedVoltage);
    Logger.recordOutput("PhysicsWorld/Heartbeat", Logger.getTimestamp() / 1e6);

    frameCurrentDrawAmps = 0.0;

    exportToAdvantageScope();
  }

  public double getTerrainZHeight(
      edu.wpi.first.math.geometry.Translation2d point, String terrainId) {
    org.dyn4j.geometry.Vector2 vPoint = new org.dyn4j.geometry.Vector2(point.getX(), point.getY());
    for (int i = 0; i < physicsWorld.getBodyCount(); i++) {
      Body body = physicsWorld.getBody(i);
      if (terrainId.equals(body.getUserData())) {
        if (body.contains(vPoint)) {
          org.dyn4j.geometry.AABB aabb = body.createAABB();
          // Distance to nearest edge
          double distLeft = vPoint.x - aabb.getMinX();
          double distRight = aabb.getMaxX() - vPoint.x;
          double distBottom = vPoint.y - aabb.getMinY();
          double distTop = aabb.getMaxY() - vPoint.y;

          double distEdge = Math.min(Math.min(distLeft, distRight), Math.min(distBottom, distTop));

          // 15 degree slope rising from the edges, capping at 6.5 inches MAX
          double slopeHeightMeters = distEdge * Math.tan(Math.toRadians(15.0));
          return Math.min(slopeHeightMeters, edu.wpi.first.math.util.Units.inchesToMeters(6.5));
        }
      }
    }
    return 0.0;
  }

  public double getSimulatedVoltage() {
    return simulatedVoltage;
  }

  private void exportToAdvantageScope() {
    // Export named mechanism bodies
    for (Map.Entry<String, Body> entry : mechanismBodies.entrySet()) {
      String mechanismName = entry.getKey();
      Body body = entry.getValue();

      double xMeters = body.getTransform().getTranslationX();
      double yMeters = body.getTransform().getTranslationY();
      double yawRads = body.getTransform().getRotationAngle();

      Pose3d pose3d = new Pose3d(xMeters, yMeters, 0.0, new Rotation3d(0.0, 0.0, yawRads));
      Logger.recordOutput("PhysicsWorld/" + mechanismName, pose3d);
    }

    // Export fuel balls to AdvantageScope (they have user data "Fuel" set by SimulatedField2026)
    java.util.List<Pose3d> fuelPoses = new java.util.ArrayList<>();
    for (int i = 0; i < physicsWorld.getBodyCount(); i++) {
      Body dynBody = physicsWorld.getBody(i);
      if ("Fuel".equals(dynBody.getUserData())) {
        double px = dynBody.getTransform().getTranslationX();
        double py = dynBody.getTransform().getTranslationY();
        fuelPoses.add(new Pose3d(px, py, 0.075, new Rotation3d())); // Fuel lies 7.5cm above ground
      }
    }

    // Concat active projectiles into the visual stream
    for (SimulationProjectile p : projectiles) {
      fuelPoses.add(p.getPose3d());
    }

    Logger.recordOutput("PhysicsWorld/GamePieces", fuelPoses.toArray(new Pose3d[0]));
  }
}
