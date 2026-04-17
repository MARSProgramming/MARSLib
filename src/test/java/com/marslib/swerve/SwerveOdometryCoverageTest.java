package com.marslib.swerve;

import static org.junit.jupiter.api.Assertions.*;

import com.marslib.power.MARSPowerManager;
import com.marslib.power.PowerConfig;
import com.marslib.power.PowerIOSim;
import com.marslib.simulation.MARSPhysicsWorld;
import com.marslib.testing.MARSTestHarness;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Targeted coverage tests for SwerveOdometry's previously-uncovered branches: gyro-disconnected
 * dead-reckoning fallback and empty odometryYawPositions array.
 */
public class SwerveOdometryCoverageTest {

  private SwerveModule[] modules;
  private SwerveConfig swerveConfig;

  @BeforeEach
  public void setUp() {
    MARSTestHarness.reset();

    swerveConfig = MARSTestHarness.createSwerveConfig();
    PowerConfig powerConfig = MARSTestHarness.createPowerConfig();
    MARSPowerManager powerManager = new MARSPowerManager(new PowerIOSim(powerConfig), powerConfig);

    modules = new SwerveModule[4];
    for (int i = 0; i < 4; i++) {
      modules[i] = new SwerveModule(i, new SwerveModuleIOSim(i, swerveConfig), swerveConfig);
    }
  }

  @Test
  public void testOdometryWithGyroDisconnected() {
    // Create a gyro that reports disconnected with empty yaw positions
    GyroIOInputsAutoLogged disconnectedInputs = new GyroIOInputsAutoLogged();
    disconnectedInputs.connected = false;
    disconnectedInputs.yawPositionRad = 0.0;
    disconnectedInputs.yawVelocityRadPerSec = 0.0;
    disconnectedInputs.pitchPositionRad = 0.0;
    disconnectedInputs.rollPositionRad = 0.0;
    disconnectedInputs.odometryYawPositions = new double[0];

    SwerveDriveKinematics kinematics = new SwerveDriveKinematics(swerveConfig.moduleLocations());

    SwerveOdometry odometry =
        new SwerveOdometry(
            kinematics,
            swerveConfig,
            new SwerveModulePosition(),
            new SwerveModulePosition(),
            new SwerveModulePosition(),
            new SwerveModulePosition());

    // Step the physics world so modules produce valid delta data
    MARSPhysicsWorld world = MARSPhysicsWorld.getInstance();
    for (int i = 0; i < 5; i++) {
      world.update(0.02);
      for (SwerveModule m : modules) {
        m.periodic();
      }
    }

    // Run odometry with disconnected gyro — exercises the dead-reckoning fallback
    odometry.updateOdometry(modules, disconnectedInputs);

    Pose2d pose = odometry.getPose();
    assertNotNull(pose, "Pose should be valid even with disconnected gyro");
  }

  @Test
  public void testOdometryWithConnectedGyroButEmptyYawArray() {
    // Connected gyro but empty odometryYawPositions — exercises fallback to
    // gyroInputs.yawPositionRad
    GyroIOInputsAutoLogged connectedEmptyYaw = new GyroIOInputsAutoLogged();
    connectedEmptyYaw.connected = true;
    connectedEmptyYaw.yawPositionRad = 0.5;
    connectedEmptyYaw.yawVelocityRadPerSec = 0.0;
    connectedEmptyYaw.pitchPositionRad = 0.0;
    connectedEmptyYaw.rollPositionRad = 0.0;
    connectedEmptyYaw.odometryYawPositions = new double[0]; // Empty!

    SwerveDriveKinematics kinematics = new SwerveDriveKinematics(swerveConfig.moduleLocations());

    SwerveOdometry odometry =
        new SwerveOdometry(
            kinematics,
            swerveConfig,
            new SwerveModulePosition(),
            new SwerveModulePosition(),
            new SwerveModulePosition(),
            new SwerveModulePosition());

    MARSPhysicsWorld world = MARSPhysicsWorld.getInstance();
    for (int i = 0; i < 5; i++) {
      world.update(0.02);
      for (SwerveModule m : modules) {
        m.periodic();
      }
    }

    odometry.updateOdometry(modules, connectedEmptyYaw);

    Pose2d pose = odometry.getPose();
    assertNotNull(pose, "Pose should be valid with connected gyro but empty yaw array");
  }

  @Test
  public void testGetPose3dCaching() {
    SwerveDriveKinematics kinematics = new SwerveDriveKinematics(swerveConfig.moduleLocations());

    SwerveOdometry odometry =
        new SwerveOdometry(
            kinematics,
            swerveConfig,
            new SwerveModulePosition(),
            new SwerveModulePosition(),
            new SwerveModulePosition(),
            new SwerveModulePosition());

    GyroIOInputsAutoLogged gyro = new GyroIOInputsAutoLogged();
    gyro.connected = true;
    gyro.rollPositionRad = 0.0;
    gyro.pitchPositionRad = 0.0;
    gyro.yawPositionRad = 0.0;
    gyro.odometryYawPositions = new double[] {0.0};

    // First call creates the cache
    var pose3d1 = odometry.getPose3d(gyro);
    assertNotNull(pose3d1);

    // Second identical call should return cached (no change)
    var pose3d2 = odometry.getPose3d(gyro);
    assertEquals(pose3d1.getX(), pose3d2.getX(), 0.001);

    // Change pitch to invalidate cache
    gyro.pitchPositionRad = 0.1;
    var pose3d3 = odometry.getPose3d(gyro);
    assertEquals(0.1, pose3d3.getRotation().getY(), 0.001, "Pitch should be updated");
  }
}
