package com.marslib.diagnostics;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import org.junit.jupiter.api.Test;

public class SystemCheckCommandTest {

  private static class DummyTestable implements SystemTestable {
    @Override
    public Command getSystemCheckCommand() {
      return Commands.none();
    }
  }

  @Test
  public void testAboveMinimumVoltageDoesNotThrow() {
    SystemCheckCommand command = new SystemCheckCommand(() -> 12.5, new DummyTestable());
    assertDoesNotThrow(command::initialize, "Should not throw if battery is above 12.0V");
  }

  @Test
  public void testBelowMinimumVoltageThrowsIllegalStateException() {
    SystemCheckCommand command = new SystemCheckCommand(() -> 11.5, new DummyTestable());
    assertThrows(
        IllegalStateException.class,
        command::initialize,
        "Should throw IllegalStateException if battery is below 12.0V");
  }
}
