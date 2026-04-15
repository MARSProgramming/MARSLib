/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.util;

import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.hardware.TalonFX;
import com.marslib.faults.MARSFaultManager;

/**
 * CAN bus configuration utility providing retry-guarded motor controller configuration.
 *
 * <p>All TalonFX {@code .apply()} calls MUST be routed through this utility to prevent silent
 * misconfiguration caused by transient CAN bus contention (e.g., during power-on sequencing or
 * heavy bus traffic). If all retries fail, a CRITICAL fault is registered with the {@link
 * MARSFaultManager}.
 *
 * <p><b>Usage:</b>
 *
 * <pre>{@code
 * TalonFXConfiguration config = new TalonFXConfiguration();
 * // ... configure ...
 * CANUtil.applyWithRetry(motor, config, "ElevatorLeader");
 * }</pre>
 */
public final class CANUtil {

  /** Maximum number of CAN config retries before reporting a critical fault. */
  private static final int DEFAULT_RETRIES = 5;

  private CANUtil() {}

  /**
   * Applies a TalonFX configuration with retry logic for CAN bus contention.
   *
   * <p>Attempts to apply the configuration up to {@link #DEFAULT_RETRIES} times. If all attempts
   * fail, a CRITICAL fault is raised through {@link MARSFaultManager}.
   *
   * @param motor The TalonFX motor to configure.
   * @param config The configuration object to apply (any type accepted by {@code
   *     TalonFXConfigurator.apply()}).
   * @param label A human-readable label for fault reporting (e.g., "ElevatorLeader").
   * @return The final {@link StatusCode} from the last attempt.
   */
  public static StatusCode applyWithRetry(TalonFX motor, Object config, String label) {
    return applyWithRetry(motor, config, label, DEFAULT_RETRIES);
  }

  /**
   * Applies a TalonFX configuration with a specified number of retries.
   *
   * @param motor The TalonFX motor to configure.
   * @param config The configuration object to apply.
   * @param label A human-readable label for fault reporting.
   * @param retries The maximum number of attempts.
   * @return The final {@link StatusCode} from the last attempt.
   */
  public static StatusCode applyWithRetry(TalonFX motor, Object config, String label, int retries) {
    StatusCode status = StatusCode.StatusCodeNotInitialized;
    for (int i = 0; i < retries; i++) {
      status = applyConfig(motor, config);
      if (status.isOK()) {
        return status;
      }
    }
    MARSFaultManager.reportHardwareDisconnect("CANConfig_" + label);
    return status;
  }

  /**
   * Dynamically dispatches the correct {@code apply()} overload based on the config type.
   *
   * <p>Phoenix 6's TalonFXConfigurator has separate {@code apply()} methods for full configs,
   * Slot0Configs, MotorOutputConfigs, etc. This dispatcher handles the common types.
   */
  private static StatusCode applyConfig(TalonFX motor, Object config) {
    if (config instanceof com.ctre.phoenix6.configs.TalonFXConfiguration c) {
      return motor.getConfigurator().apply(c);
    } else if (config instanceof com.ctre.phoenix6.configs.Slot0Configs c) {
      return motor.getConfigurator().apply(c);
    } else if (config instanceof com.ctre.phoenix6.configs.MotorOutputConfigs c) {
      return motor.getConfigurator().apply(c);
    } else if (config instanceof com.ctre.phoenix6.configs.CurrentLimitsConfigs c) {
      return motor.getConfigurator().apply(c);
    } else {
      throw new IllegalArgumentException(
          "CANUtil: Unsupported config type: " + config.getClass().getSimpleName());
    }
  }
}
