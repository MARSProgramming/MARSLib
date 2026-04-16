package com.marslib.auto;

import static org.junit.jupiter.api.Assertions.*;

import com.marslib.testing.MARSTestHarness;
import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.path.PathPlannerPath;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for MARSAuto command factory methods.
 *
 * <p>Since MARSAuto wraps PathPlanner's AutoBuilder (which requires full robot configuration),
 * these tests verify graceful fallback behavior when trajectories are unavailable.
 */
public class MARSAutoTest {

  private com.marslib.swerve.SwerveDrive swerveDrive;

  @BeforeEach
  public void setUp() {
    MARSTestHarness.reset();

    com.marslib.swerve.SwerveConfig swerveConfig = MARSTestHarness.createSwerveConfig();
    com.marslib.power.MARSPowerManager powerManager =
        new com.marslib.power.MARSPowerManager(
            new com.marslib.power.PowerIOSim(MARSTestHarness.createPowerConfig()),
            MARSTestHarness.createPowerConfig());

    com.marslib.swerve.SwerveModule[] modules = new com.marslib.swerve.SwerveModule[4];
    for (int i = 0; i < 4; i++) {
      modules[i] =
          new com.marslib.swerve.SwerveModule(
              i, new com.marslib.swerve.SwerveModuleIOSim(i, swerveConfig), swerveConfig);
    }

    swerveDrive =
        new com.marslib.swerve.SwerveDrive(
            modules, new com.marslib.swerve.GyroIOSim(), powerManager, swerveConfig);

    com.marslib.swerve.SwerveAutoBuilder.configure(swerveDrive);
  }

  @Test
  public void testRunChoreoTrajectoryReturnsFallbackForMissingFile() {
    // PathPlanner is not configured in test context, so this should gracefully return a fallback
    Command result = MARSAuto.runChoreoTrajectory("nonexistent_trajectory");
    assertNotNull(result, "Should return a fallback command, never null.");
  }

  @Test
  public void testPathfindThenRunChoreoReturnsFallbackForMissingFile() {
    Command result =
        MARSAuto.pathfindThenRunChoreoTrajectory(swerveDrive, "nonexistent_trajectory");
    assertNotNull(result, "Should return a fallback command, never null.");
  }

  @Test
  public void testPathfindObstacleAvoidanceDoesNotThrowWithoutAutoBuilder() {
    assertDoesNotThrow(
        () -> {
          MARSAuto.pathfindObstacleAvoidance(
              swerveDrive,
              new edu.wpi.first.math.geometry.Pose2d(
                  1.0, 1.0, new edu.wpi.first.math.geometry.Rotation2d()));
        },
        "Should successfully create pathfinding command");
  }

  @Test
  public void testMarsAutoInstantiation() {
    // Cover the implicit public constructor for 100% block coverage
    MARSAuto instance = new MARSAuto();
    assertNotNull(instance);
  }

  @Test
  public void testRunChoreoTrajectoryCanLoadFile() {
    try (org.mockito.MockedStatic<PathPlannerPath> mockedPath =
            org.mockito.Mockito.mockStatic(PathPlannerPath.class);
        org.mockito.MockedStatic<AutoBuilder> mockedAuto =
            org.mockito.Mockito.mockStatic(AutoBuilder.class)) {
      PathPlannerPath dummyPath = org.mockito.Mockito.mock(PathPlannerPath.class);
      mockedPath.when(() -> PathPlannerPath.fromChoreoTrajectory("test")).thenReturn(dummyPath);
      mockedAuto
          .when(() -> AutoBuilder.followPath(dummyPath))
          .thenReturn(edu.wpi.first.wpilibj2.command.Commands.none());

      Command result = MARSAuto.runChoreoTrajectory("test");
      assertNotNull(result);
    }
  }

  @Test
  public void testRunChoreoTrajectoryException() {
    try (org.mockito.MockedStatic<PathPlannerPath> mockedPath =
        org.mockito.Mockito.mockStatic(PathPlannerPath.class)) {
      mockedPath
          .when(() -> PathPlannerPath.fromChoreoTrajectory("test_throws"))
          .thenThrow(new RuntimeException("Simulated exception"));

      Command result = MARSAuto.runChoreoTrajectory("test_throws");
      assertEquals(Commands.none().getClass(), result.getClass());
    }
  }

  @Test
  public void testPathfindThenRunChoreoCanLoadFile() {
    try (org.mockito.MockedStatic<PathPlannerPath> mockedPath =
            org.mockito.Mockito.mockStatic(PathPlannerPath.class);
        org.mockito.MockedStatic<AutoBuilder> mockedAuto =
            org.mockito.Mockito.mockStatic(AutoBuilder.class)) {
      PathPlannerPath dummyPath = org.mockito.Mockito.mock(PathPlannerPath.class);
      mockedPath.when(() -> PathPlannerPath.fromChoreoTrajectory("test")).thenReturn(dummyPath);
      mockedAuto
          .when(
              () ->
                  AutoBuilder.pathfindThenFollowPath(
                      org.mockito.ArgumentMatchers.eq(dummyPath),
                      org.mockito.ArgumentMatchers.any()))
          .thenReturn(edu.wpi.first.wpilibj2.command.Commands.none());

      Command result = MARSAuto.pathfindThenRunChoreoTrajectory(swerveDrive, "test");
      assertNotNull(result);
    }
  }

  @Test
  public void testPathfindThenRunChoreoException() {
    try (org.mockito.MockedStatic<PathPlannerPath> mockedPath =
        org.mockito.Mockito.mockStatic(PathPlannerPath.class)) {
      mockedPath
          .when(() -> PathPlannerPath.fromChoreoTrajectory("test_throws"))
          .thenThrow(new RuntimeException("Simulated exception"));

      Command result = MARSAuto.pathfindThenRunChoreoTrajectory(swerveDrive, "test_throws");
      assertEquals(Commands.none().getClass(), result.getClass());
    }
  }
}
