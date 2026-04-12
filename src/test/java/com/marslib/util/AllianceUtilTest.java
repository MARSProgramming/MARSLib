package com.marslib.util;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AllianceUtilTest {

  @BeforeEach
  public void setUp() {
    HAL.initialize(500, 0);
    DriverStationSim.setAllianceStationId(edu.wpi.first.hal.AllianceStationID.Unknown);
    DriverStationSim.notifyNewData();
  }

  @Test
  public void testIsRed() {
    assertFalse(AllianceUtil.isRed(), "Should be false when unknown");

    DriverStationSim.setAllianceStationId(edu.wpi.first.hal.AllianceStationID.Red1);
    DriverStationSim.notifyNewData();
    assertTrue(AllianceUtil.isRed(), "Should be true when Red");
    assertFalse(AllianceUtil.isBlue(), "Should be false when Red");
  }

  @Test
  public void testIsBlue() {
    assertFalse(AllianceUtil.isBlue(), "Should be false when unknown");

    DriverStationSim.setAllianceStationId(edu.wpi.first.hal.AllianceStationID.Blue1);
    DriverStationSim.notifyNewData();
    assertTrue(AllianceUtil.isBlue(), "Should be true when Blue");
    assertFalse(AllianceUtil.isRed(), "Should be false when Blue");
  }
}
