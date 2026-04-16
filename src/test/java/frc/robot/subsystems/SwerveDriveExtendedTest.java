package frc.robot.subsystems;

import static org.junit.jupiter.api.Assertions.*;

import com.marslib.power.MARSPowerManager;
import com.marslib.power.PowerConfig;
import com.marslib.power.PowerIO;
import com.marslib.swerve.GyroIO;
import com.marslib.swerve.SwerveConfig;
import com.marslib.swerve.SwerveDrive;
import com.marslib.swerve.SwerveModule;
import com.marslib.swerve.SwerveModuleIOSim;
import com.marslib.testing.MARSTestHarness;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.Timer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Extended reliability tests for the MARSLib SwerveDrive implementation. Targets 3D pose estimation
 * and vision fusion logic to reach the 70% milestone.
 */
public class SwerveDriveExtendedTest {

  private static class MutableMockGyro implements GyroIO {
    public boolean connected = true;
    public double yaw = 0;
    public double pitch = Math.toRadians(10.0);
    public double roll = Math.toRadians(5.0);

    @Override
    public void updateInputs(GyroIOInputs inputs) {
      inputs.connected = connected;
      inputs.yawPositionRad = yaw;
      inputs.pitchPositionRad = pitch;
      inputs.rollPositionRad = roll;
    }
  }

  private SwerveDrive swerveDrive;
  private MutableMockGyro mockGyro;
  private SwerveConfig swerveConfig;
  private PowerConfig powerConfig;

  @BeforeEach
  public void setUp() {
    MARSTestHarness.reset();

    mockGyro = new MutableMockGyro();

    swerveConfig = MARSTestHarness.createSwerveConfig();
    powerConfig = MARSTestHarness.createPowerConfig();

    SwerveModule[] modules = new SwerveModule[4];
    for (int i = 0; i < 4; i++) {
      modules[i] = new SwerveModule(i, new SwerveModuleIOSim(i, swerveConfig), swerveConfig);
    }

    PowerIO spoofedVoltageIO =
        new PowerIO() {
          @Override
          public void updateInputs(PowerIOInputs inputs) {
            inputs.voltage = 12.0;
          }
        };

    swerveDrive =
        new SwerveDrive(
            modules, mockGyro, new MARSPowerManager(spoofedVoltageIO, powerConfig), swerveConfig);
  }

  @AfterEach
  public void tearDown() {
    MARSTestHarness.cleanup();
  }

  @Test
  public void testPose3dCalculations() {
    // Force a periodic update to process the gyro
    swerveDrive.periodic();

    Pose3d pose3d = swerveDrive.getPose3d();
    assertNotNull(pose3d);

    // Check that pitch and roll are correctly captured from the gyro
    assertEquals(
        10.0, pose3d.getRotation().getY() * (180.0 / Math.PI), 0.1, "Pitch should match gyro");
    assertEquals(
        5.0, pose3d.getRotation().getX() * (180.0 / Math.PI), 0.1, "Roll should match gyro");

    // Invalidate the cache by changing roll/pitch and forcing an update
    mockGyro.pitch = Math.toRadians(20.0);
    mockGyro.roll = Math.toRadians(15.0);
    swerveDrive.periodic();

    pose3d = swerveDrive.getPose3d();
    assertEquals(20.0, pose3d.getRotation().getY() * (180.0 / Math.PI), 0.1);
    assertEquals(15.0, pose3d.getRotation().getX() * (180.0 / Math.PI), 0.1);
  }

  @Test
  public void testResetPoseDisconnectedGyro() {
    mockGyro.connected = false;
    swerveDrive.periodic();
    swerveDrive.resetPose(new Pose2d(5, 5, Rotation2d.fromDegrees(45)));

    // Should adopt exactly 45 degrees since it fell back to parameter pose
    assertEquals(45.0, swerveDrive.getPose().getRotation().getDegrees(), 0.1);
  }

  @Test
  public void testVisionMeasurementInjection() {
    // 1. Set initial pose
    swerveDrive.resetPose(new Pose2d(0, 0, new Rotation2d()));
    swerveDrive.periodic();

    Pose2d initialPose = swerveDrive.getPose();

    // 2. Inject a high-confidence vision measurement at (2, 2)
    Pose2d visionPose = new Pose2d(2, 2, new Rotation2d());
    double timestamp = Timer.getFPGATimestamp();

    // Use low standard deviations for high confidence
    swerveDrive.addVisionMeasurement(
        visionPose, timestamp, edu.wpi.first.math.VecBuilder.fill(0.1, 0.1, 0.1));

    // 3. Process multiple updates to allow the pose estimator to converge
    for (int i = 0; i < 5; i++) {
      swerveDrive.periodic();
    }

    Pose2d finalPose = swerveDrive.getPose();

    // The final pose should have moved TOWARDS the vision pose
    assertTrue(finalPose.getX() > initialPose.getX(), "X should have increased toward vision");
    assertTrue(finalPose.getY() > initialPose.getY(), "Y should have increased toward vision");

    // Verify it didn't overshoot or teleport (it's a filter)
    assertTrue(finalPose.getX() < 2.1, "X should stay within reasonable bounds of vision");
  }
}
