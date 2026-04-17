package com.marslib.simulation;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import org.junit.jupiter.api.Test;

/** Targeted coverage tests for SimulatedHub2026's scoring detection and post-score generation. */
public class SimulatedHub2026CoverageTest {

  @Test
  public void testCheckScoredBlueInside() {
    SimulationProjectile inside =
        new SimulationProjectile(
            new Pose3d(SimulatedHub2026.BLUE_HUB_POSE, new Rotation3d()), 0, 0, 0);
    assertTrue(SimulatedHub2026.checkScoredBlue(inside), "Projectile at blue hub should score");
  }

  @Test
  public void testCheckScoredBlueOutside() {
    SimulationProjectile outside =
        new SimulationProjectile(new Pose3d(0, 0, 0, new Rotation3d()), 0, 0, 0);
    assertFalse(
        SimulatedHub2026.checkScoredBlue(outside), "Projectile far from blue hub should not score");
  }

  @Test
  public void testCheckScoredRedInside() {
    SimulationProjectile inside =
        new SimulationProjectile(
            new Pose3d(SimulatedHub2026.RED_HUB_POSE, new Rotation3d()), 0, 0, 0);
    assertTrue(SimulatedHub2026.checkScoredRed(inside), "Projectile at red hub should score");
  }

  @Test
  public void testCheckScoredRedOutside() {
    SimulationProjectile outside =
        new SimulationProjectile(new Pose3d(0, 0, 0, new Rotation3d()), 0, 0, 0);
    assertFalse(
        SimulatedHub2026.checkScoredRed(outside), "Projectile far from red hub should not score");
  }

  @Test
  public void testGeneratePostScoreProjectile() {
    SimulationProjectile sprayed =
        SimulatedHub2026.generatePostScoreProjectile(SimulatedHub2026.BLUE_HUB_POSE);

    assertNotNull(sprayed, "Post-score projectile must not be null");
    // Verify it starts below the hub
    assertTrue(
        sprayed.getPose3d().getZ() < SimulatedHub2026.BLUE_HUB_POSE.getZ(),
        "Post-score projectile should start below hub height");
    // Verify it has downward Z velocity
    assertEquals(-1.0, sprayed.getVelocityZ(), 0.001, "Post-score projectile should fall");
  }

  @Test
  public void testGeneratePostScoreProjectileRedHub() {
    SimulationProjectile sprayed =
        SimulatedHub2026.generatePostScoreProjectile(SimulatedHub2026.RED_HUB_POSE);

    assertNotNull(sprayed);
    assertTrue(
        sprayed.getPose3d().getZ() < SimulatedHub2026.RED_HUB_POSE.getZ(),
        "Red hub post-score should start below hub");
  }
}
