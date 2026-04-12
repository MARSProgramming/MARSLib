package com.marslib.vision;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.marslib.swerve.GyroIO;
import com.marslib.swerve.SwerveDrive;
import com.marslib.swerve.SwerveModule;
import com.marslib.testing.MARSTestHarness;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.wpilibj.Timer;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MARSVisionExtendedTest {
  private SwerveDrive swerveDrive;
  private MARSVision vision;
  private MockAprilTagIO mockIO;

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
    mockIO = new MockAprilTagIO();
    vision = new MARSVision(swerveDrive, List.of(mockIO), List.of());
  }

  @Test
  public void testMultiCameraAprilTagAggregation() {
    // Inject a valid pose
    mockIO.currentPose = new Pose3d(5.0, 5.0, 0.0, new Rotation3d());
    mockIO.tagCount = 2;
    mockIO.timestamp = Timer.getFPGATimestamp();

    vision.periodic();

    // Verify the pose estimator was updated (best way is to check if getBestTargetTranslation
    // matches)
    assertTrue(vision.getBestTargetTranslation().isPresent());
    assertEquals(5.0, vision.getBestTargetTranslation().get().getX(), 0.01);
  }

  @Test
  public void testHighAmbiguityEstimatesAreRejected() {
    mockIO.currentPose = new Pose3d(5.0, 5.0, 0.0, new Rotation3d());
    mockIO.tagCount = 1;
    mockIO.ambiguity = 0.9; // Very high ambiguity
    mockIO.timestamp = Timer.getFPGATimestamp();

    vision.periodic();

    assertFalse(
        vision.getBestTargetTranslation().isPresent(), "High ambiguity pose should be rejected");
  }

  @Test
  public void testFieldBoundaryRejection() {
    // Pose way outside the field
    mockIO.currentPose = new Pose3d(100.0, 100.0, 0.0, new Rotation3d());
    mockIO.tagCount = 2;
    mockIO.timestamp = Timer.getFPGATimestamp();

    vision.periodic();

    assertFalse(
        vision.getBestTargetTranslation().isPresent(), "Out-of-bounds pose should be rejected");
  }

  @Test
  public void testZHeightHallucinationRejection() {
    // Flying robot hallucination
    mockIO.currentPose = new Pose3d(5.0, 5.0, 2.0, new Rotation3d());
    mockIO.tagCount = 2;
    mockIO.timestamp = Timer.getFPGATimestamp();

    vision.periodic();

    assertFalse(
        vision.getBestTargetTranslation().isPresent(), "High Z-height pose should be rejected");
  }

  @Test
  public void testImpactShockRejection() {
    // First loop to establish last angular velocity
    vision.periodic();

    // Simulate a massive angular acceleration (impact)
    // We override the swerveDrive's gyro inputs to simulate a high dOmega/dt

    // We need to inject this into SwerveDrive since MARSVision pulls from it
    // Actually, we can just use a separate SwerveDrive with a mocked GyroIO if we want to be clean
    // But let's just use the harness setup if possible.

    // Instead of messing with singletons, lets create a local SwerveDrive with a mocked GyroIO
    double[] mockYawRate = {0.0};
    GyroIO shockGyro =
        new GyroIO() {
          @Override
          public void updateInputs(GyroIOInputs inputs) {
            inputs.connected = true;
            inputs.yawVelocityRadPerSec = mockYawRate[0];
          }
        };

    SwerveModule[] modules = new SwerveModule[4];
    for (int i = 0; i < 4; i++) {
      modules[i] = new SwerveModule(i, new com.marslib.swerve.SwerveModuleIOSim(i));
    }
    com.marslib.power.MARSPowerManager powerManager =
        new com.marslib.power.MARSPowerManager(new com.marslib.power.PowerIO() {});

    SwerveDrive shockSwerve = new SwerveDrive(modules, shockGyro, powerManager);
    MARSVision shockVision = new MARSVision(shockSwerve, List.of(mockIO), List.of());

    // Estable base
    shockVision.periodic();

    // Trigger shock
    mockYawRate[0] = 500.0; // Huge jump
    mockIO.currentPose = new Pose3d(5.0, 5.0, 0.0, new Rotation3d());
    mockIO.timestamp = Timer.getFPGATimestamp();

    shockSwerve.periodic(); // Update gyro inputs
    shockVision.periodic(); // Process vision with new data

    assertFalse(
        shockVision.getBestTargetTranslation().isPresent(),
        "Vision should be rejected during impact shock");
  }

  private static class MockAprilTagIO implements AprilTagVisionIO {
    public Pose3d currentPose = null;
    public double timestamp = 0.0;
    public int tagCount = 0;
    public double avgDist = 1.0;
    public double ambiguity = 0.1;

    @Override
    public void updateInputs(AprilTagVisionIOInputs inputs) {
      if (currentPose != null) {
        inputs.estimatedPoses = new Pose3d[] {currentPose};
        inputs.timestamps = new double[] {timestamp};
        inputs.tagCounts = new int[] {tagCount};
        inputs.averageDistancesMeters = new double[] {avgDist};
        inputs.ambiguities = new double[] {ambiguity};
      } else {
        inputs.estimatedPoses = new Pose3d[0];
      }
    }
  }
}
