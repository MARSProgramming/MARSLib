/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.diagnostics;

import com.marslib.faults.Alert;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import java.util.Arrays;
import org.littletonrobotics.junction.Logger;

/**
 * A comprehensive, robust pre-match diagnostic routine decoupled from mechanism logic.
 *
 * <p>Takes any number of {@link SystemTestable} objects and sequences them sequentially, starting
 * with a Battery Voltage Gate to ensure physical capability.
 */
public class SystemCheckCommand extends SequentialCommandGroup {

  private static final double MINIMUM_BATTERY_VOLTAGE = 12.0;
  private final Alert batteryAlert =
      new Alert("SystemCheck: Battery too low to test! Swap battery.", Alert.AlertType.CRITICAL);
  private final Alert successAlert =
      new Alert("SystemCheck: ALL SYSTEM ROUTINES EXECUTED", Alert.AlertType.INFO);

  /**
   * Safely iterates through all passed testable subsystems.
   *
   * @param subsystems Array or varargs of components implementing SystemTestable.
   */
  public SystemCheckCommand(SystemTestable... subsystems) {
    addCommands(
        Commands.runOnce(
            () -> {
              double voltage = RobotController.getBatteryVoltage();
              Logger.recordOutput("SystemCheck/BatteryVoltage", voltage);
              if (voltage < MINIMUM_BATTERY_VOLTAGE) {
                batteryAlert.set(true);
                throw new IllegalStateException("Battery below 12.0V, aborting physical tests.");
              } else {
                batteryAlert.set(false);
                successAlert.set(false);
              }
            }));

    Command[] tests =
        Arrays.stream(subsystems)
            .map(SystemTestable::getSystemCheckCommand)
            .toArray(Command[]::new);

    addCommands(tests);

    addCommands(Commands.runOnce(() -> successAlert.set(true)));
  }
}
