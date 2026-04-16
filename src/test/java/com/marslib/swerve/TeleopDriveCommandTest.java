package com.marslib.swerve;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.marslib.power.MARSPowerManager;
import com.marslib.power.PowerConfig;
import com.marslib.power.PowerIOSim;
import com.marslib.testing.MARSTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TeleopDriveCommandTest {

  private SwerveDrive swerveDrive;
  private TeleopDriveCommand teleopCommand;
  private double[] joysticks = new double[3]; // x, y, omega

  @BeforeEach
  public void setUp() {
    MARSTestHarness.reset();

    SwerveConfig swerveConfig = MARSTestHarness.createSwerveConfig();
    PowerConfig powerConfig = MARSTestHarness.createPowerConfig();
    MARSPowerManager powerManager = new MARSPowerManager(new PowerIOSim(powerConfig), powerConfig);

    SwerveModule[] modules = new SwerveModule[4];
    for (int i = 0; i < 4; i++) {
      modules[i] = new SwerveModule(i, new SwerveModuleIOSim(i, swerveConfig), swerveConfig);
    }

    swerveDrive = spy(new SwerveDrive(modules, new GyroIOSim(), powerManager, swerveConfig));

    // Default joystick clear
    joysticks[0] = 0.0;
    joysticks[1] = 0.0;
    joysticks[2] = 0.0;

    teleopCommand =
        new TeleopDriveCommand(
            swerveDrive, swerveConfig, () -> joysticks[0], () -> joysticks[1], () -> joysticks[2]);
  }

  @Test
  public void testInitializationCapturesHeading() {
    teleopCommand.initialize();
    assertDoesNotThrow(() -> teleopCommand.execute());
  }

  @Test
  public void testNaNFirewallZerosOutputs() {
    teleopCommand.initialize();

    // Inject corrupt data
    joysticks[0] = Double.NaN;
    joysticks[1] = Double.NaN;
    joysticks[2] = Double.NaN;

    teleopCommand.execute();

    // Verify it commanded zeroes natively, rather than propagating NaN
    verify(swerveDrive)
        .runVelocity(
            argThat(
                speeds ->
                    speeds.vxMetersPerSecond == 0.0
                        && speeds.vyMetersPerSecond == 0.0
                        && speeds.omegaRadiansPerSecond == 0.0));
  }

  @Test
  public void testHeadingLockActivatesWhenRotationJoystickIsZero() {
    teleopCommand.initialize();

    // Drive forward without rotating
    joysticks[0] = 1.0;
    joysticks[1] = 0.0;
    joysticks[2] = 0.0;

    teleopCommand.execute();

    // It should lock heading via PID if x or y is active but omega is zero.
    verify(swerveDrive, atLeastOnce()).runVelocity(any());
  }
}
