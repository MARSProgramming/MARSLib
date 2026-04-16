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
      modules[i] = new SwerveModule(i, new SwerveModuleIOSim(i), config);
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

    cmd.initialize();
    cmd.execute();
    assertFalse(cmd.isFinished());
  }

  @Test
  void testGetSystemCheckCommand() {
    Command cmd = diagnostics.getSystemCheckCommand();
    assertNotNull(cmd);

    cmd.initialize();
    cmd.execute();
    assertFalse(cmd.isFinished());
  }
}
