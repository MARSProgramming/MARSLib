package com.marslib.mechanisms;

import static org.junit.jupiter.api.Assertions.*;

import com.marslib.swerve.SwerveModuleIOTalonFX;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Coverage instrumentation for hardware-specific IO layers.
 *
 * <p>Note: These tests do not verify hardware behavior, but rather ensure that configuration
 * mapping and telemetry update logic are instrumented for reliability reporting.
 */
public class MechanismIOInstrumentationTest {

  @BeforeEach
  public void setUp() {
    com.marslib.testing.MARSTestHarness.reset();
  }

  @Test
  public void testLinearMechanismIOTalonFX() {
    // We instantiate with a mock ID. In simulation, TalonFX creation is safe.
    assertDoesNotThrow(
        () -> {
          LinearMechanismIOTalonFX io = new LinearMechanismIOTalonFX(10, "rio", 1.0, 0.0, false);
          io.updateInputs(new LinearMechanismIO.LinearMechanismIOInputs());
          io.setVoltage(1.0);
          io.setVoltage(0.0);
        });
  }

  @Test
  public void testRotaryMechanismIOTalonFX() {
    assertDoesNotThrow(
        () -> {
          RotaryMechanismIOTalonFX io = new RotaryMechanismIOTalonFX(11, "rio", 1.0, false);
          io.updateInputs(new RotaryMechanismIO.RotaryMechanismIOInputs());
          io.setVoltage(1.0);
          io.setVoltage(0.0);
        });
  }

  @Test
  public void testSwerveModuleIOTalonFX() {
    assertDoesNotThrow(
        () -> {
          SwerveModuleIOTalonFX io = new SwerveModuleIOTalonFX(12, 13, "rio");
          io.updateInputs(new com.marslib.swerve.SwerveModuleIO.SwerveModuleIOInputs());
          io.setDriveVoltage(1.0);
          io.setTurnVoltage(1.0);
        });
  }
}
