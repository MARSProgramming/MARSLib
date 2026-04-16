package com.marslib.power;

import static org.junit.jupiter.api.Assertions.*;

import com.marslib.testing.MARSTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MARSPowerManagerTest {
  @BeforeEach
  public void setUp() {
    MARSTestHarness.reset();
  }

  @Test
  public void testPowerManagerAlertsAndScaling() {
    PowerConfig config = new PowerConfig(11.0, 10.0, 8.0);
    MARSPowerManager power = new MARSPowerManager(new PowerIO() {}, config);

    // Default voltage = 0.0, no alerts should be active but voltage < 0 check
    power.periodic();
    assertFalse(power.isWarning());
    assertFalse(power.isCritical());

    // Note: To test the scaleLoadShedding and isWarning/isCritical branches,
    // we would ideally need a mock IO, but PowerIO() {} doesn't let us modify inputs natively
    // unless we create a custom inline class.
    MARSPowerManager powerMock =
        new MARSPowerManager(
            new PowerIO() {
              @Override
              public void updateInputs(PowerIOInputs inputs) {
                inputs.voltage = 9.0;
              }
            },
            config);
    powerMock.periodic();
    assertTrue(powerMock.isWarning());
    assertFalse(powerMock.isCritical());

    assertEquals(9.0, powerMock.getVoltage(), 0.01);
  }

  @Test
  public void testCriticalAlerts() {
    PowerConfig config = new PowerConfig(11.0, 10.0, 8.0);
    MARSPowerManager powerMock =
        new MARSPowerManager(
            new PowerIO() {
              @Override
              public void updateInputs(PowerIOInputs inputs) {
                inputs.voltage = 7.0;
              }
            },
            config);

    powerMock.periodic();
    assertTrue(powerMock.isWarning());
    assertTrue(powerMock.isCritical());
  }
}
