package com.marslib.power;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class MARSPowerManagerTest {

  @Test
  public void testVoltageScalingDuringBrownouts() {
    // 1. Mock PowerIO to report exact voltages
    PowerIO mockPowerIO =
        new PowerIO() {
          @Override
          public void updateInputs(PowerIOInputs inputs) {
            inputs.voltage = 7.0;
          }
        };
    MARSPowerManager powerManager = new MARSPowerManager(mockPowerIO);

    // 2. Proc updates
    powerManager.periodic();

    // 3. Assert linear scaling between 6.0V (0.0 multiplier) and 10.0V (1.0 multiplier)
    // At 7.0V, it should be exactly 0.25 scaling.
    double scale = powerManager.calculateVoltageScaleFactor(10.0, 6.0);

    assertEquals(0.25, scale, 0.01, "PowerManager failed to scale kinematics perfectly at 7.0V");

    // 4. Test nominal clamps to 1.0
    PowerIO mockNominal =
        new PowerIO() {
          @Override
          public void updateInputs(PowerIOInputs inputs) {
            inputs.voltage = 12.0;
          }
        };
    MARSPowerManager nominalManager = new MARSPowerManager(mockNominal);
    nominalManager.periodic();

    assertEquals(
        1.0,
        nominalManager.calculateVoltageScaleFactor(10.0, 6.0),
        0.01,
        "PowerManager should cap scale at 1.0");
  }
}
