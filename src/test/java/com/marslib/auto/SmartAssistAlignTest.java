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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SmartAssistAlignTest {

  private SwerveDrive swerveDrive;

  @BeforeEach
  public void setUp() {
    MARSTestHarness.reset();
    MARSPowerManager powerManager =
        new MARSPowerManager(
            new PowerIOSim(MARSTestHarness.createPowerConfig()),
            MARSTestHarness.createPowerConfig());
    SwerveConfig config = MARSTestHarness.createSwerveConfig();

    swerveDrive =
        new SwerveDrive(
            new SwerveModule[] {
              new SwerveModule(0, new SwerveModuleIOSim(0), config),
              new SwerveModule(1, new SwerveModuleIOSim(1), config),
              new SwerveModule(2, new SwerveModuleIOSim(2), config),
              new SwerveModule(3, new SwerveModuleIOSim(3), config)
            },
            new GyroIOSim(),
            powerManager,
            config);
  }

  @Test
  public void testSmartAssistAllowsXMovementButAutomatesYAndTheta() {
    // Current spawn is at 0, 0, 0
    Pose2d targetNode = new Pose2d(3.0, 3.0, Rotation2d.fromDegrees(90));

    // Driver is pushing forward at 2.0 m/s
    SmartAssistAlign command = new SmartAssistAlign(swerveDrive, () -> 2.0, targetNode);

    command.initialize();

    // Tick the environment for ~4 seconds to allow physics propagation
    for (int i = 0; i < 200; i++) {
      command.execute();
      swerveDrive.periodic();
      com.marslib.simulation.MARSPhysicsWorld.getInstance().update(0.02);
    }

    Pose2d newPose = swerveDrive.getPose();
    System.out.println("FINAL POSE IS: " + newPose);

    // X should have moved positively (user input drives forward)
    assertTrue(newPose.getX() > 0.01, "Should move in positive X due to human input.");
    // Y should have moved positively (auto align toward target Y=3.0)
    assertTrue(
        newPose.getY() > 0.01, "Should automatically strafe leftward (positive Y) toward target.");

    // Theta should have rotated positively (auto align toward target 90°)
    assertTrue(
        newPose.getRotation().getRadians() > 0.01,
        "Should automatically rotate positive toward target theta.");

    assertTrue(command.isFinished(), "Should be finished after 4 seconds of alignment");
    command.end(false);
  }

  @Test
  public void testSmartAssistFinishedCondition() {
    Pose2d targetNode = new Pose2d(1.5, 3.0, Rotation2d.fromDegrees(180));
    swerveDrive.resetPose(targetNode);
    SmartAssistAlign command = new SmartAssistAlign(swerveDrive, () -> 0.0, targetNode);
    assertTrue(command.isFinished(), "Should immediately finish if perfectly aligned");
  }
}
