/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.diagnostics;

import edu.wpi.first.wpilibj2.command.Command;

/**
 * Indicates that a subsystem or component can perform an automated self-test.
 *
 * <p>Implementing classes must provide a command that executes physical maneuvers and assert
 * hardware readings against strict tolerances, triggering Alerts upon failure.
 */
public interface SystemTestable {
  /**
   * Generates an autonomous, state-machine style verification routine for this subsystem.
   *
   * @return A command that runs through diagnostic checks and logs results.
   */
  Command getSystemCheckCommand();
}
