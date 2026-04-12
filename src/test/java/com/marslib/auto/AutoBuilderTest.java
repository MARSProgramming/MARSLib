package com.marslib.auto;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.marslib.swerve.SwerveDrive;
import com.marslib.swerve.SwerveModule;
import com.marslib.testing.MARSTestHarness;
import com.pathplanner.lib.auto.AutoBuilder;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.Command;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AutoBuilderTest {
  private SwerveDrive swerveDrive;

  @BeforeEach
  public void setUp() {
    MARSTestHarness.reset();

    // Setup SwerveDrive dependencies
    SwerveModule[] modules = new SwerveModule[4];
    for (int i = 0; i < 4; i++) {
      modules[i] = new SwerveModule(i, new com.marslib.swerve.SwerveModuleIOSim(i));
    }
    com.marslib.power.MARSPowerManager powerManager =
        new com.marslib.power.MARSPowerManager(new com.marslib.power.PowerIO() {});

    swerveDrive = new SwerveDrive(modules, new com.marslib.swerve.GyroIOSim(), powerManager);
    // Configure PathPlanner as it would be in RobotContainer
    swerveDrive.configurePathPlanner();
  }

  @Test
  public void testAutoBuilderIsConfigured() {
    assertTrue(
        AutoBuilder.isConfigured(), "AutoBuilder should be configured after SwerveDrive setup");
  }

  @Test
  public void testPathFollowingCommandCreation() {
    // This verifies that we can create a path-following command without crashing
    // even without a real path file (using a placeholder if necessary, or just buildTrajectory)

    Pose2d target = new Pose2d(2.0, 2.0, Rotation2d.fromDegrees(0));

    // AutoBuilder.pathfindToPose is a convenient way to test the configuration
    assertDoesNotThrow(
        () -> {
          Command pathfindCommand =
              AutoBuilder.pathfindToPose(
                  target, new com.pathplanner.lib.path.PathConstraints(1.0, 1.0, 1.0, 1.0), 0.0);
          assertNotNull(pathfindCommand);
        });
  }
}
