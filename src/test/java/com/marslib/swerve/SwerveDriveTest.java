package com.marslib.swerve;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.marslib.power.MARSPowerManager;
import com.marslib.power.PowerIO;
import com.marslib.simulation.MARSPhysicsWorld;
import com.marslib.testing.MARSTestHarness;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.simulation.SimHooks;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SwerveDriveTest {

  private double simulatedVoltageOverride = 12.0;
  private SwerveDrive swerveDrive;
  private SwerveModule[] modules;
  private SwerveModuleIOSim[] simIOs;
  private GyroIOSim gyroIOSim;
  private MARSPowerManager spoofedPowerManager;

  @BeforeEach
  public void setUp() {
    MARSTestHarness.reset();
    simulatedVoltageOverride = 12.0;

    // Use a custom PowerIO to manipulate testing voltages independently
    PowerIO spoofedVoltageIO =
        new PowerIO() {
          @Override
          public void updateInputs(PowerIOInputs inputs) {
            inputs.voltage = simulatedVoltageOverride;
            inputs.isBrownedOut = simulatedVoltageOverride < 6.0;
          }
        };
    spoofedPowerManager =
        new MARSPowerManager(spoofedVoltageIO, MARSTestHarness.createPowerConfig());

    gyroIOSim = new GyroIOSim();
    modules = new SwerveModule[4];
    simIOs = new SwerveModuleIOSim[4];

    SwerveConfig config = MARSTestHarness.createSwerveConfig();
    for (int i = 0; i < 4; i++) {
      simIOs[i] = new SwerveModuleIOSim(i, config);
      modules[i] = new SwerveModule(i, simIOs[i], config);
    }

    swerveDrive = new SwerveDrive(modules, gyroIOSim, spoofedPowerManager, config);
  }

  @Test
  public void testSwerveDriveIntegrationPhysicallyMovesRobot() {
    // 1. Initial State
    swerveDrive.resetPose(new Pose2d(2.0, 2.0, new edu.wpi.first.math.geometry.Rotation2d()));
    swerveDrive.periodic();
    swerveDrive.simulationPeriodic();
    assertEquals(2.0, swerveDrive.getPose().getX(), 0.1);

    // 2. Command forward velocity mapping
    ChassisSpeeds targetSpeeds = new ChassisSpeeds(3.0, 0.0, 0.0);

    // 3. Fast-forward simulation by 1.0 second
    for (int i = 0; i < 50; i++) {
      swerveDrive.runVelocity(targetSpeeds);
      CommandScheduler.getInstance().run();
      MARSPhysicsWorld.getInstance().update(0.02);
      SimHooks.stepTiming(0.02);
    }

    // 4. Assert robot physically attained the intended distance purely mathematically through dyn4j
    Pose2d finalPose = swerveDrive.getPose();
    assertEquals(
        4.8,
        finalPose.getX(),
        0.5,
        "Robot failed to traverse ~2.8m natively within 1 sec through dyn4j");
  }

  @Test
  public void testLoadSheddingRestrictsKinematicsOutputs() {
    // Drop voltage to trigger protective shutdown mode (below 6.0 in PowerConfig)
    simulatedVoltageOverride = 5.5;

    // Command drive forward
    ChassisSpeeds targetSpeeds = new ChassisSpeeds(3.0, 0.0, 0.0);
    double initialX = swerveDrive.getPose().getX();

    for (int i = 0; i < 50; i++) {
      swerveDrive.runVelocity(targetSpeeds);
      CommandScheduler.getInstance().run(); // Polls the power manager
      MARSPhysicsWorld.getInstance().update(0.02);
      SimHooks.stepTiming(0.02);
    }

    // Because voltage scales to 0.0 in deep brownout, the kinematics should have completely
    // halted and the robot shouldn't have moved.
    assertEquals(
        initialX,
        swerveDrive.getPose().getX(),
        0.05,
        "Robot moved despite critical brownout voltage ceiling clamping.");
  }

  @Test
  public void testOdometryTrustDegradesOnTilt() {
    // We create a separate temporary SwerveDrive with mocked IO to specifically test trust logic
    // because the standard simIO is linked to the physics engine and hard to manually override.

    double[] mockPitch = {0.0};
    GyroIO mockGyro =
        new GyroIO() {
          @Override
          public void updateInputs(GyroIOInputs inputs) {
            inputs.connected = true;
            inputs.pitchPositionRad = mockPitch[0];
            inputs.yawPositionRad = 0.0;
          }
        };

    double[] mockDrivePos = {0.0};
    SwerveModuleIO mockModuleIO =
        new SwerveModuleIO() {
          @Override
          public void updateInputs(SwerveModuleIOInputs inputs) {
            inputs.drivePositionsRad = new double[] {mockDrivePos[0]};
            inputs.turnPositionsRad = new double[] {0.0};
            inputs.odometryTimestamps =
                new double[] {edu.wpi.first.wpilibj.Timer.getFPGATimestamp()};
          }
        };

    SwerveConfig config = MARSTestHarness.createSwerveConfig();
    SwerveModule[] mockModules = {
      new SwerveModule(0, mockModuleIO, config), new SwerveModule(1, mockModuleIO, config),
      new SwerveModule(2, mockModuleIO, config), new SwerveModule(3, mockModuleIO, config)
    };

    SwerveDrive trustSwerve = new SwerveDrive(mockModules, mockGyro, spoofedPowerManager, config);

    // 1. Nominal case: flat ground (0 deg tilt)
    mockPitch[0] = 0.0;
    double initialX = trustSwerve.getPose().getX();
    mockDrivePos[0] = 1.0; // 1 meter moved
    trustSwerve.periodic();

    assertTrue(trustSwerve.getPose().getX() > initialX, "Robot should have moved on flat ground");

    // 2. Tilted case: 30 degrees (exceeds 25 deg threshold)
    mockPitch[0] = Math.toRadians(30.0);
    double tiltedX = trustSwerve.getPose().getX();
    mockDrivePos[0] = 5.0; // Another 4 meters of "slip"
    trustSwerve.periodic();

    assertEquals(
        tiltedX,
        trustSwerve.getPose().getX(),
        0.01,
        "Odometry should have ignored slip while tilted >25 deg");
  }

  @Test
  public void testDiagnosticSequenceGeneration() {
    assertNotNull(swerveDrive.getSystemCheckCommand());
    assertNotNull(swerveDrive.finalClimbLineupCommand());

    edu.wpi.first.wpilibj2.command.Command quasistaticFwd =
        swerveDrive.sysIdQuasistatic(
            edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine.Direction.kForward);
    edu.wpi.first.wpilibj2.command.Command dynamicFwd =
        swerveDrive.sysIdDynamic(
            edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine.Direction.kForward);

    assertNotNull(quasistaticFwd);
    assertNotNull(dynamicFwd);

    // Run a cycle of the climb lineup just to exercise lambda paths
    swerveDrive.finalClimbLineupCommand().initialize();

    // Simulate generation and execution of the system check command
    edu.wpi.first.wpilibj2.command.Command sysCheck = swerveDrive.getSystemCheckCommand();
    edu.wpi.first.wpilibj2.command.CommandScheduler.getInstance().schedule(sysCheck);

    // Step scheduler heavily to churn through all waitStates and system check asserts
    for (int i = 0; i < 4; i++) {
      edu.wpi.first.wpilibj.simulation.SimHooks.stepTiming(1.6); // Jump the 1.5s waits
      edu.wpi.first.wpilibj2.command.CommandScheduler.getInstance().run();
    }

    edu.wpi.first.wpilibj2.command.CommandScheduler.getInstance().cancelAll();
    edu.wpi.first.wpilibj2.command.CommandScheduler.getInstance().schedule(quasistaticFwd);
    edu.wpi.first.wpilibj2.command.CommandScheduler.getInstance().run();
    edu.wpi.first.wpilibj2.command.CommandScheduler.getInstance().cancelAll();

    edu.wpi.first.wpilibj2.command.CommandScheduler.getInstance().schedule(dynamicFwd);
    edu.wpi.first.wpilibj2.command.CommandScheduler.getInstance().run();
    edu.wpi.first.wpilibj2.command.CommandScheduler.getInstance().cancelAll();
  }

  @org.junit.jupiter.api.Test
  public void testSwerveAutoBuilderCoverage() {
    // Should successfully configure AutoBuilder or catch safely
    com.marslib.swerve.SwerveAutoBuilder.configure(swerveDrive);

    // Generate align to point
    edu.wpi.first.wpilibj2.command.Command alignCmd =
        com.marslib.swerve.SwerveAutoBuilder.alignToPoint(
            swerveDrive, () -> new edu.wpi.first.math.geometry.Pose2d());
    assertNotNull(alignCmd);

    // Initialize the deferred command
    alignCmd.initialize();
  }
}
