package com.marslib.simulation;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import org.junit.jupiter.api.Test;

/** Targeted coverage test for SimulationProjectile's getVelocityZ() accessor. */
public class SimulationProjectileCoverageTest {

  @Test
  public void testAllVelocityAccessors() {
    SimulationProjectile p =
        new SimulationProjectile(new Pose3d(0, 0, 1.0, new Rotation3d()), 2.5, 3.5, 4.5);

    assertEquals(2.5, p.getVelocityX(), 0.001);
    assertEquals(3.5, p.getVelocityY(), 0.001);
    assertEquals(4.5, p.getVelocityZ(), 0.001);
  }

  @Test
  public void testGravityDecreasesZVelocity() {
    SimulationProjectile p =
        new SimulationProjectile(new Pose3d(0, 0, 5.0, new Rotation3d()), 0, 0, 10.0);

    p.update(0.5); // 0.5s of flight

    // vZ should have decreased by 9.81 * 0.5 = 4.905
    assertEquals(10.0 - 9.81 * 0.5, p.getVelocityZ(), 0.01);
    assertTrue(p.getPose3d().getZ() > 5.0, "Should have risen");
  }

  @Test
  public void testGroundingDetection() {
    // Start just above ground at 0.076m (PIECE_RADIUS = 0.075)
    SimulationProjectile p =
        new SimulationProjectile(new Pose3d(0, 0, 0.076, new Rotation3d()), 0, 0, -1.0);

    assertFalse(p.isGrounded(), "Should not be grounded yet");

    p.update(0.02); // Falls to ~0.076 + (-1.0)(0.02) = 0.056m
    assertTrue(p.isGrounded(), "Should be grounded after falling");
  }
}
