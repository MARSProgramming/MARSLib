package com.marslib.swerve;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.marslib.power.MARSPowerManager;
import com.marslib.power.PowerIOSim;
import com.marslib.testing.MARSTestHarness;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SwerveDiagnosticsTest {

  private SwerveDrive drive;
  private SwerveDiagnostics diagnostics;

  @BeforeEach
  void setUp() {
    MARSTestHarness.reset();
    var config = MARSTestHarness.createSwerveConfig();
    var powerConfig = MARSTestHarness.createPowerConfig();

    var powerManager = new MARSPowerManager(new PowerIOSim(powerConfig), powerConfig);
    var gyroIO = new GyroIOSim();

    SwerveModule[] modules = new SwerveModule[4];
    for (int i = 0; i < 4; i++) {
      modules[i] = new SwerveModule(i, new SwerveModuleIOSim(i, config), config);
    }

    drive = new SwerveDrive(modules, gyroIO, powerManager, config);
    diagnostics = new SwerveDiagnostics(drive, modules);
  }

  @AfterEach
  void tearDown() {
    drive = null;
    diagnostics = null;
  }

  @Test
  void testSysIdRoutinesNotNull() {
    assertNotNull(diagnostics.sysIdQuasistatic(SysIdRoutine.Direction.kForward));
    assertNotNull(diagnostics.sysIdQuasistatic(SysIdRoutine.Direction.kReverse));
    assertNotNull(diagnostics.sysIdDynamic(SysIdRoutine.Direction.kForward));
    assertNotNull(diagnostics.sysIdDynamic(SysIdRoutine.Direction.kReverse));
  }

  @Test
  void testFinalClimbLineupCommand() {
    Command cmd = diagnostics.finalClimbLineupCommand();
    assertNotNull(cmd);

    cmd.schedule();

    // The sequence has two 0.5s timeout run commands + finallyDo. 1.5 seconds total max.
    for (int i = 0; i < 75; i++) {
      edu.wpi.first.wpilibj.simulation.SimHooks.stepTiming(0.02);
      edu.wpi.first.wpilibj2.command.CommandScheduler.getInstance().run();
    }

    // By the end, it should be finished and velocity should be zero.
    assertFalse(edu.wpi.first.wpilibj2.command.CommandScheduler.getInstance().isScheduled(cmd));
  }

  @Test
  void testGetSystemCheckCommand() {
    Command cmd = diagnostics.getSystemCheckCommand();
    assertNotNull(cmd);

    cmd.schedule();

    // Run the scheduler to advance the 1.5 second waits and hit all lambdas
    for (int i = 0; i < 150; i++) { // 3 seconds of simulated time
      edu.wpi.first.wpilibj.simulation.SimHooks.stepTiming(0.02);
      edu.wpi.first.wpilibj2.command.CommandScheduler.getInstance().run();
    }

    // Test the failure branches by manually injecting a critical fault beforehand
    new com.marslib.faults.Alert("Mock Critical Fault", com.marslib.faults.Alert.AlertType.CRITICAL)
        .set(true);

    Command cmdFail = diagnostics.getSystemCheckCommand();
    cmdFail.schedule();
    for (int i = 0; i < 150; i++) { // 3 seconds of simulated time
      edu.wpi.first.wpilibj.simulation.SimHooks.stepTiming(0.02);
      edu.wpi.first.wpilibj2.command.CommandScheduler.getInstance().run();
    }
  }
}
