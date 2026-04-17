/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.faults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.littletonrobotics.junction.Logger;

/** Class for managing various alerts to be displayed on the driver station. */
public class Alert {
  private static Map<String, SendableAlerts> groups = new HashMap<>();

  /** Clears all alert groups. Required for JUnit test isolation to prevent state bleed. */
  public static void resetAll() {
    groups.clear();
  }

  private final AlertType type;
  private final String group;
  private boolean active = false;
  private double activeStartTime = 0.0;
  private String text;

  public Alert(String text, AlertType type) {
    this("Alerts", text, type);
  }

  public Alert(String group, String text, AlertType type) {
    this.group = group;
    this.text = text;
    this.type = type;

    if (!groups.containsKey(group)) {
      groups.put(group, new SendableAlerts());
    }
  }

  /**
   * Activates or deactivates this alert. When activated, the alert's timestamp is recorded and (for
   * CRITICAL type) a critical fault is registered with {@link MARSFaultManager}.
   *
   * @param active {@code true} to activate, {@code false} to deactivate.
   */
  public void set(boolean active) {
    if (active && !this.active) {
      activeStartTime = Logger.getTimestamp() / 1e6;
      if (type == AlertType.CRITICAL) {
        MARSFaultManager.registerCriticalFault();
      }
    } else if (!active && this.active) {
      if (type == AlertType.CRITICAL) {
        MARSFaultManager.unregisterCriticalFault();
      }
    }
    this.active = active;
    if (!groups.containsKey(group)) {
      groups.put(group, new SendableAlerts());
    }
    groups.get(group).updateAlert(this);
  }

  /**
   * Updates the displayed text of this alert. If the alert is currently active, the display is
   * refreshed immediately.
   *
   * @param text The new alert message.
   */
  public void setText(String text) {
    this.text = text;
    if (active) {
      if (!groups.containsKey(group)) {
        groups.put(group, new SendableAlerts());
      }
      groups.get(group).updateAlert(this);
    }
  }

  /**
   * Returns whether this alert is currently active.
   *
   * @return {@code true} if the alert is active.
   */
  public boolean get() {
    return active;
  }

  public static enum AlertType {
    INFO,
    WARNING,
    CRITICAL
  }

  private static class SendableAlerts {
    private final List<Alert> alerts = new ArrayList<>();

    /** Pre-allocated empty array to avoid repeated zero-length String[] allocations. */
    private static final String[] EMPTY_STRINGS = new String[0];

    public SendableAlerts() {}

    public void updateAlert(Alert alert) {
      if (alert.active && !alerts.contains(alert)) {
        alerts.add(alert);
      } else if (!alert.active) {
        alerts.remove(alert);
      }

      alerts.sort((a, b) -> Double.compare(a.activeStartTime, b.activeStartTime));

      List<String> infoStrings = new ArrayList<>();
      List<String> warningStrings = new ArrayList<>();
      List<String> criticalStrings = new ArrayList<>();

      for (Alert a : alerts) {
        switch (a.type) {
          case INFO:
            infoStrings.add(a.text);
            break;
          case WARNING:
            warningStrings.add(a.text);
            break;
          case CRITICAL:
            criticalStrings.add(a.text);
            break;
          default:
            break;
        }
      }

      // Use the group name in the log key so different alert groups don't overwrite each other
      String prefix = alerts.isEmpty() ? "Alerts" : alerts.get(0).group;
      Logger.recordOutput(
          prefix + "/Info",
          infoStrings.isEmpty() ? EMPTY_STRINGS : infoStrings.toArray(EMPTY_STRINGS));
      Logger.recordOutput(
          prefix + "/Warning",
          warningStrings.isEmpty() ? EMPTY_STRINGS : warningStrings.toArray(EMPTY_STRINGS));
      Logger.recordOutput(
          prefix + "/Critical",
          criticalStrings.isEmpty() ? EMPTY_STRINGS : criticalStrings.toArray(EMPTY_STRINGS));
    }
  }
}
