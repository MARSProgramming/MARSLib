package com.marslib.power;

import com.marslib.faults.Alert;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.constants.PowerConstants;
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

  private final Alert warningAlert =
      new Alert(
          "Power",
          "Voltage Shedding: Voltage below " + PowerConstants.WARNING_VOLTAGE + "V",
          Alert.AlertType.WARNING);
  private final Alert criticalAlert =
      new Alert(
          "Power",
          "Voltage Shedding: Voltage below " + PowerConstants.CRITICAL_VOLTAGE + "V",
          Alert.AlertType.CRITICAL);

  /**
   * Initializes the Power Manager.
   *
   * @param io The selected IO layer (Sim or Hardware PDH) pulling raw voltages.
   */
  public MARSPowerManager(PowerIO io) {
    this.io = io;
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
      if (inputs.voltage < PowerConstants.CRITICAL_VOLTAGE) {
        warningAlert.set(true);
        criticalAlert.set(true);
      } else if (inputs.voltage < PowerConstants.WARNING_VOLTAGE) {
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

  /**
   * Helper function for mechanisms to compute dynamically shedded voltage scaling when battery sag
   * impacts structural stability.
   *
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
