package com.marslib.swerve;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import frc.robot.SwerveConstants;
import org.junit.jupiter.api.Test;

public class SwerveSetpointGeneratorTest {

  @Test
  public void testZeroTwistDoesNotSteer() {
    SwerveDriveKinematics kinematics = new SwerveDriveKinematics(SwerveConstants.MODULE_LOCATIONS);
    SwerveSetpointGenerator generator = new SwerveSetpointGenerator(kinematics);

    SwerveSetpointGenerator.KinematicLimits limits = new SwerveSetpointGenerator.KinematicLimits();
    limits.maxDriveVelocity = 4.5;
    limits.maxDriveAcceleration = 10.0;
    limits.maxSteeringVelocity = Math.PI * 4;

    SwerveModuleState[] initialStates =
        new SwerveModuleState[] {
          new SwerveModuleState(0, Rotation2d.fromDegrees(45)),
          new SwerveModuleState(0, Rotation2d.fromDegrees(135)),
          new SwerveModuleState(0, Rotation2d.fromDegrees(-135)),
          new SwerveModuleState(0, Rotation2d.fromDegrees(-45))
        };

    SwerveSetpointGenerator.SwerveSetpoint prevState =
        new SwerveSetpointGenerator.SwerveSetpoint(new ChassisSpeeds(), initialStates);

    // Goal: Stop. (Zero twist). Generator should NOT change module angles.
    SwerveSetpointGenerator.SwerveSetpoint newState =
        generator.generateSetpoint(limits, prevState, new ChassisSpeeds(), 0.02);

    assertEquals(0.0, newState.chassisSpeeds.vxMetersPerSecond, 0.001);
    assertEquals(0.0, newState.chassisSpeeds.vyMetersPerSecond, 0.001);

    // Verify angles didn't snap to 0
    assertEquals(45, newState.moduleStates[0].angle.getDegrees(), 0.1);
    assertEquals(135, newState.moduleStates[1].angle.getDegrees(), 0.1);
    assertEquals(-135, newState.moduleStates[2].angle.getDegrees(), 0.1);
    assertEquals(-45, newState.moduleStates[3].angle.getDegrees(), 0.1);

    // Verify speeds are zero
    for (int i = 0; i < 4; i++) {
      assertEquals(0.0, newState.moduleStates[i].speedMetersPerSecond, 0.001);
    }
  }

  @Test
  public void testAccelerationLimits() {
    SwerveDriveKinematics kinematics = new SwerveDriveKinematics(SwerveConstants.MODULE_LOCATIONS);
    SwerveSetpointGenerator generator = new SwerveSetpointGenerator(kinematics);

    SwerveSetpointGenerator.KinematicLimits limits = new SwerveSetpointGenerator.KinematicLimits();
    limits.maxDriveVelocity = 4.0;
    limits.maxDriveAcceleration = 2.0; // Restrictive accel limits
    limits.maxSteeringVelocity = Math.PI;

    SwerveModuleState[] initialStates =
        new SwerveModuleState[] {
          new SwerveModuleState(0, new Rotation2d()),
          new SwerveModuleState(0, new Rotation2d()),
          new SwerveModuleState(0, new Rotation2d()),
          new SwerveModuleState(0, new Rotation2d())
        };

    SwerveSetpointGenerator.SwerveSetpoint prevState =
        new SwerveSetpointGenerator.SwerveSetpoint(new ChassisSpeeds(), initialStates);

    // Command a massive instant acceleration
    SwerveSetpointGenerator.SwerveSetpoint newState =
        generator.generateSetpoint(
            limits, prevState, new ChassisSpeeds(4.0, 0, 0), 0.02); // dt = 0.02s

    // Max delta v = accel * dt = 2.0 * 0.02 = 0.04 m/s
    assertTrue(newState.chassisSpeeds.vxMetersPerSecond > 0.0, "Should move forward");
    assertTrue(newState.chassisSpeeds.vxMetersPerSecond <= 0.041, "Accelerated too quickly!");
  }

  @Test
  public void testSteeringVelocityLimits() {
    SwerveDriveKinematics kinematics = new SwerveDriveKinematics(SwerveConstants.MODULE_LOCATIONS);
    SwerveSetpointGenerator generator = new SwerveSetpointGenerator(kinematics);

    SwerveSetpointGenerator.KinematicLimits limits = new SwerveSetpointGenerator.KinematicLimits();
    limits.maxDriveVelocity = 4.0;
    limits.maxDriveAcceleration = 10.0;
    limits.maxSteeringVelocity = Math.PI; // 180 deg per sec

    SwerveModuleState[] initialStates =
        new SwerveModuleState[] {
          new SwerveModuleState(1.0, new Rotation2d()),
          new SwerveModuleState(1.0, new Rotation2d()),
          new SwerveModuleState(1.0, new Rotation2d()),
          new SwerveModuleState(1.0, new Rotation2d())
        };

    SwerveSetpointGenerator.SwerveSetpoint prevState =
        new SwerveSetpointGenerator.SwerveSetpoint(new ChassisSpeeds(1, 0, 0), initialStates);

    // Command full strafe sideways (90-degree instantaneous required turn)
    SwerveSetpointGenerator.SwerveSetpoint newState =
        generator.generateSetpoint(limits, prevState, new ChassisSpeeds(0.0, 1.0, 0), 0.02);

    // Just assert that we didn't steer all the way to 90 degrees instantly, proving constraint
    // works
    assertTrue(
        newState.moduleStates[0].angle.getDegrees() < 90.0,
        "Steered instantly without constraints!");
  }

  @Test
  public void testModuleHeadingFlipLimits() {
    SwerveDriveKinematics kinematics = new SwerveDriveKinematics(SwerveConstants.MODULE_LOCATIONS);
    SwerveSetpointGenerator generator = new SwerveSetpointGenerator(kinematics);

    SwerveSetpointGenerator.KinematicLimits limits = new SwerveSetpointGenerator.KinematicLimits();
    limits.maxDriveVelocity = 4.0;
    limits.maxDriveAcceleration = 20.0;
    limits.maxSteeringVelocity = Math.PI * 4;

    SwerveModuleState[] initialStates =
        new SwerveModuleState[] {
          new SwerveModuleState(1.0, new Rotation2d()),
          new SwerveModuleState(1.0, new Rotation2d()),
          new SwerveModuleState(1.0, new Rotation2d()),
          new SwerveModuleState(1.0, new Rotation2d())
        };

    SwerveSetpointGenerator.SwerveSetpoint prevState =
        new SwerveSetpointGenerator.SwerveSetpoint(new ChassisSpeeds(1, 0, 0), initialStates);

    // Command EXACT opposite direction. Module heading optimization should flip wheel speeds
    // negative instead of rotating 180!
    SwerveSetpointGenerator.SwerveSetpoint newState =
        generator.generateSetpoint(limits, prevState, new ChassisSpeeds(-1.0, 0.0, 0), 0.02);

    // Because of 254's kinematics safety limits, instead of rotating 180 or reversing instantly,
    // the system forces a stop (decelerating towards origin) before flipping headings when extreme
    // vectors overlap.
    assertTrue(
        newState.moduleStates[0].speedMetersPerSecond < 1.0,
        "Did not decelerate before heading flip! " + newState.moduleStates[0].speedMetersPerSecond);

    // Feed it back recursively a few times to hit the flip-heading logic and negative incoming
  }

  @Test
  public void testNegativePrevSpeedHeadingFlip() {
    SwerveDriveKinematics kinematics = new SwerveDriveKinematics(SwerveConstants.MODULE_LOCATIONS);
    SwerveSetpointGenerator generator = new SwerveSetpointGenerator(kinematics);
    SwerveSetpointGenerator.KinematicLimits limits = new SwerveSetpointGenerator.KinematicLimits();
    limits.maxDriveVelocity = 4.0;
    limits.maxDriveAcceleration = 20.0;
    limits.maxSteeringVelocity = Math.PI * 4;

    SwerveModuleState[] negativeStates =
        new SwerveModuleState[] {
          new SwerveModuleState(-1.0, new Rotation2d()),
          new SwerveModuleState(-1.0, new Rotation2d()),
          new SwerveModuleState(-1.0, new Rotation2d()),
          new SwerveModuleState(-1.0, new Rotation2d())
        };

    SwerveSetpointGenerator.SwerveSetpoint prevState =
        new SwerveSetpointGenerator.SwerveSetpoint(new ChassisSpeeds(-1, 0, 0), negativeStates);

    SwerveSetpointGenerator.SwerveSetpoint newState =
        generator.generateSetpoint(limits, prevState, new ChassisSpeeds(1.0, 0.0, 0), 0.02);

    assertNotNull(newState);
  }

  @Test
  public void testSingularityCrossing() {
    SwerveDriveKinematics kinematics = new SwerveDriveKinematics(SwerveConstants.MODULE_LOCATIONS);
    SwerveSetpointGenerator generator = new SwerveSetpointGenerator(kinematics);
    SwerveSetpointGenerator.KinematicLimits limits = new SwerveSetpointGenerator.KinematicLimits();
    limits.maxDriveVelocity = 4.0;
    limits.maxDriveAcceleration = 1000.0;
    limits.maxSteeringVelocity = Math.PI * 4;

    // Goal is to rotate at -1 rad/s.
    // However, to prevent `allModulesShouldFlip` from returning TRUE, we append a small translation
    // to ONE module's axis? No, actually since the modules are centered around robot origin, pure
    // rotation reversal means all modules perfectly reverse their individual vectors.
    // So allModulesShouldFlip WOULD be true!
    // We must command a rotation around an OFF-CENTER point (e.g. at Module 0).
    // Around Module 0: Module 0 velocity is (0,0). Mod 1, 2, 3 have large velocities.
    double x = SwerveConstants.MODULE_LOCATIONS[0].getX();
    double y = SwerveConstants.MODULE_LOCATIONS[0].getY();

    // Rotating around Module 0 means chassis moves with vx = omega * y, vy = -omega * x
    ChassisSpeeds rotAroundMod0Plus = new ChassisSpeeds(y, -x, 1.0);

    // Ensure Mod 0 doesn't just bypass the epsilon check entirely by giving it a miniscule heading
    // difference. Actually we just generate a valid setpoint first so the internal cache is primed.
    SwerveSetpointGenerator.SwerveSetpoint primedState =
        generator.generateSetpoint(
            limits,
            new SwerveSetpointGenerator.SwerveSetpoint(
                new ChassisSpeeds(),
                new SwerveModuleState[] {
                  new SwerveModuleState(),
                  new SwerveModuleState(),
                  new SwerveModuleState(),
                  new SwerveModuleState()
                }),
            rotAroundMod0Plus,
            0.02);

    ChassisSpeeds rotAroundMod0Minus = new ChassisSpeeds(-y, x, -1.0);

    // By reversing the rotation entirely:
    // Module 0 stays at zero velocity (doesn't rotate necessarily, preventing
    // allModulesShouldFlip).
    // Modules 1, 2, 3 must exactly invert their velocities, passing through (0,0) in the bisection.
    SwerveSetpointGenerator.SwerveSetpoint newState =
        generator.generateSetpoint(limits, primedState, rotAroundMod0Minus, 0.02);

    assertNotNull(newState);
  }

  @Test
  public void testSteerAroundModule() {
    SwerveDriveKinematics kinematics = new SwerveDriveKinematics(SwerveConstants.MODULE_LOCATIONS);
    SwerveSetpointGenerator generator = new SwerveSetpointGenerator(kinematics);

    SwerveSetpointGenerator.KinematicLimits limits = new SwerveSetpointGenerator.KinematicLimits();
    limits.maxDriveVelocity = 4.0;
    limits.maxDriveAcceleration = 20.0;
    limits.maxSteeringVelocity = Math.PI * 4;

    SwerveModuleState[] initialStates =
        new SwerveModuleState[] {
          new SwerveModuleState(0.0, new Rotation2d()),
          new SwerveModuleState(0.0, new Rotation2d()),
          new SwerveModuleState(0.0, new Rotation2d()),
          new SwerveModuleState(0.0, new Rotation2d())
        };

    SwerveSetpointGenerator.SwerveSetpoint prevState =
        new SwerveSetpointGenerator.SwerveSetpoint(new ChassisSpeeds(0, 0, 0), initialStates);

    double x = SwerveConstants.MODULE_LOCATIONS[0].getX();
    double y = SwerveConstants.MODULE_LOCATIONS[0].getY();
    double omega = 1.0;
    ChassisSpeeds rotateAround0 = new ChassisSpeeds(omega * y, -omega * x, omega);

    SwerveSetpointGenerator.SwerveSetpoint newState =
        generator.generateSetpoint(limits, prevState, rotateAround0, 0.02);

    assertEquals(0.0, newState.moduleStates[0].speedMetersPerSecond, 0.001);
  }
}
