package com.marslib;

import com.marslib.simulation.MARSPhysicsWorld;
import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.Test;

public class TestArena {
  @Test
  public void testArenaInitialization() {
    HAL.initialize(500, 0);
    MARSPhysicsWorld.resetInstance();
    MARSPhysicsWorld world = MARSPhysicsWorld.getInstance();

    System.out.println("Bodies before step: " + world.getBodyCount());

    for (int i = 0; i < 50; i++) {
      world.update(0.02); // Step dyn4j world 20ms
    }

    System.out.println("Bodies after 50 steps: " + world.getBodyCount());
  }
}
