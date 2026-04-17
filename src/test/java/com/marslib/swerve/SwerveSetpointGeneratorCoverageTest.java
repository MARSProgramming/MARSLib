package com.marslib.swerve;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import frc.robot.SwerveConstants;
import org.junit.jupiter.api.Test;

/**
 * Targeted coverage tests for SwerveSetpointGenerator's previously-uncovered bisection NaN guard
 * branches and the maxDriveVelocity=0 edge case.
 */
public class SwerveSetpointGeneratorCoverageTest {

  @Test
  public void testFindSteeringMaxSNaNGuard() {
    SwerveDriveKinematics kinematics = new SwerveDriveKinematics(SwerveConstants.MODULE_LOCATIONS);
    SwerveSetpointGenerator generator = new SwerveSetpointGenerator(kinematics);

    // Exercise the NaN guard (line 161-163) by feeding values that could produce NaN
    // via Math.atan2(0,0) at the midpoint of bisection
    double result =
        generator.findSteeringMaxS(
            0.0, 0.0, 0.0, // x0, y0, f0 — all zero
            0.0, 0.0, Math.PI, // x1, y1, f1 — zero velocity but large angle jump
            0.01, // maxDeviation: tiny step
            20); // many iterations to trigger edge cases

    assertTrue(result >= 0.0 && result <= 1.0, "Result must be in [0,1], got: " + result);
  }

  @Test
  public void testFindDriveMaxSNaNGuard() {
    SwerveDriveKinematics kinematics = new SwerveDriveKinematics(SwerveConstants.MODULE_LOCATIONS);
    SwerveSetpointGenerator generator = new SwerveSetpointGenerator(kinematics);

    // Exercise the NaN guard in findDriveMaxS (line 218-220)
    double result =
        generator.findDriveMaxS(
            0.0,
            0.0,
            0.0, // x0, y0, f0
            Double.MIN_VALUE,
            Double.MIN_VALUE,
            Math.hypot(Double.MIN_VALUE, Double.MIN_VALUE),
            0.0001, // maxVelStep: tiny
            20);

    assertTrue(result >= 0.0 && result <= 1.0, "Result must be in [0,1], got: " + result);
  }

  @Test
  public void testZeroMaxDriveVelocitySkipsDesaturation() {
    SwerveDriveKinematics kinematics = new SwerveDriveKinematics(SwerveConstants.MODULE_LOCATIONS);
    SwerveSetpointGenerator generator = new SwerveSetpointGenerator(kinematics);

    SwerveSetpointGenerator.KinematicLimits limits = new SwerveSetpointGenerator.KinematicLimits();
    limits.maxDriveVelocity = 0.0; // Explicitly zero — skips desaturation branch
    limits.maxDriveAcceleration = 10.0;
    limits.maxSteeringVelocity = Math.PI * 4;

    SwerveModuleState[] initialStates =
        new SwerveModuleState[] {
          new SwerveModuleState(0, new Rotation2d()),
          new SwerveModuleState(0, new Rotation2d()),
          new SwerveModuleState(0, new Rotation2d()),
          new SwerveModuleState(0, new Rotation2d())
        };

    SwerveSetpointGenerator.SwerveSetpoint prevState =
        new SwerveSetpointGenerator.SwerveSetpoint(new ChassisSpeeds(), initialStates);

    SwerveSetpointGenerator.SwerveSetpoint result =
        generator.generateSetpoint(limits, prevState, new ChassisSpeeds(1.0, 0, 0), 0.02);

    assertNotNull(result, "Should produce valid setpoint even with zero maxDriveVelocity");
  }

  @Test
  public void testOverrideSteeringFlipBranch() {
    SwerveDriveKinematics kinematics = new SwerveDriveKinematics(SwerveConstants.MODULE_LOCATIONS);
    SwerveSetpointGenerator generator = new SwerveSetpointGenerator(kinematics);

    SwerveSetpointGenerator.KinematicLimits limits = new SwerveSetpointGenerator.KinematicLimits();
    limits.maxDriveVelocity = 4.0;
    limits.maxDriveAcceleration = 20.0;
    limits.maxSteeringVelocity = 0.01; // Very restrictive steering → forces override + slow step

    // Start at zero with arbitrary headings
    SwerveModuleState[] initialStates =
        new SwerveModuleState[] {
          new SwerveModuleState(0.0, Rotation2d.fromDegrees(0)),
          new SwerveModuleState(0.0, Rotation2d.fromDegrees(0)),
          new SwerveModuleState(0.0, Rotation2d.fromDegrees(0)),
          new SwerveModuleState(0.0, Rotation2d.fromDegrees(0))
        };

    SwerveSetpointGenerator.SwerveSetpoint prev =
        new SwerveSetpointGenerator.SwerveSetpoint(new ChassisSpeeds(), initialStates);

    // Command 90-degree strafe — prev speed is exactly 0, desired is non-zero
    // This exercises the "from stationary, need large rotation" override with numStepsNeeded > 1
    SwerveSetpointGenerator.SwerveSetpoint result =
        generator.generateSetpoint(limits, prev, new ChassisSpeeds(0, 2.0, 0), 0.02);

    assertNotNull(result);
    // Because steering is so restrictive, the modules should barely have moved
    for (int i = 0; i < 4; i++) {
      assertTrue(
          Math.abs(result.moduleStates[i].speedMetersPerSecond) < 0.1,
          "Speed should be near-zero because steering hasn't completed");
    }
  }
}
