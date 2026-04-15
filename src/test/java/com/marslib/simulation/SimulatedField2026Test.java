package com.marslib.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.MassType;
import org.junit.jupiter.api.Test;

public class SimulatedField2026Test {

  @Test
  public void testFieldBoundaries() {
    List<Body> boundariesWithRamps = SimulatedField2026.getFieldBoundaries(true);
    // 4 walls, 2 blue uprights, 2 red uprights, 2 blue trench, 2 red trench, 2 hubs = 14
    assertEquals(14, boundariesWithRamps.size());
    for (Body b : boundariesWithRamps) {
      assertEquals(MassType.INFINITE, b.getMass().getType());
    }
  }

  @Test
  public void testFuelBodiesGeneration() {
    // Test efficiencyMode = false
    List<Body> fullFuelBodies = SimulatedField2026.getFuelBodies(false);

    // 12 * 30 center + 4 * 6 blue depot + 4 * 6 red depot
    assertEquals(408, fullFuelBodies.size(), "Should spawn all 408 full-grid fuel bodies");

    for (Body b : fullFuelBodies) {
      assertEquals(MassType.NORMAL, b.getMass().getType());
      assertEquals("Fuel", b.getUserData());
    }

    // Test efficiencyMode = true
    List<Body> culledFuelBodies = SimulatedField2026.getFuelBodies(true);
    assertEquals(
        168,
        culledFuelBodies.size(),
        "Should cull center floor balls slightly to optimize performance");
  }
}
