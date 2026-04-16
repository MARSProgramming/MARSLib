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

  @Test
  void testAlertGroupRecreatedAfterReset() {
    Alert alert1 = new Alert("TestGroup1", "Alert1", Alert.AlertType.INFO);
    Alert alert2 = new Alert("TestGroup2", "Alert2", Alert.AlertType.INFO);

    // Clear the static groups map
    Alert.resetAll();

    // Trigger set() with a missing group key
    alert1.set(true);
    assertTrue(alert1.get());

    // Make alert2 active, then reset, then call setText to trigger line 78
    alert2.set(true);
    Alert.resetAll();
    alert2.setText("Updated");
    assertTrue(alert2.get());
  }
}
