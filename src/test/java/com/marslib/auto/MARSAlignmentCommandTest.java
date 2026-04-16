package com.marslib.auto;

import static org.junit.jupiter.api.Assertions.*;

import com.marslib.power.MARSPowerManager;
import com.marslib.power.PowerIOSim;
import com.marslib.swerve.GyroIOSim;
import com.marslib.swerve.SwerveConfig;
import com.marslib.swerve.SwerveDrive;
import com.marslib.swerve.SwerveModule;
import com.marslib.swerve.SwerveModuleIOSim;
import com.marslib.testing.MARSTestHarness;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.constants.ModeConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MARSAlignmentCommandTest {

  private SwerveDrive swerveDrive;

  @BeforeEach
  public void setUp() {
    edu.wpi.first.hal.HAL.initialize(500, 0);
    edu.wpi.first.wpilibj.simulation.SimHooks.pauseTiming();
    MARSTestHarness.reset();
    // Required to configure WPI standard HAL hooks for integrated physical Simulation tests
    // com.marslib.simulation.
    // Construct genuine simulation architectures entirely decoupled from Mockito wrappers
    GyroIOSim gyroSim = new GyroIOSim();
    MARSPowerManager powerManager =
        new MARSPowerManager(
            new PowerIOSim(MARSTestHarness.createPowerConfig()),
            MARSTestHarness.createPowerConfig());
    SwerveConfig config = MARSTestHarness.createSwerveConfig();

    SwerveModule[] modules = new SwerveModule[4];
    for (int i = 0; i < 4; i++) {
      modules[i] = new SwerveModule(i, new SwerveModuleIOSim(i, config), config);
    }

    swerveDrive = new SwerveDrive(modules, gyroSim, powerManager, config);

    // Hard-set robot origin
    swerveDrive.resetPose(new Pose2d(0, 0, new Rotation2d(0)));
  }

  @Test
  public void testCommandDrivesTowardsTargetPhysically() {
    Pose2d targetPose = new Pose2d(2.0, 0.5, Rotation2d.fromDegrees(90));
    MARSAlignmentCommand command =
        new MARSAlignmentCommand(
            swerveDrive,
            () -> targetPose,
            4.0,
            0.4,
            1.5,
            2.0,
            Math.PI * 2,
            Math.PI * 4,
            0.15,
            0.05);

    // Start physical controller pipeline
    CommandScheduler.getInstance().schedule(command);

    // The robot starts at (0,0,0).
    // Stepping the CommandScheduler physically invokes the Holonomic PathFollower,
    // translating outputs into Volts, driving the TalonFX simulations, stepping the Dyn4j bodies,
    // reading Wheel slip back into the estimators, and updating Odometry. Total "Digital Twin"
    // pipeline!

    for (int i = 0; i < 500; i++) {
      // Step WPILib HAL timings manually!
      edu.wpi.first.wpilibj.simulation.SimHooks.stepTiming(ModeConstants.LOOP_PERIOD_SECS);
      CommandScheduler.getInstance().run();
      com.marslib.simulation.MARSPhysicsWorld.getInstance().update(ModeConstants.LOOP_PERIOD_SECS);

      if (i % 100 == 0 && !command.isFinished()) {
        // Output for debugging simulation health
      }
    }

    Pose2d resultingPose = swerveDrive.getPose();

    org.littletonrobotics.junction.Logger.recordOutput("Test/ResultingPose", resultingPose);
    System.out.println("RESULTING POSE: " + resultingPose.toString());

    // We expect the robot to have physically traveled significantly towards (2.0, 0.5)
    // and rotated towards 90 degrees after 10 seconds of simulation time (500 ticks).
    assertTrue(
        resultingPose.getX() > 1.5,
        "Robot failed to traverse physically along X axis due to physics friction constraint or lack of command");
    assertTrue(resultingPose.getY() > 0.3, "Robot failed to traverse physically along Y axis");

    // Due to the high peak acceleration constraints of the native Dyn4j swerve, the robot easily
    // traverses the map. Exact settling time depends on PID tuning which is outside the scope of
    // this pure-physics test.
  }

  @Test
  public void testCommandFinishesWhenPhysicallyAligned() {
    Pose2d targetPose = new Pose2d(0.001, 0.001, Rotation2d.fromDegrees(0));
    swerveDrive.resetPose(targetPose);

    MARSAlignmentCommand command =
        new MARSAlignmentCommand(
            swerveDrive,
            () -> targetPose,
            5.0,
            5.0,
            3.0,
            3.0,
            Math.PI * 2,
            Math.PI * 4,
            0.05,
            0.05);
    CommandScheduler.getInstance().schedule(command);

    CommandScheduler.getInstance().run();

    assertTrue(
        command.isFinished(),
        "Should cleanly terminate natively if within PID tolerance limits from start");
  }
}
