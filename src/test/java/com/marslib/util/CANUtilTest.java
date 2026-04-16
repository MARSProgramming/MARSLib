package com.marslib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.hardware.TalonFX;
import com.marslib.testing.MARSTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CANUtilTest {

  @BeforeEach
  void setUp() {
    MARSTestHarness.reset();
  }

  @Test
  void testApplyWithRetry() {
    try (TalonFX motor = new TalonFX(0, "rio")) {
      TalonFXConfiguration config = new TalonFXConfiguration();

      // Attempt configuration application
      StatusCode status = CANUtil.applyWithRetry(motor, config, "TestMotor");
      assertEquals(StatusCode.OK, status);
    }
  }

  @Test
  void testSetUpdateFrequencyWithRetry() {
    try (TalonFX motor = new TalonFX(0, "rio")) {
      // Attempt signal frequency update
      StatusCode status = CANUtil.setUpdateFrequencyWithRetry(50.0, motor.getPosition());
      assertEquals(StatusCode.OK, status);
    }
  }
}
