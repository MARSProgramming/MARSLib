package com.marslib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    try (TalonFX motor = new TalonFX(0)) {
      TalonFXConfiguration config = new TalonFXConfiguration();

      // Attempt configuration application
      StatusCode status = CANUtil.applyWithRetry(motor, config, "TestMotor");
      assertEquals(StatusCode.OK, status);
    }
  }

  @Test
  void testSetUpdateFrequencyWithRetry() {
    try (TalonFX motor = new TalonFX(0)) {
      // Attempt signal frequency update
      StatusCode status = CANUtil.setUpdateFrequencyWithRetry(50.0, motor.getPosition());
      assertEquals(StatusCode.OK, status);
    }
  }

  @Test
  void testUnsupportedConfigType() {
    try (TalonFX motor = new TalonFX(0)) {
      Object invalidConfig = new Object();
      IllegalArgumentException ex =
          assertThrows(
              IllegalArgumentException.class,
              () -> CANUtil.applyWithRetry(motor, invalidConfig, "TestFail"));
      assertTrue(ex.getMessage().contains("Unsupported config type"));
    }
  }

  @Test
  public void testSetUpdateFrequencyWithRetryTimeout() {
    try (org.mockito.MockedStatic<com.ctre.phoenix6.BaseStatusSignal> mockedSignal =
        org.mockito.Mockito.mockStatic(com.ctre.phoenix6.BaseStatusSignal.class)) {
      mockedSignal
          .when(
              () ->
                  com.ctre.phoenix6.BaseStatusSignal.setUpdateFrequencyForAll(
                      org.mockito.ArgumentMatchers.anyDouble(),
                      org.mockito.ArgumentMatchers.any(com.ctre.phoenix6.BaseStatusSignal[].class)))
          .thenReturn(StatusCode.GeneralError);

      try (TalonFX motor = new TalonFX(0)) {
        StatusCode status = CANUtil.setUpdateFrequencyWithRetry(50.0, motor.getPosition());
        assertEquals(StatusCode.GeneralError, status);
      }
    }
  }

  @Test
  public void testApplyWithRetryTimeoutUsingMock() {
    TalonFX mockMotor = org.mockito.Mockito.mock(TalonFX.class);
    com.ctre.phoenix6.configs.TalonFXConfigurator mockConfigurator =
        org.mockito.Mockito.mock(com.ctre.phoenix6.configs.TalonFXConfigurator.class);
    org.mockito.Mockito.when(mockMotor.getConfigurator()).thenReturn(mockConfigurator);
    org.mockito.Mockito.when(
            mockConfigurator.apply(org.mockito.ArgumentMatchers.any(TalonFXConfiguration.class)))
        .thenReturn(StatusCode.GeneralError);

    TalonFXConfiguration config = new TalonFXConfiguration();
    StatusCode status = CANUtil.applyWithRetry(mockMotor, config, "TestFailMock");
    assertEquals(StatusCode.GeneralError, status);
  }

  @Test
  void testApplyPartialConfigs() {
    try (TalonFX motor = new TalonFX(0)) {
      com.ctre.phoenix6.configs.Slot0Configs slot0 = new com.ctre.phoenix6.configs.Slot0Configs();
      com.ctre.phoenix6.configs.MotorOutputConfigs motorOutput =
          new com.ctre.phoenix6.configs.MotorOutputConfigs();
      com.ctre.phoenix6.configs.CurrentLimitsConfigs currentLimits =
          new com.ctre.phoenix6.configs.CurrentLimitsConfigs();

      assertEquals(StatusCode.OK, CANUtil.applyWithRetry(motor, slot0, "TestSlot0"));
      assertEquals(StatusCode.OK, CANUtil.applyWithRetry(motor, motorOutput, "TestMotorOutput"));
      assertEquals(
          StatusCode.OK, CANUtil.applyWithRetry(motor, currentLimits, "TestCurrentLimits"));
    }
  }
}
