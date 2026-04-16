package com.marslib.faults;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.marslib.testing.MARSTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AlertTest {

  @BeforeEach
  void setUp() {
    MARSTestHarness.reset();
    Alert.resetAll();
  }

  @Test
  void testAlertActivationAndDeactivation() {
    Alert alert = new Alert("Test Group", "Test Alert", Alert.AlertType.INFO);
    assertFalse(alert.get());

    alert.set(true);
    assertTrue(alert.get());

    alert.set(false);
    assertFalse(alert.get());
  }

  @Test
  void testAlertCriticalFaultRegistration() {
    Alert alert = new Alert("Test Critical", Alert.AlertType.CRITICAL);

    alert.set(true);
    assertTrue(MARSFaultManager.hasActiveCriticalFaults());

    alert.set(false);
    assertFalse(MARSFaultManager.hasActiveCriticalFaults());
  }

  @Test
  void testAlertSetText() {
    Alert alert = new Alert("Before", Alert.AlertType.WARNING);
    alert.set(true);
    alert.setText("After");
    assertTrue(alert.get());
  }
}
