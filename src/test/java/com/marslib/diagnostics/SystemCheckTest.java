package com.marslib.diagnostics;

import static org.junit.jupiter.api.Assertions.*;

import com.marslib.testing.MARSTestHarness;
import edu.wpi.first.wpilibj.simulation.RoboRioSim;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Commands;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SystemCheckTest {

  @BeforeEach
  public void setUp() {
    MARSTestHarness.reset();
  }

  @Test
  public void testSystemCheckSuccess() {
    // 1. Set battery voltage high
    RoboRioSim.setVInVoltage(12.5);

    // 2. Create mock testable
    SystemTestable mockTestable = () -> Commands.print("Mock subsystem test running...");

    // 3. Create command
    SystemCheckCommand check =
        new SystemCheckCommand(() -> RoboRioSim.getVInVoltage(), mockTestable);

    // 4. Run it
    CommandScheduler.getInstance().schedule(check);
    for (int i = 0; i < 10; i++) {
      CommandScheduler.getInstance().run();
    }

    // Check that it finished
    assertFalse(CommandScheduler.getInstance().isScheduled(check));
  }

  @Test
  public void testSystemCheckBatteryFailure() {
    // 1. Set battery voltage low
    RoboRioSim.setVInVoltage(11.0);

    // 2. Create mock testable
    SystemTestable mockTestable = () -> Commands.print("Should not run");

    // 3. Create command
    SystemCheckCommand check =
        new SystemCheckCommand(() -> RoboRioSim.getVInVoltage(), mockTestable);

    // 4. Run it (we expect an exception from the battery gate)
    assertThrows(
        IllegalStateException.class,
        () -> {
          CommandScheduler.getInstance().schedule(check);
          CommandScheduler.getInstance().run();
        });
  }
}
