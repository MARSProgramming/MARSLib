/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.hmi;

/**
 * Hardware abstraction interface for robot LED feedback systems.
 *
 * <p>Implementations may target WS2812B addressable strips ({@link LEDIOAddressable}) or CTRE
 * CANdle controllers. The {@link LEDManager} calls these methods each loop to reflect the robot's
 * current state (default, load shedding, or critical fault).
 */
public interface LEDIO {

  /** The discrete visual states an LED strip can render. */
  public enum State {
    /** Default idle pattern (e.g., solid team color). */
    DEFAULT,
    /** Warning pattern indicating active power load shedding. */
    LOAD_SHEDDING,
    /** Critical fault flash pattern (e.g., flashing red). */
    CRITICAL_FAULT
  }

  /** Sets the LED strip to its default idle color pattern (e.g., solid team color). */
  public void setDefaultColors();

  /** Sets the LED strip to a warning pattern indicating active power load shedding. */
  public void setLoadSheddingColors();

  /** Sets the LED strip to a critical fault flash pattern (e.g., flashing red). */
  public void setCriticalFaultFlash();

  /** Pushes the current color buffer to the physical LED hardware. */
  public void update();
}
