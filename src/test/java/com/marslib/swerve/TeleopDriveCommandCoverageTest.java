package com.marslib.swerve;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.marslib.power.MARSPowerManager;
import com.marslib.power.PowerConfig;
import com.marslib.power.PowerIOSim;
import com.marslib.simulation.MARSPhysicsWorld;
import com.marslib.testing.MARSTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Targeted coverage tests for TeleopDriveCommand's previously-uncovered branches: isFinished(),
 * gyro-disconnected heading lock, and spinning anti-snapback logic.
 */
public class TeleopDriveCommandCoverageTest {

  private SwerveDrive swerveDrive;
  private TeleopDriveCommand teleopCommand;
  private double[] joysticks = new double[3];

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

    joysticks[0] = 0.0;
    joysticks[1] = 0.0;
    joysticks[2] = 0.0;

    teleopCommand =
        new TeleopDriveCommand(
            swerveDrive, swerveConfig, () -> joysticks[0], () -> joysticks[1], () -> joysticks[2]);
  }

  @Test
  public void testIsFinishedReturnsFalse() {
    // Default commands must never self-terminate
    assertFalse(teleopCommand.isFinished(), "Default teleop command must never finish on its own");
  }

  @Test
  public void testGyroDisconnectedDeadReckoningFallback() {
    // Simulate gyro disconnect
    GyroIO disconnectedGyro =
        new GyroIO() {
          @Override
          public void updateInputs(GyroIO.GyroIOInputs inputs) {
            inputs.connected = false;
            inputs.yawPositionRad = 0.0;
            inputs.yawVelocityRadPerSec = 0.0;
            inputs.pitchPositionRad = 0.0;
            inputs.rollPositionRad = 0.0;
            inputs.odometryYawPositions = new double[0];
          }
        };

    SwerveConfig swerveConfig = MARSTestHarness.createSwerveConfig();
    PowerConfig powerConfig = MARSTestHarness.createPowerConfig();
    MARSPowerManager powerManager = new MARSPowerManager(new PowerIOSim(powerConfig), powerConfig);

    SwerveModule[] modules = new SwerveModule[4];
    for (int i = 0; i < 4; i++) {
      modules[i] = new SwerveModule(i, new SwerveModuleIOSim(i, swerveConfig), swerveConfig);
    }

    SwerveDrive disconnectedDrive =
        spy(new SwerveDrive(modules, disconnectedGyro, powerManager, swerveConfig));

    TeleopDriveCommand cmd =
        new TeleopDriveCommand(
            disconnectedDrive,
            swerveConfig,
            () -> joysticks[0],
            () -> joysticks[1],
            () -> joysticks[2]);

    cmd.initialize();

    // Drive forward — exercises the gyro-disconnected dead reckoning branch for measuredOmega
    joysticks[0] = 0.8;
    joysticks[1] = 0.0;
    joysticks[2] = 0.0;

    // Run multiple cycles to exercise the heading lock fallback path
    for (int i = 0; i < 10; i++) {
      MARSPhysicsWorld.getInstance().update(0.02);
      disconnectedDrive.periodic();
      cmd.execute();
    }

    verify(disconnectedDrive, atLeast(5)).runVelocity(any());
  }

  @Test
  public void testSpinningAntiSnapbackBranch() {
    teleopCommand.initialize();

    // First: rotate the robot to build angular momentum
    joysticks[0] = 0.0;
    joysticks[1] = 0.0;
    joysticks[2] = 1.0; // Full rotation

    for (int i = 0; i < 20; i++) {
      MARSPhysicsWorld.getInstance().update(0.02);
      swerveDrive.periodic();
      teleopCommand.execute();
    }

    // Now: release rotation stick but maintain translation.
    // The robot should still be spinning from angular momentum → triggers the >0.4 rad/s branch
    joysticks[0] = 0.5;
    joysticks[1] = 0.0;
    joysticks[2] = 0.0;

    // Execute a few more cycles to hit the anti-snapback logic
    for (int i = 0; i < 5; i++) {
      MARSPhysicsWorld.getInstance().update(0.02);
      swerveDrive.periodic();
      teleopCommand.execute();
    }

    verify(swerveDrive, atLeast(20)).runVelocity(any());
  }

  @Test
  public void testStationaryNoTranslationOrRotation() {
    teleopCommand.initialize();

    // Zero all inputs — exercises the "else" branch where targetHeading is set and omega = 0
    joysticks[0] = 0.0;
    joysticks[1] = 0.0;
    joysticks[2] = 0.0;

    teleopCommand.execute();

    verify(swerveDrive, atLeastOnce())
        .runVelocity(
            argThat(speeds -> speeds.vxMetersPerSecond == 0.0 && speeds.vyMetersPerSecond == 0.0));
  }
}
