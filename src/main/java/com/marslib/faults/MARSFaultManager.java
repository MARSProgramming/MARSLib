/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.faults;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Central aggregator for all mission-critical error states and hardware timeouts.
 *
 * <p>Students: When a hardware layer fails (e.g. TalonFX CAN frame timeout), the exception is
 * passed here. This singleton triggers the LEDs to flash red, the Xbox Controllers to rumble, and
 * posts the Alert directly onto AdvantageScope's dashboard.
 *
 * <p><b>Thread Safety:</b> All fields use atomic or concurrent data structures because {@link
 * #reportHardwareDisconnect(String)} may be called from multiple IO threads concurrently (e.g. CAN
 * bus dropout affecting multiple modules simultaneously).
 */
public class MARSFaultManager {
  private static final AtomicBoolean unacknowledgedCriticalFault = new AtomicBoolean(false);
  private static final AtomicInteger activeCriticalFaults = new AtomicInteger(0);

  /** Resets all fault state. Required for JUnit test isolation. */
  public static void clear() {
    unacknowledgedCriticalFault.set(false);
    activeCriticalFaults.set(0);

    // Explicitly shut down all pending alert trackers to clear UI state
    for (Alert alert : disconnectAlerts.values()) {
      alert.set(false);
    }

    disconnectAlerts.clear();
  }

  /** Reports that a new critical fault has occurred. */
  private static void reportNewCriticalFault() {
    unacknowledgedCriticalFault.set(true);
  }

  /** Increments the critical fault counter and registers a dashboard flag state. */
  static void registerCriticalFault() {
    activeCriticalFaults.incrementAndGet();
    reportNewCriticalFault();
  }

  /** Safely decrements the active fault tracker as sub-systems return online. */
  static void unregisterCriticalFault() {
    activeCriticalFaults.updateAndGet(current -> Math.max(0, current - 1));
  }

  /**
   * Evaluates if any fault states exist in the stack currently.
   *
   * @return True if one or more hardware modules are reporting critical disconnects.
   */
  public static boolean hasActiveCriticalFaults() {
    return activeCriticalFaults.get() > 0;
  }

  /** Returns whether a new critical fault has occurred recently. */
  public static boolean hasNewCriticalFault() {
    return unacknowledgedCriticalFault.get();
  }

  /**
   * Clears the new critical fault flag. Usually called by the HMI once it has acknowledged and
   * triggered the appropriate rumble/flash sequences.
   */
  public static void clearNewCriticalFault() {
    unacknowledgedCriticalFault.set(false);
  }

  private static final ConcurrentHashMap<String, Alert> disconnectAlerts =
      new ConcurrentHashMap<>();

  /**
   * Standard helper triggering an automatic CAN API structural error block.
   *
   * @param deviceName Name of the specific controller/device throwing the timeout (e.g.
   *     "ElevatorTalon").
   */
  public static void reportHardwareDisconnect(String deviceName) {
    disconnectAlerts.computeIfAbsent(
        deviceName, key -> new Alert("Hardware Disconnect: " + key, Alert.AlertType.CRITICAL));
    disconnectAlerts.get(deviceName).set(true);
  }
}
