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

  public SwerveChassisPhysics(
      double massKg,
      double bumperWidthMeters,
      double bumperLengthMeters,
      double staticFrictionCoef) {
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
    return new Pose2d(
        body.getTransform().getTranslationX(),
        body.getTransform().getTranslationY(),
        new Rotation2d(body.getTransform().getRotationAngle()));
  }

  public Body getBody() {
    return body;
  }

  /**
   * Applies the theoretical ChassisSpeeds to the physics body. If the requested change in velocity
   * exceeds the tires' grip limit (max acceleration), the robot mathematically slips.
   */
  public void applyKinematicSpeeds(ChassisSpeeds requestedSpeeds, double dtSeconds) {
    double currentVx = body.getLinearVelocity().x;
    double currentVy = body.getLinearVelocity().y;

    double dVx = requestedSpeeds.vxMetersPerSecond - currentVx;
    double dVy = requestedSpeeds.vyMetersPerSecond - currentVy;

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
