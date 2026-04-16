package com.marslib.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.marslib.testing.MARSTestHarness;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PhysicsStressTest {
  private SwerveChassisPhysics physics;
  private final double MASS_KG = 50.0;
  private final double MU = 1.0; // Static friction coefficient

  @BeforeEach
  public void setup() {
    MARSTestHarness.reset();
    physics =
        new SwerveChassisPhysics(
            MASS_KG,
            0.7,
            0.7,
            MU,
            new edu.wpi.first.math.geometry.Translation2d[] {
              new edu.wpi.first.math.geometry.Translation2d(0.35, 0.35),
              new edu.wpi.first.math.geometry.Translation2d(0.35, -0.35),
              new edu.wpi.first.math.geometry.Translation2d(-0.35, 0.35),
              new edu.wpi.first.math.geometry.Translation2d(-0.35, -0.35)
            });
    physics.setPose(new Pose2d(2.0, 2.0, new Rotation2d()));
  }

  @AfterEach
  public void teardown() {
    MARSTestHarness.cleanup();
  }

  @Test
  public void testTractionLimitMaintainsStability() {
    // Attempt an impossible acceleration (0 -> 100 m/s in 20ms)
    ChassisSpeeds impossibleSpeeds = new ChassisSpeeds(100.0, 0.0, 0.0);

    // maxDeltaV should be mu * 9.81 * 0.02 = 1.0 * 9.81 * 0.02 = 0.1962 m/s
    physics.applyKinematicSpeeds(impossibleSpeeds, 0.02);

    double resultingVx = physics.getBody().getLinearVelocity().x;

    // Validate the physics engine correctly slipped the tires and capped accel
    assertEquals(0.1962, resultingVx, 0.001, "Velocity should be capped by traction limits");
  }

  @Test
  public void testHighSpeedContinuousSlip() {
    // Run the engine for 5 seconds (250 loops) commanding an absurd velocity
    ChassisSpeeds absurdSpeeds = new ChassisSpeeds(50.0, 50.0, 0.0);

    for (int i = 0; i < 250; i++) {
      physics.applyKinematicSpeeds(absurdSpeeds, 0.02);
      // Step dyn4j world if it was attached. We just verify the chassis wrapper.
    }

    double magnitude =
        Math.hypot(
            physics.getBody().getLinearVelocity().x, physics.getBody().getLinearVelocity().y);

    // The robot should eventually reach the target if we keep holding it,
    // but we want to make sure it didn't throw NaNs.
    assertTrue(
        Double.isFinite(magnitude),
        "Swerve chassis physics collapsed into NaN under extreme slip conditions");
    assertTrue(
        magnitude > 10.0,
        "Swerve chassis failed to build up velocity sequentially during continuous slip");
  }
}
