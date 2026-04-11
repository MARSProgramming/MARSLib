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
