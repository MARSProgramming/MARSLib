package com.marslib.swerve;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Twist2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Takes a prior setpoint (ChassisSpeeds), a desired setpoint (from a driver, or from a path
 * follower), and outputs a new setpoint that respects all of the kinematic constraints on module
 * rotation speed and wheel velocity/acceleration. By generating a new setpoint every iteration, the
 * robot will converge to the desired setpoint quickly while avoiding any intermediate state that is
 * kinematically infeasible (and can result in wheel slip or robot heading drift as a result).
 */
public class SwerveSetpointGenerator {
  public static class KinematicLimits {
    public double maxDriveVelocity; // m/s
    public double maxDriveAcceleration; // m/s^2
    public double maxSteeringVelocity; // rad/s
  }

  public static class SwerveSetpoint {
    public ChassisSpeeds chassisSpeeds;
    public SwerveModuleState[] moduleStates;

    public SwerveSetpoint(ChassisSpeeds chassisSpeeds, SwerveModuleState... moduleStates) {
      this.chassisSpeeds = chassisSpeeds;
      this.moduleStates = moduleStates.clone();
    }
  }

  private final SwerveDriveKinematics kinematics;
  private static final double kEpsilon = 1e-12;

  public SwerveSetpointGenerator(final SwerveDriveKinematics kinematics) {
    this.kinematics = kinematics;
  }

  private boolean epsilonEquals(double a, double b) {
    return Math.abs(a - b) < kEpsilon;
  }

  private boolean twistEpsilonEquals(Twist2d a, Twist2d b, double epsilon) {
    return Math.abs(a.dx - b.dx) < epsilon
        && Math.abs(a.dy - b.dy) < epsilon
        && Math.abs(a.dtheta - b.dtheta) < epsilon;
  }

  private boolean flipHeading(Rotation2d prevToGoal) {
    return Math.abs(prevToGoal.getRadians()) > Math.PI / 2.0;
  }

  private double unwrapAngle(double ref, double angle) {
    double diff = angle - ref;
    if (diff > Math.PI) {
      return angle - 2.0 * Math.PI;
    } else if (diff < -Math.PI) {
      return angle + 2.0 * Math.PI;
    } else {
      return angle;
    }
  }

  @FunctionalInterface
  private interface Function2d {
    double f(double x, double y);
  }

  private double findRoot(
      Function2d func,
      double x0,
      double y0,
      double f0,
      double x1,
      double y1,
      double f1,
      int iterationsLeft) {
    if (iterationsLeft < 0 || epsilonEquals(f0, f1)) {
      return 1.0;
    }
    double sGuess = Math.max(0.0, Math.min(1.0, -f0 / (f1 - f0)));
    double xGuess = (x1 - x0) * sGuess + x0;
    double yGuess = (y1 - y0) * sGuess + y0;
    double fGuess = func.f(xGuess, yGuess);
    if (Math.signum(f0) == Math.signum(fGuess)) {
      return sGuess
          + (1.0 - sGuess) * findRoot(func, xGuess, yGuess, fGuess, x1, y1, f1, iterationsLeft - 1);
    } else {
      return sGuess * findRoot(func, x0, y0, f0, xGuess, yGuess, fGuess, iterationsLeft - 1);
    }
  }

  protected double findSteeringMaxS(
      double x0,
      double y0,
      double f0,
      double x1,
      double y1,
      double f1,
      double maxDeviation,
      int maxIterations) {
    double f1Unwrapped = unwrapAngle(f0, f1);
    double diff = f1Unwrapped - f0;
    if (Math.abs(diff) <= maxDeviation) {
      return 1.0;
    }
    double offset = f0 + Math.signum(diff) * maxDeviation;
    Function2d func =
        (x, y) -> {
          return unwrapAngle(f0, Math.atan2(y, x)) - offset;
        };
    return findRoot(func, x0, y0, f0 - offset, x1, y1, f1Unwrapped - offset, maxIterations);
  }

  protected double findDriveMaxS(
      double x0,
      double y0,
      double f0,
      double x1,
      double y1,
      double f1,
      double maxVelStep,
      int maxIterations) {
    double diff = f1 - f0;
    if (Math.abs(diff) <= maxVelStep) {
      return 1.0;
    }
    double offset = f0 + Math.signum(diff) * maxVelStep;
    Function2d func =
        (x, y) -> {
          return Math.hypot(x, y) - offset;
        };
    return findRoot(func, x0, y0, f0 - offset, x1, y1, f1 - offset, maxIterations);
  }

  public SwerveSetpoint generateSetpoint(
      final KinematicLimits limits,
      final SwerveSetpoint prevSetpoint,
      ChassisSpeeds desiredState,
      double dt) {
    final Translation2d[] modules = kinematics.getModules();

    ChassisSpeeds workingDesiredState = desiredState;
    SwerveModuleState[] desiredModuleState = kinematics.toSwerveModuleStates(workingDesiredState);
    if (limits.maxDriveVelocity > 0.0) {
      SwerveDriveKinematics.desaturateWheelSpeeds(desiredModuleState, limits.maxDriveVelocity);
      workingDesiredState = kinematics.toChassisSpeeds(desiredModuleState);
    }

    boolean needToSteer = true;
    Twist2d dsTwist =
        new Twist2d(
            workingDesiredState.vxMetersPerSecond,
            workingDesiredState.vyMetersPerSecond,
            workingDesiredState.omegaRadiansPerSecond);
    if (twistEpsilonEquals(dsTwist, new Twist2d(), kEpsilon)) {
      needToSteer = false;
      for (int i = 0; i < modules.length; ++i) {
        desiredModuleState[i].angle = prevSetpoint.moduleStates[i].angle;
        desiredModuleState[i].speedMetersPerSecond = 0.0;
      }
    }

    double[] prevVx = new double[modules.length];
    double[] prevVy = new double[modules.length];
    Rotation2d[] prevHeading = new Rotation2d[modules.length];
    double[] desiredVx = new double[modules.length];
    double[] desiredVy = new double[modules.length];
    Rotation2d[] desiredHeading = new Rotation2d[modules.length];
    boolean allModulesShouldFlip = true;

    for (int i = 0; i < modules.length; ++i) {
      prevVx[i] =
          prevSetpoint.moduleStates[i].angle.getCos()
              * prevSetpoint.moduleStates[i].speedMetersPerSecond;
      prevVy[i] =
          prevSetpoint.moduleStates[i].angle.getSin()
              * prevSetpoint.moduleStates[i].speedMetersPerSecond;
      prevHeading[i] = prevSetpoint.moduleStates[i].angle;
      if (prevSetpoint.moduleStates[i].speedMetersPerSecond < 0.0) {
        prevHeading[i] = prevHeading[i].rotateBy(Rotation2d.fromDegrees(180));
      }
      desiredVx[i] =
          desiredModuleState[i].angle.getCos() * desiredModuleState[i].speedMetersPerSecond;
      desiredVy[i] =
          desiredModuleState[i].angle.getSin() * desiredModuleState[i].speedMetersPerSecond;
      desiredHeading[i] = desiredModuleState[i].angle;
      if (desiredModuleState[i].speedMetersPerSecond < 0.0) {
        desiredHeading[i] = desiredHeading[i].rotateBy(Rotation2d.fromDegrees(180));
      }
      if (allModulesShouldFlip) {
        double requiredRotationRad =
            Math.abs(prevHeading[i].unaryMinus().rotateBy(desiredHeading[i]).getRadians());
        if (requiredRotationRad < Math.PI / 2.0) {
          allModulesShouldFlip = false;
        }
      }
    }

    Twist2d prevTwist =
        new Twist2d(
            prevSetpoint.chassisSpeeds.vxMetersPerSecond,
            prevSetpoint.chassisSpeeds.vyMetersPerSecond,
            prevSetpoint.chassisSpeeds.omegaRadiansPerSecond);
    if (allModulesShouldFlip
        && !twistEpsilonEquals(prevTwist, new Twist2d(), kEpsilon)
        && !twistEpsilonEquals(dsTwist, new Twist2d(), kEpsilon)) {
      return generateSetpoint(limits, prevSetpoint, new ChassisSpeeds(), dt);
    }

    double dx =
        workingDesiredState.vxMetersPerSecond - prevSetpoint.chassisSpeeds.vxMetersPerSecond;
    double dy =
        workingDesiredState.vyMetersPerSecond - prevSetpoint.chassisSpeeds.vyMetersPerSecond;
    double dtheta =
        workingDesiredState.omegaRadiansPerSecond
            - prevSetpoint.chassisSpeeds.omegaRadiansPerSecond;

    double minS = 1.0;

    List<Optional<Rotation2d>> overrideSteering = new ArrayList<>(modules.length);
    final double maxThetaStep = dt * limits.maxSteeringVelocity;

    for (int i = 0; i < modules.length; ++i) {
      if (!needToSteer) {
        overrideSteering.add(Optional.of(prevSetpoint.moduleStates[i].angle));
        continue;
      }
      overrideSteering.add(Optional.empty());
      if (epsilonEquals(prevSetpoint.moduleStates[i].speedMetersPerSecond, 0.0)) {
        if (epsilonEquals(desiredModuleState[i].speedMetersPerSecond, 0.0)) {
          overrideSteering.set(i, Optional.of(prevSetpoint.moduleStates[i].angle));
          continue;
        }

        var necessaryRotation =
            prevSetpoint.moduleStates[i].angle.unaryMinus().rotateBy(desiredModuleState[i].angle);
        if (flipHeading(necessaryRotation)) {
          necessaryRotation = necessaryRotation.rotateBy(Rotation2d.fromRadians(Math.PI));
        }
        final double numStepsNeeded = Math.abs(necessaryRotation.getRadians()) / maxThetaStep;

        if (numStepsNeeded <= 1.0) {
          overrideSteering.set(i, Optional.of(desiredModuleState[i].angle));
          continue;
        } else {
          overrideSteering.set(
              i,
              Optional.of(
                  prevSetpoint.moduleStates[i].angle.rotateBy(
                      Rotation2d.fromRadians(
                          Math.signum(necessaryRotation.getRadians()) * maxThetaStep))));
          minS = 0.0;
          continue;
        }
      }
      if (minS == 0.0) {
        continue;
      }

      final int kMaxIterations = 8;
      double s =
          findSteeringMaxS(
              prevVx[i],
              prevVy[i],
              prevHeading[i].getRadians(),
              desiredVx[i],
              desiredVy[i],
              desiredHeading[i].getRadians(),
              maxThetaStep,
              kMaxIterations);
      minS = Math.min(minS, s);
    }

    final double maxVelStep = dt * limits.maxDriveAcceleration;
    for (int i = 0; i < modules.length; ++i) {
      if (minS == 0.0) {
        break;
      }
      double vxMinS = minS == 1.0 ? desiredVx[i] : (desiredVx[i] - prevVx[i]) * minS + prevVx[i];
      double vyMinS = minS == 1.0 ? desiredVy[i] : (desiredVy[i] - prevVy[i]) * minS + prevVy[i];

      final int kMaxIterations = 10;
      double s =
          minS
              * findDriveMaxS(
                  prevVx[i],
                  prevVy[i],
                  Math.hypot(prevVx[i], prevVy[i]),
                  vxMinS,
                  vyMinS,
                  Math.hypot(vxMinS, vyMinS),
                  maxVelStep,
                  kMaxIterations);
      minS = Math.min(minS, s);
    }

    ChassisSpeeds retSpeeds =
        new ChassisSpeeds(
            prevSetpoint.chassisSpeeds.vxMetersPerSecond + minS * dx,
            prevSetpoint.chassisSpeeds.vyMetersPerSecond + minS * dy,
            prevSetpoint.chassisSpeeds.omegaRadiansPerSecond + minS * dtheta);
    var retStates = kinematics.toSwerveModuleStates(retSpeeds);
    for (int i = 0; i < modules.length; ++i) {
      final var maybeOverride = overrideSteering.get(i);
      if (maybeOverride.isPresent()) {
        var override = maybeOverride.get();
        if (flipHeading(retStates[i].angle.unaryMinus().rotateBy(override))) {
          retStates[i].speedMetersPerSecond *= -1.0;
        }
        retStates[i].angle = override;
      }
      final var deltaRotation =
          prevSetpoint.moduleStates[i].angle.unaryMinus().rotateBy(retStates[i].angle);
      if (flipHeading(deltaRotation)) {
        retStates[i].angle = retStates[i].angle.rotateBy(Rotation2d.fromDegrees(180));
        retStates[i].speedMetersPerSecond *= -1.0;
      }
    }
    return new SwerveSetpoint(retSpeeds, retStates);
  }
}
