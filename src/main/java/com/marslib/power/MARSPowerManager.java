/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.power;

import com.marslib.faults.Alert;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import org.littletonrobotics.junction.Logger;

/**
 * Subsystem responsible for monitoring the central Power Distribution Hub (PDH).
 *
 * <p>Students: This layer pulls input telemetry directly from the physical hardware
 * (Voltage/Current) and actively manages structural alerts. Most importantly, it pushes real-time
 * voltage down into the SwerveDrive subsystem allowing the robot to automatically execute "Load
 * Shedding" to prevent brownouts.
 */
public class MARSPowerManager extends SubsystemBase {
  private final PowerIO io;
  private final PowerIOInputsAutoLogged inputs = new PowerIOInputsAutoLogged();
  private final edu.wpi.first.wpilibj.PowerDistribution pdh =
      new edu.wpi.first.wpilibj.PowerDistribution();
  private final PowerConfig config;

  private final Alert warningAlert;
  private final Alert criticalAlert;

  /**
   * Initializes the Power Manager.
   *
   * @param io The selected IO layer (Sim or Hardware PDH) pulling raw voltages.
   * @param config The power threshold configuration for the robot.
   */
  public MARSPowerManager(PowerIO io, PowerConfig config) {
    this.io = io;
    this.config = config;
    this.warningAlert =
        new Alert(
            "Power",
            "Voltage Shedding: Voltage below " + config.warningVoltage() + "V",
            Alert.AlertType.WARNING);
    this.criticalAlert =
        new Alert(
            "Power",
            "Voltage Shedding: Voltage below " + config.criticalVoltage() + "V",
            Alert.AlertType.CRITICAL);
  }

  /**
   * Processes IO loops periodically. It triggers AdvantageScope "Alerts" if voltage falls below
   * safe structural operating limits.
   */
  @Override
  public void periodic() {
    io.updateInputs(inputs);
    Logger.processInputs("Power", inputs);

    // Track total amperage to diagnose subsystem stalls and predict brownouts
    Logger.recordOutput("Power/TotalCurrentDraw_A", pdh.getTotalCurrent());

    if (inputs.voltage > 0) {
      if (inputs.voltage < config.criticalVoltage()) {
        warningAlert.set(true);
        criticalAlert.set(true);
      } else if (inputs.voltage < config.warningVoltage()) {
        warningAlert.set(true);
        criticalAlert.set(false);
      } else {
        warningAlert.set(false);
        criticalAlert.set(false);
      }
    } else {
      warningAlert.set(false);
      criticalAlert.set(false);
    }
  }

  /**
   * Evaluates the absolute bus voltage dynamically.
   *
   * @return The exact system voltage across the PDP/PDH. Usually ~12.5V, dropping during heavy
   *     loads.
   */
  public double getVoltage() {
    return inputs.voltage;
  }

  /** Returns true if the system voltage is currently below the warning threshold. */
  public boolean isWarning() {
    return inputs.voltage < config.warningVoltage();
  }

  /** Returns true if the system voltage is currently below the critical threshold. */
  public boolean isCritical() {
    return inputs.voltage < config.criticalVoltage();
  }

  /**
   * Helper function for mechanisms to compute dynamically shedded voltage scaling based on the
   * internal PowerConfig thresholds.
   *
   * @return A multiplier [0.0 - 1.0].
   */
  public double calculateSheddingFactor() {
    return calculateVoltageScaleFactor(config.nominalVoltage(), config.criticalVoltage());
  }

  /**
   * Helper function for mechanisms to compute dynamically shedded voltage scaling when battery sag
   * impacts structural stability.
   *
   * @param nominalVoltage The voltage at which shedding starts.
   * @param criticalVoltage The voltage at which output is zeroed.
   * @return A multiplier [0.0 - 1.0]. Returns 1.0 when nominal. Scales down towards 0.0 near
   *     critical voltage bounds.
   */
  public double calculateVoltageScaleFactor(double nominalVoltage, double criticalVoltage) {
    if (inputs.voltage >= nominalVoltage) {
      return 1.0;
    }
    double scale = (inputs.voltage - criticalVoltage) / (nominalVoltage - criticalVoltage);
    return MathUtil.clamp(scale, 0.0, 1.0);
  }
}
