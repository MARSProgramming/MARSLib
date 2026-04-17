package com.marslib.simulation;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Rectangle;

/**
 * Encapsulates the native dyn4j physics body for the Swerve Drive. Provides a global traction model
 * by capping the maximum velocity delta based on the assumed coefficient of static friction.
 */
public class SwerveChassisPhysics {
  private final Body body;
  private final double maxAccelerationMps2;
  private final edu.wpi.first.math.geometry.Translation2d[] moduleLocations;
  private final edu.wpi.first.math.geometry.Transform2d[] moduleTransforms;

  // Pre-allocated pose cache to avoid per-tick Pose2d allocation (D-02)
  private Pose2d cachedPose = new Pose2d();

  private double currentSimPitch = 0.0;
  private double currentSimRoll = 0.0;

  public SwerveChassisPhysics(
      double massKg,
      double bumperWidthMeters,
      double bumperLengthMeters,
      double staticFrictionCoef,
      edu.wpi.first.math.geometry.Translation2d... moduleLocations) {
    this.moduleLocations = moduleLocations.clone();
    this.moduleTransforms = new edu.wpi.first.math.geometry.Transform2d[moduleLocations.length];
    for (int i = 0; i < moduleLocations.length; i++) {
      this.moduleTransforms[i] =
          new edu.wpi.first.math.geometry.Transform2d(moduleLocations[i], new Rotation2d());
    }
    body = new Body();
    // In dyn4j, Geometry.createRectangle centers on (0,0)
    Rectangle rectangle = Geometry.createRectangle(bumperLengthMeters, bumperWidthMeters);
    BodyFixture fixture = new BodyFixture(rectangle);

    // Non-bouncy, high friction bumpers
    fixture.setRestitution(0.05);
    fixture.setFriction(0.8);
    // Area = length * width, so density ensures total mass is correct
    fixture.setDensity(massKg / (bumperLengthMeters * bumperWidthMeters));
    body.addFixture(fixture);

    body.setMass(MassType.NORMAL);

    // CRITICAL: Disable auto-sleeping so dyn4j never skips this body during world.step().
    // Without this, the body falls asleep when velocity reaches zero, and subsequent
    // setLinearVelocity() calls do NOT wake it — causing the robot to appear frozen.
    body.setAtRestDetectionEnabled(false);

    // We actively control the velocity, so we don't want natural damping slowing us down
    body.setLinearDamping(0.0);
    body.setAngularDamping(0.0);

    // a = mu * g
    maxAccelerationMps2 = staticFrictionCoef * 9.81;
  }

  public void setPose(Pose2d pose) {
    body.getTransform().setTranslation(pose.getX(), pose.getY());
    body.getTransform().setRotation(pose.getRotation().getRadians());
    body.setLinearVelocity(0, 0);
    body.setAngularVelocity(0);
  }

  public Pose2d getPose() {
    cachedPose =
        new Pose2d(
            body.getTransform().getTranslationX(),
            body.getTransform().getTranslationY(),
            new Rotation2d(body.getTransform().getRotationAngle()));
    return cachedPose;
  }

  public double getSimPitch() {
    return currentSimPitch;
  }

  public double getSimRoll() {
    return currentSimRoll;
  }

  public Body getBody() {
    return body;
  }

  /**
   * Applies the theoretical ChassisSpeeds to the physics body. If the requested change in velocity
   * exceeds the tires' grip limit (max acceleration), the robot mathematically slips.
   */
  public void applyKinematicSpeeds(ChassisSpeeds requestedSpeeds, double dtSeconds) {
    // 4-Wheel Raycast for Terrain
    double[] zHeights = new double[4];
    int wheelsOnBump = 0;

    // Front-Left, Front-Right, Back-Left, Back-Right are standard layout
    Pose2d currentPose = getPose();

    for (int i = 0; i < 4; i++) {
      edu.wpi.first.math.geometry.Translation2d fieldPos =
          currentPose.transformBy(moduleTransforms[i]).getTranslation();
      zHeights[i] = MARSPhysicsWorld.getInstance().getTerrainZHeight(fieldPos, "TerrainBump");
      if (zHeights[i] > 0.001) {
        wheelsOnBump++;
      }
    }

    // Simple 3D plane approximation
    double wheelbase = Math.abs(moduleLocations[0].getX() - moduleLocations[2].getX());
    double trackwidth = Math.abs(moduleLocations[0].getY() - moduleLocations[1].getY());

    // Pitch is difference between front and back
    double frontZ = (zHeights[0] + zHeights[1]) / 2.0;
    double backZ = (zHeights[2] + zHeights[3]) / 2.0;
    currentSimPitch =
        -Math.atan2(frontZ - backZ, wheelbase); // negative because nose up is negative pitch

    // Roll is difference between left and right
    double leftZ = (zHeights[0] + zHeights[2]) / 2.0;
    double rightZ = (zHeights[1] + zHeights[3]) / 2.0;
    currentSimRoll = Math.atan2(leftZ - rightZ, trackwidth);

    // Resistance modifier
    double speedPenalty = Math.pow(0.85, wheelsOnBump);

    double currentVx = body.getLinearVelocity().x;
    double currentVy = body.getLinearVelocity().y;

    double cos = Math.cos(body.getTransform().getRotationAngle());
    double sin = Math.sin(body.getTransform().getRotationAngle());
    double fieldTargetVx =
        (requestedSpeeds.vxMetersPerSecond * cos - requestedSpeeds.vyMetersPerSecond * sin)
            * speedPenalty;
    double fieldTargetVy =
        (requestedSpeeds.vxMetersPerSecond * sin + requestedSpeeds.vyMetersPerSecond * cos)
            * speedPenalty;

    double dVx = fieldTargetVx - currentVx;
    double dVy = fieldTargetVy - currentVy;

    double deltaVTargetMagnitude = Math.hypot(dVx, dVy);
    double maxDeltaV = maxAccelerationMps2 * dtSeconds;

    double actualDVX = dVx;
    double actualDVY = dVy;

    // Traction Control limits
    if (deltaVTargetMagnitude > maxDeltaV) {
      double scale = maxDeltaV / deltaVTargetMagnitude;
      actualDVX *= scale;
      actualDVY *= scale;
    }

    body.setLinearVelocity(currentVx + actualDVX, currentVy + actualDVY);
    body.setAngularVelocity(requestedSpeeds.omegaRadiansPerSecond);
  }
}
