package com.marslib.vision;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Transform3d;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import org.photonvision.EstimatedRobotPose;
import org.photonvision.PhotonCamera;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.PhotonPoseEstimator.PoseStrategy;
import org.photonvision.simulation.PhotonCameraSim;
import org.photonvision.simulation.SimCameraProperties;
import org.photonvision.simulation.VisionSystemSim;

/**
 * Desktop simulation implementation of {@link AprilTagVisionIO} using PhotonVision's simulation
 * framework.
 *
 * <p>Renders synthetic camera views of the AprilTag field layout from the robot's true pose,
 * injecting realistic calibration error, latency, and optional occlusion dropout to stress-test the
 * pose estimator fusion pipeline without physical hardware.
 */
public class AprilTagVisionIOSim implements AprilTagVisionIO {
  private static final Pose3d[] EMPTY_POSES = new Pose3d[0];
  private static final double[] EMPTY_DOUBLES = new double[0];
  private static final int[] EMPTY_INTS = new int[0];

  private static VisionSystemSim visionSim;
  private static AprilTagFieldLayout fieldLayout;

  /**
   * Resets the shared VisionSystemSim singleton. Must be called in test {@code @BeforeEach} to
   * prevent stale camera state from leaking between test classes.
   */
  @SuppressWarnings("PMD.NullAssignment")
  public static void resetSimulation() {
    visionSim = null;
    fieldLayout = null;
  }

  private final PhotonCamera camera;
  private final PhotonCameraSim cameraSim;
  private final PhotonPoseEstimator poseEstimator;
  private final Supplier<Pose2d> poseSupplier;
  private final Transform3d robotToCamera;

  private static final int MAX_RESULTS = 8;
  private final Pose3d[][] poseCaches = new Pose3d[MAX_RESULTS + 1][];
  private final double[][] timestampCaches = new double[MAX_RESULTS + 1][];
  private final int[][] tagCountCaches = new int[MAX_RESULTS + 1][];
  private final double[][] distanceCaches = new double[MAX_RESULTS + 1][];
  private final double[][] ambiguityCaches = new double[MAX_RESULTS + 1][];
  private final FrustumVisualizer frustumVisualizer = new FrustumVisualizer(70.0, 50.0, 4.0);

  private static double lastVisionSimUpdate = -1.0;

  @SuppressWarnings({"PMD.AssignmentToNonFinalStatic", "StaticAssignmentInConstructor"})
  public AprilTagVisionIOSim(
      String cameraName, Transform3d robotToCamera, Supplier<Pose2d> poseSupplier) {
    this.poseSupplier = poseSupplier;
    this.robotToCamera = robotToCamera;

    for (int i = 0; i <= MAX_RESULTS; i++) {
      poseCaches[i] = new Pose3d[i];
      timestampCaches[i] = new double[i];
      tagCountCaches[i] = new int[i];
      distanceCaches[i] = new double[i];
      ambiguityCaches[i] = new double[i];
    }

    // Initialize global vision sim once
    if (visionSim == null) {
      visionSim = new VisionSystemSim("main");
      try {
        fieldLayout =
            AprilTagFieldLayout.loadFromResource(AprilTagFields.k2026RebuiltWelded.m_resourceFile);
      } catch (Exception e) {
        fieldLayout = new AprilTagFieldLayout(new java.util.ArrayList<>(), 16.54, 8.21);
      }
      visionSim.addAprilTags(fieldLayout);
    }

    camera = new PhotonCamera(cameraName);

    // Setup camera sim properties
    SimCameraProperties cameraProp = new SimCameraProperties();
    cameraProp.setCalibration(960, 720, edu.wpi.first.math.geometry.Rotation2d.fromDegrees(90));
    cameraProp.setCalibError(0.25, 0.08); // Simulate realistic calibration errors
    cameraProp.setFPS(20); // 20 FPS simulated
    cameraProp.setAvgLatencyMs(35);
    cameraProp.setLatencyStdDevMs(15);

    cameraSim = new PhotonCameraSim(camera, cameraProp);

    visionSim.addCamera(cameraSim, robotToCamera);

    @SuppressWarnings("removal")
    PhotonPoseEstimator tmpEstimator =
        new PhotonPoseEstimator(
            fieldLayout, PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR, robotToCamera);
    poseEstimator = tmpEstimator;
  }

  @SuppressWarnings("removal")
  @Override
  public void updateInputs(AprilTagVisionIOInputs inputs) {
    // Update simulation view from current true pose only once per physical loop
    double currentTime = edu.wpi.first.wpilibj.Timer.getFPGATimestamp();
    if (currentTime > lastVisionSimUpdate) {
      visionSim.update(poseSupplier.get());
      lastVisionSimUpdate = currentTime;
    }

    // Generate FOV visualizer based on true physical position + mounting location
    inputs.cameraFrustum =
        frustumVisualizer.update(new Pose3d(poseSupplier.get()).plus(robotToCamera));

    var results = camera.getAllUnreadResults();

    boolean simulatedOcclusion = false;
    if (frc.robot.constants.SimulationConstants.ENABLE_VISION_OCCLUSION) {
      simulatedOcclusion =
          ThreadLocalRandom.current().nextDouble()
              < frc.robot.constants.SimulationConstants.VISION_OCCLUSION_DROP_PROBABILITY;
    }

    int validCount = 0;
    int maxProcess = Math.min(results.size(), MAX_RESULTS);

    if (maxProcess > 0 && !simulatedOcclusion) {
      Pose3d[] pCache = poseCaches[maxProcess];
      double[] tCache = timestampCaches[maxProcess];
      int[] tcCache = tagCountCaches[maxProcess];
      double[] dCache = distanceCaches[maxProcess];
      double[] aCache = ambiguityCaches[maxProcess];

      for (int i = 0; i < maxProcess; i++) {
        var result = results.get(i);
        if (result.hasTargets()) {
          Optional<EstimatedRobotPose> estimatedPose = poseEstimator.update(result);
          if (estimatedPose.isPresent()) {
            EstimatedRobotPose pose = estimatedPose.get();
            int size = result.getTargets().size();
            if (size > 0) {
              pCache[validCount] = pose.estimatedPose;
              tCache[validCount] = pose.timestampSeconds;
              tcCache[validCount] = size;

              double avgDist = 0.0;
              double avgAmbiguity = 0.0;

              var targets = result.getTargets();
              for (int t = 0; t < size; t++) {
                var target = targets.get(t);
                avgDist += target.getBestCameraToTarget().getTranslation().getNorm();
                avgAmbiguity += target.getPoseAmbiguity();
              }

              dCache[validCount] = avgDist / size;
              aCache[validCount] = avgAmbiguity / size;
              validCount++;
            }
          }
        }
      }

      if (validCount > 0) {
        inputs.estimatedPoses = poseCaches[validCount];
        inputs.timestamps = timestampCaches[validCount];
        inputs.tagCounts = tagCountCaches[validCount];
        inputs.averageDistancesMeters = distanceCaches[validCount];
        inputs.ambiguities = ambiguityCaches[validCount];
        return;
      }
    }

    inputs.estimatedPoses = EMPTY_POSES;
    inputs.timestamps = EMPTY_DOUBLES;
    inputs.tagCounts = EMPTY_INTS;
    inputs.averageDistancesMeters = EMPTY_DOUBLES;
    inputs.ambiguities = EMPTY_DOUBLES;
  }
}
