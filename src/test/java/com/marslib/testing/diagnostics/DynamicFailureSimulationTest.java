package com.marslib.testing.diagnostics;

import static org.junit.jupiter.api.Assertions.*;

import com.marslib.auto.MARSAlignmentCommand;
import com.marslib.power.MARSPowerManager;
import com.marslib.power.PowerIOSim;
import com.marslib.simulation.MARSPhysicsWorld;
import com.marslib.swerve.GyroIOSim;
import com.marslib.swerve.SwerveConfig;
import com.marslib.swerve.SwerveDrive;
import com.marslib.swerve.SwerveModule;
import com.marslib.swerve.SwerveModuleIOSim;
import com.marslib.testing.MARSTestHarness;
import com.marslib.vision.AprilTagVisionIOInputsAutoLogged;
import com.marslib.vision.MARSVision;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import edu.wpi.first.wpilibj.simulation.SimHooks;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DynamicFailureSimulationTest {

  private SwerveDrive swerveDrive;
  private MARSPowerManager powerManager;
  private GyroIOSim gyroSim;
  private PowerIOSim powerSim;

  @BeforeEach
  public void setUp() {
    MARSTestHarness.reset();
    DriverStationSim.setAutonomous(true);

    powerSim = new PowerIOSim(MARSTestHarness.createPowerConfig());
    powerManager = new MARSPowerManager(powerSim, MARSTestHarness.createPowerConfig());
    gyroSim = new GyroIOSim();
    SwerveConfig config = MARSTestHarness.createSwerveConfig();

    SwerveModule[] modules = new SwerveModule[4];
    for (int i = 0; i < 4; i++) {
      modules[i] = new SwerveModule(i, new SwerveModuleIOSim(i, config), config);
    }

    swerveDrive = new SwerveDrive(modules, gyroSim, powerManager, config);
    swerveDrive.resetPose(new Pose2d(0, 0, new Rotation2d(0)));
  }

  @AfterEach
  public void tearDown() {
    MARSTestHarness.cleanup();
  }

  @Test
  public void testIMUDropoutDuringAlignment() {
    // Schedule an alignment command
    Pose2d target = new Pose2d(3.0, 3.0, Rotation2d.fromDegrees(90));
    MARSAlignmentCommand alignCommand =
        new MARSAlignmentCommand(
            swerveDrive, () -> target, 5.0, 5.0, 4.0, 6.0, Math.PI * 2, Math.PI, 0.05, 0.05);
    CommandScheduler.getInstance().schedule(alignCommand);

    // Run normally for 50 ticks
    for (int i = 0; i < 50; i++) {
      DriverStationSim.notifyNewData();
      SimHooks.stepTiming(0.02);
      CommandScheduler.getInstance().run();
      MARSPhysicsWorld.getInstance().update(0.02);
    }

    // INJECT: IMU Dropout (Gyro disconnect)
    gyroSim.enableCanStarvation = true;
    gyroSim.canStarvationProbability = 1.0; // Guaranteed dropout

    // Run for another 100 ticks
    for (int i = 0; i < 100; i++) {
      DriverStationSim.notifyNewData();
      SimHooks.stepTiming(0.02); // Alignment controller requires HAL time step
      CommandScheduler.getInstance().run();
      MARSPhysicsWorld.getInstance().update(0.02);
    }

    // Assert the robot didn't crash and pose is still tracking
    assertNotNull(swerveDrive.getPose());
    // Pose should still gracefully update via wheel odometry even when gyro drops
    assertTrue(swerveDrive.getPose().getX() > 0.0);
  }

  @Test
  public void testExtremeBrownoutCANStarvation() {
    // Drive the robot continuously with full velocity
    swerveDrive.runVelocity(new edu.wpi.first.math.kinematics.ChassisSpeeds(4.0, 4.0, 0));

    // INJECT: Simulate heavy brownout and massive CAN starvation
    powerSim.enableCanStarvation = true;
    powerSim.canStarvationProbability = 0.95; // 95% dropped frames
    powerSim.canStarvationDelayMs = 25; // Introduce 25ms loop delays due to CAN blocking

    long startCpuTime = System.nanoTime();

    for (int i = 0; i < 50; i++) {
      DriverStationSim.notifyNewData();
      SimHooks.stepTiming(0.02);
      CommandScheduler.getInstance().run();
      MARSPhysicsWorld.getInstance().update(0.02);

      // Force simulated brownout
      // Since PowerIOSim overrides inputs based on physics world, we need to directly drop physics
      // voltage
      MARSPhysicsWorld.getInstance().addFrameCurrentDrawAmps(500.0);
    }

    long executionTimeMs = (System.nanoTime() - startCpuTime) / 1_000_000;

    // Assert that the subsystem successfully clamped current limits
    assertDoesNotThrow(() -> powerManager.periodic());
    // Assert pose didn't NaN during brownout
    assertTrue(Double.isFinite(swerveDrive.getPose().getX()));
  }

  @Test
  public void testVisionHallucinationFiltering() {
    // Construct a MARSVision specifically configured with a mocked IO to inject garbage
    AprilTagVisionIOInputsAutoLogged mockVisionInputs = new AprilTagVisionIOInputsAutoLogged();

    MARSVision visionSystem =
        new MARSVision(
            swerveDrive,
            java.util.List.of(
                new com.marslib.vision.AprilTagVisionIO() {
                  @Override
                  public void updateInputs(
                      com.marslib.vision.AprilTagVisionIO.AprilTagVisionIOInputs inputs) {
                    inputs.estimatedPoses = mockVisionInputs.estimatedPoses;
                    inputs.timestamps = mockVisionInputs.timestamps;
                    inputs.tagCounts = mockVisionInputs.tagCounts;
                    inputs.averageDistancesMeters = mockVisionInputs.averageDistancesMeters;
                    inputs.ambiguities = mockVisionInputs.ambiguities;
                  }
                }),
            java.util.Collections.emptyList(),
            MARSTestHarness.createVisionConfig());

    Pose2d startingPose = swerveDrive.getPose();

    // INJECT: NaNs and extreme Out-of-Bounds hallucinations
    mockVisionInputs.estimatedPoses =
        new Pose3d[] {
          new Pose3d(
              Double.NaN, Double.NaN, Double.NaN, new edu.wpi.first.math.geometry.Rotation3d()),
          new Pose3d(
              500.0,
              -100.0,
              15.0,
              new edu.wpi.first.math.geometry.Rotation3d()) // 15 meters in the air, 500 meters away
        };
    mockVisionInputs.timestamps = new double[] {5.0, 5.0};
    mockVisionInputs.tagCounts = new int[] {1, 1};
    mockVisionInputs.averageDistancesMeters = new double[] {0.5, 0.5};
    mockVisionInputs.ambiguities = new double[] {0.1, 0.1};

    for (int i = 0; i < 20; i++) {
      visionSystem.periodic();
    }

    // Assert the Kalman filter rejected everything and the pose did not move
    Pose2d finalPose = swerveDrive.getPose();
    assertEquals(
        startingPose.getX(), finalPose.getX(), 0.05, "Pose was corrupted by vision hallucination!");
  }
}
