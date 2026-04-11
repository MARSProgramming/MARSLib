package com.marslib;

import org.ironmaple.simulation.seasonspecific.rebuilt2026.Arena2026Rebuilt;
import org.junit.jupiter.api.Test;

public class TestArena {
  @Test
  public void testArenaBallCount() {
    Arena2026Rebuilt arena = new Arena2026Rebuilt(true);
    arena.setEfficiencyMode(false);
    arena.resetFieldForAuto();
    System.out.println("Balls before step: " + arena.getGamePiecesPosesByType("Fuel").size());

    for (int i = 0; i < 50; i++) {
      arena.simulationPeriodic(); // Actually steps the dyn4j world
    }

    System.out.println("Balls after 50 steps: " + arena.getGamePiecesPosesByType("Fuel").size());
  }
}
