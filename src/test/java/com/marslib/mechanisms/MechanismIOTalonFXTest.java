package com.marslib.mechanisms;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.marslib.testing.MARSTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MechanismIOTalonFXTest {

  @BeforeEach
  public void setUp() {
    MARSTestHarness.reset();
  }

  @Test
  public void testLinearMechanismConfiguresWithoutThrowing() {
    assertDoesNotThrow(
        () -> {
          // Native simulation instantiation
          new LinearMechanismIOTalonFX(
              20, new int[] {21}, new boolean[] {false}, "rio", 10.0, 0.05, false);
        },
        "LinearMechanismIOTalonFX should cleanly configure its CAN devices during simulation");
  }

  @Test
  public void testRotaryMechanismConfiguresWithoutThrowing() {
    assertDoesNotThrow(
        () -> {
          // Rotary IO takes motor, canbus, ratio, inversion natively. (Absolute Encoders are
          // layered ontop in the subsystem)
          new RotaryMechanismIOTalonFX(23, "rio", 10.0, false);
        },
        "RotaryMechanismIOTalonFX should cleanly configure its CAN devices during simulation");
  }

  @Test
  public void testFlywheelConfiguresWithoutThrowing() {
    assertDoesNotThrow(
        () -> {
          new FlywheelIOTalonFX(25, new int[] {26}, new boolean[] {false}, "rio", false);
        },
        "FlywheelIOTalonFX should cleanly configure its CAN devices during simulation");
  }
}
