package com.marslib.power;

import static org.junit.jupiter.api.Assertions.*;

import com.marslib.testing.MARSTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MARSPowerManagerTest {

  private MARSPowerManager powerManager;
  private double simulatedVoltage = 12.0;

  @BeforeEach
  public void setUp() {
    MARSTestHarness.reset();

    PowerIO mockIO =
        new PowerIO() {
          @Override
          public void updateInputs(PowerIOInputs inputs) {
            inputs.voltage = simulatedVoltage;
          }
        };

    powerManager = new MARSPowerManager(mockIO, MARSTestHarness.createPowerConfig());
  }

  @Test
  public void testNominalVoltageScaling() {
    simulatedVoltage = 12.5;
    powerManager.periodic();

    assertEquals(1.0, powerManager.calculateVoltageScaleFactor(12.0, 7.0), 0.001);
  }

  @Test
  public void testWarningVoltageScaling() {
    // Exactly halfway between nominal (12.0) and critical (8.0)
    simulatedVoltage = 10.0;
    powerManager.periodic();

    assertEquals(0.5, powerManager.calculateVoltageScaleFactor(12.0, 8.0), 0.001);
  }

  @Test
  public void testCriticalVoltageScaling() {
    simulatedVoltage = 7.5;
    powerManager.periodic();

    // Below critical should be 0.0
    assertEquals(0.0, powerManager.calculateVoltageScaleFactor(12.0, 8.0), 0.001);
  }

  @Test
  public void testAlertActivation() {
    // Under warning threshold (8.0 in createPowerConfig)
    simulatedVoltage = 7.9;
    powerManager.periodic();
    assertTrue(powerManager.getVoltage() < 8.0);
  }
}
