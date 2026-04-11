package com.marslib.vision;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;

/**
 * Frustum Visualizer for AdvantageScope 3D telemetry.
 *
 * <p>Intercepts the physical limits (FOV) of simulated cameras (e.g., Limelight) and structurally
 * casts their bounding boxes into a WPILib Pose3d array. This array is natively logged using
 * AdvantageKit and renders semi-transparent lines in the simulator showing mathematically exactly
 * where the camera can see.
 */
public class FrustumVisualizer {
  private final Transform3d tTopLeft;
  private final Transform3d tTopRight;
  private final Transform3d tBottomLeft;
  private final Transform3d tBottomRight;
  private final Pose3d[] cachedRenderBuffer = new Pose3d[11];

  /**
   * Generate an array of 5 3D poses representing a visual cone/frustum.
   *
   * @param horizontalFovDeg The horizontal FOV in degrees (e.g. Limelight 3 = 63.3)
   * @param verticalFovDeg The vertical FOV in degrees (e.g. Limelight 3 = 49.7)
   * @param rangeMeters How far out to draw the bounding rays in meters.
   */
  public FrustumVisualizer(double horizontalFovDeg, double verticalFovDeg, double rangeMeters) {
    double hFovRads = Math.toRadians(horizontalFovDeg / 2.0);
    double vFovRads = Math.toRadians(verticalFovDeg / 2.0);

    double leftPlaneY = rangeMeters * Math.tan(hFovRads);
    double rightPlaneY = -rangeMeters * Math.tan(hFovRads);
    double topPlaneZ = rangeMeters * Math.tan(vFovRads);
    double bottomPlaneZ = -rangeMeters * Math.tan(vFovRads);

    tTopLeft =
        new Transform3d(new Translation3d(rangeMeters, leftPlaneY, topPlaneZ), new Rotation3d());
    tTopRight =
        new Transform3d(new Translation3d(rangeMeters, rightPlaneY, topPlaneZ), new Rotation3d());
    tBottomLeft =
        new Transform3d(new Translation3d(rangeMeters, leftPlaneY, bottomPlaneZ), new Rotation3d());
    tBottomRight =
        new Transform3d(
            new Translation3d(rangeMeters, rightPlaneY, bottomPlaneZ), new Rotation3d());
  }

  @SuppressWarnings("PMD.MethodReturnsInternalArray")
  public Pose3d[] update(Pose3d cameraPose) {
    Pose3d p0 = cameraPose;
    Pose3d p1 = cameraPose.plus(tTopLeft);
    Pose3d p2 = cameraPose.plus(tTopRight);
    Pose3d p3 = cameraPose.plus(tBottomLeft);
    Pose3d p4 = cameraPose.plus(tBottomRight);

    cachedRenderBuffer[0] = p0;
    cachedRenderBuffer[1] = p1;
    cachedRenderBuffer[2] = p2;
    cachedRenderBuffer[3] = p4;
    cachedRenderBuffer[4] = p3;
    cachedRenderBuffer[5] = p1;
    cachedRenderBuffer[6] = p0;
    cachedRenderBuffer[7] = p2;
    cachedRenderBuffer[8] = p4;
    cachedRenderBuffer[9] = p0;
    cachedRenderBuffer[10] = p3;

    return cachedRenderBuffer;
  }
}
