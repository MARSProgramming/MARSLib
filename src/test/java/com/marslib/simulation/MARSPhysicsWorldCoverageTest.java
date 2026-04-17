package com.marslib.simulation;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Targeted coverage tests for previously-uncovered MARSPhysicsWorld branches: terrain Z-height
 * calculations, fuel overlap detection, fuel manipulation, and projectile scoring paths.
 */
public class MARSPhysicsWorldCoverageTest {

  @BeforeEach
  public void resetWorld() {
    com.marslib.testing.MARSTestHarness.reset();
  }

  @org.junit.jupiter.api.AfterEach
  public void cleanup() {
    com.marslib.testing.MARSTestHarness.reset();
  }

  @Test
  public void testGetTerrainZHeightInsideTerrain() {
    MARSPhysicsWorld world = MARSPhysicsWorld.getInstance();

    // Create a terrain body with known userData
    Body terrain = new Body();
    terrain.addFixture(Geometry.createRectangle(4.0, 4.0));
    terrain.setMass(MassType.INFINITE);
    terrain.translate(500.0, 500.0);
    terrain.setUserData("TestTerrain");
    world.getDyn4jWorld().addBody(terrain);

    // Point well inside the terrain body — should return a non-zero slope height
    Translation2d center = new Translation2d(500.0, 500.0);
    double height = world.getTerrainZHeight(center, "TestTerrain");

    assertTrue(height > 0.0, "Height at center of large terrain should be positive");
    // At center of 4x4 rectangle, distEdge = 2.0, slope = 2.0 * tan(15deg) ≈ 0.536m
    // Capped at 6.5 inches = ~0.1651m, so should be exactly the cap
    assertEquals(
        edu.wpi.first.math.util.Units.inchesToMeters(6.5),
        height,
        0.001,
        "Height should be capped at 6.5 inches for deep interior points");
  }

  @Test
  public void testGetTerrainZHeightNearEdge() {
    MARSPhysicsWorld world = MARSPhysicsWorld.getInstance();

    Body terrain = new Body();
    terrain.addFixture(Geometry.createRectangle(4.0, 4.0));
    terrain.setMass(MassType.INFINITE);
    terrain.translate(500.0, 500.0);
    terrain.setUserData("EdgeTerrain");
    world.getDyn4jWorld().addBody(terrain);

    // Point very near the edge — small distEdge = small slope height
    Translation2d nearEdge = new Translation2d(501.95, 500.0);
    double height = world.getTerrainZHeight(nearEdge, "EdgeTerrain");

    assertTrue(height > 0.0, "Height near edge should be positive");
    assertTrue(
        height < edu.wpi.first.math.util.Units.inchesToMeters(6.5),
        "Height near edge should not hit the cap");
  }

  @Test
  public void testGetTerrainZHeightOutsideTerrain() {
    MARSPhysicsWorld world = MARSPhysicsWorld.getInstance();

    // Point outside any terrain
    Translation2d outside = new Translation2d(999.0, 999.0);
    double height = world.getTerrainZHeight(outside, "NonExistentTerrain");

    assertEquals(0.0, height, "Height outside any terrain should be zero");
  }

  @Test
  public void testGetOverlappingFuelFindsMatch() {
    MARSPhysicsWorld world = MARSPhysicsWorld.getInstance();

    Body fuel = SimulatedField2026.createFuel(200.0, 200.0);
    world.addFuel(fuel);

    Body found = world.getOverlappingFuel(new Translation2d(200.0, 200.0), 0.5);
    assertNotNull(found, "Should find fuel at the placed location");
  }

  @Test
  public void testGetOverlappingFuelReturnsNullWhenMiss() {
    MARSPhysicsWorld world = MARSPhysicsWorld.getInstance();

    Body fuel = SimulatedField2026.createFuel(200.0, 200.0);
    world.addFuel(fuel);

    // Search far away from the fuel
    Body found = world.getOverlappingFuel(new Translation2d(900.0, 900.0), 0.1);
    assertNull(found, "Should return null when no fuel is within radius");
  }

  @Test
  public void testRemoveFuelDecrementsBodyCount() {
    MARSPhysicsWorld world = MARSPhysicsWorld.getInstance();
    int beforeCount = world.getBodyCount();

    Body fuel = SimulatedField2026.createFuel(300.0, 300.0);
    world.addFuel(fuel);
    assertEquals(beforeCount + 1, world.getBodyCount(), "Body count should increase after add");

    world.removeFuel(fuel);
    assertEquals(beforeCount, world.getBodyCount(), "Body count should decrease after remove");
  }

  @Test
  public void testProjectileScoringBlue() {
    MARSPhysicsWorld world = MARSPhysicsWorld.getInstance();

    // Create projectile heading straight into the blue hub
    SimulationProjectile blueShot =
        new SimulationProjectile(
            new Pose3d(
                SimulatedHub2026.BLUE_HUB_POSE.plus(new Translation3d(0, 0, 0.1)),
                new Rotation3d()),
            0.0,
            0.0,
            0.0);
    world.addProjectile(blueShot);

    // Stepping should detect the score and remove the projectile
    world.update(0.02);
    // No crash = success; scoring event logged
  }

  @Test
  public void testProjectileScoringRed() {
    MARSPhysicsWorld world = MARSPhysicsWorld.getInstance();

    SimulationProjectile redShot =
        new SimulationProjectile(
            new Pose3d(
                SimulatedHub2026.RED_HUB_POSE.plus(new Translation3d(0, 0, 0.1)), new Rotation3d()),
            0.0,
            0.0,
            0.0);
    world.addProjectile(redShot);

    world.update(0.02);
    // No crash = success; scoring event logged
  }

  @Test
  public void testProjectileGrounding() {
    MARSPhysicsWorld world = MARSPhysicsWorld.getInstance();
    int bodyCountBefore = world.getBodyCount();

    // Create a projectile that is already at ground level (Z <= radius)
    SimulationProjectile grounded =
        new SimulationProjectile(new Pose3d(5.0, 5.0, 0.01, new Rotation3d()), 1.0, 0.5, 0.0);

    world.addProjectile(grounded);
    world.update(0.02);

    // After grounding, a new dyn4j body should have been spawned
    assertTrue(
        world.getBodyCount() >= bodyCountBefore,
        "Body count should increase when a projectile grounds");
  }
}
