package com.marslib.util;

import static org.junit.jupiter.api.Assertions.*;

import com.marslib.testing.MARSTestHarness;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoggedTunableNumberTest {

  private final List<LoggedTunableNumber> activeTunables = new ArrayList<>();

  @BeforeEach
  public void setUp() {
    MARSTestHarness.reset();
    activeTunables.clear();
    LoggedTunableNumber.clear();
    DriverStationSim.setFmsAttached(false);
    DriverStationSim.notifyNewData();
  }

  @AfterEach
  public void tearDown() {
    for (LoggedTunableNumber tunable : activeTunables) {
      tunable.close();
    }
    LoggedTunableNumber.clear();
    MARSTestHarness.cleanup();
  }

  private LoggedTunableNumber createTunable(String key, double defaultValue) {
    LoggedTunableNumber t = new LoggedTunableNumber(key, defaultValue);
    activeTunables.add(t);
    return t;
  }

  @Test
  public void testBasicFunctionality() {
    LoggedTunableNumber tunable = createTunable("TestKey", 1.23);
    assertEquals(1.23, tunable.get(), 0.001);

    // Simulate NT update
    NetworkTableInstance.getDefault()
        .getTable("TunableNumbers")
        .getEntry("TestKey")
        .setDouble(4.56);
    NetworkTableInstance.getDefault().flush();
    try {
      Thread.sleep(50);
    } catch (InterruptedException ignored) {
    }

    assertEquals(4.56, tunable.get(), 0.001);
  }

  @Test
  public void testHasChangedTracking() {
    LoggedTunableNumber tunable = createTunable("ChangeKey", 0.0);
    assertTrue(tunable.hasChanged(1), "Initial check should be true");
    assertFalse(tunable.hasChanged(1), "Subsequent check without change should be false");

    NetworkTableInstance.getDefault()
        .getTable("TunableNumbers")
        .getEntry("ChangeKey")
        .setDouble(10.0);
    NetworkTableInstance.getDefault().flush();
    try {
      Thread.sleep(50);
    } catch (InterruptedException ignored) {
    }

    assertTrue(tunable.hasChanged(1), "Check after update should be true");
    assertTrue(tunable.hasChanged(2), "Initial check for consumer 2 should be true");
  }

  @Test
  public void testFmsLock() {
    LoggedTunableNumber tunable = createTunable("LockKey", 5.0);
    DriverStationSim.setFmsAttached(true);
    DriverStationSim.notifyNewData();

    NetworkTableInstance.getDefault()
        .getTable("TunableNumbers")
        .getEntry("LockKey")
        .setDouble(100.0);
    NetworkTableInstance.getDefault().flush();
    try {
      Thread.sleep(50);
    } catch (InterruptedException ignored) {
    }

    assertEquals(5.0, tunable.get(), "Value should be locked to default when FMS is attached");
    assertFalse(tunable.hasChanged(1), "hasChanged should be false when FMS is attached");
  }

  @Test
  public void testDashboardBuilding() {
    createTunable("Sub/T1", 1);
    createTunable("Sub/T2", 2);
    createTunable("Global", 3);

    assertDoesNotThrow(LoggedTunableNumber::buildTuningDashboard);
  }

  @Test
  public void testDumpCommand() {
    createTunable("DumpMe", 42);
    assertDoesNotThrow(() -> LoggedTunableNumber.getDumpCommand().initialize());
  }
}
