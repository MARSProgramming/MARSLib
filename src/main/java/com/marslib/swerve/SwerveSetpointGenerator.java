/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.swerve;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModuleState;

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

  // Pre-allocated buffers to prevent GC churn in 250Hz loop
  private final double[] prevVx = new double[4];
  private final double[] prevVy = new double[4];
  private final Rotation2d[] prevHeading = new Rotation2d[4];
  private final double[] desiredVx = new double[4];
  private final double[] desiredVy = new double[4];
  private final Rotation2d[] desiredHeading = new Rotation2d[4];
  private final Rotation2d[] overrideSteeringCache =
      new Rotation2d[] {new Rotation2d(), new Rotation2d(), new Rotation2d(), new Rotation2d()};
  private final boolean[] overrideSteeringActive = new boolean[4];

  // Static zero speeds for recursive fallback — avoids allocation
  private static final ChassisSpeeds ZERO_CHASSIS_SPEEDS = new ChassisSpeeds();

  // Result cache to avoid SwerveSetpoint allocation
  private final SwerveSetpoint resultCache =
      new SwerveSetpoint(
          new ChassisSpeeds(),
          new SwerveModuleState[] {
            new SwerveModuleState(),
            new SwerveModuleState(),
            new SwerveModuleState(),
            new SwerveModuleState()
          });

  public SwerveSetpointGenerator(final SwerveDriveKinematics kinematics) {
    this.kinematics = kinematics;
    for (int i = 0; i < 4; i++) {
      prevHeading[i] = new Rotation2d();
      desiredHeading[i] = new Rotation2d();
    }
  }

  private boolean epsilonEquals(double a, double b) {
    return Math.abs(a - b) < kEpsilon;
  }

  /**
   * Checks if all three chassis speed components are effectively zero.
   *
   * @return true if vx, vy, and omega are all within epsilon of zero.
   */
  private boolean chassisSpeedsAreZero(ChassisSpeeds speeds, double epsilon) {
    return Math.abs(speeds.vxMetersPerSecond) < epsilon
        && Math.abs(speeds.vyMetersPerSecond) < epsilon
        && Math.abs(speeds.omegaRadiansPerSecond) < epsilon;
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

  /**
   * Computes the maximum positional scalar \( s_{max} \) for the steering rotation of a Swerve
   * Module.
   *
   * <p>Utilizes {@code findRoot} to determine the exact timestamp bound where the change in module
   * orientation \( d\theta \) precisely equals the physical limit {@code maxDeviation}. The
   * equation evaluates \( f(\theta) = \text{unwrap}(\theta) - \text{offset} = 0 \).
   *
   * @param x0 Prior structural X (Cosine mapping vector)
   * @param y0 Prior structural Y (Sine mapping vector)
   * @param f0 Previous rotation radians
   * @param x1 Desired structural X
   * @param y1 Desired structural Y
   * @param f1 Desired rotation radians
   * @param maxDeviation The structural Max Steering velocity * \( dt \)
   * @param maxIterations Bisection cut-off depth.
   * @return The maximum safe step multiplier scale.
   */
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

    // Iterative bisection to avoid lambda allocation
    double sLow = 0.0;
    double sHigh = 1.0;
    double fLow = f0 - offset;
    for (int i = 0; i < maxIterations; i++) {
      double sGuess = (sLow + sHigh) / 2.0;
      double xGuess = (x1 - x0) * sGuess + x0;
      double yGuess = (y1 - y0) * sGuess + y0;
      double fGuess = unwrapAngle(f0, Math.atan2(yGuess, xGuess)) - offset;

      if (Math.signum(fLow) == Math.signum(fGuess)) {
        sLow = sGuess;
        fLow = fGuess;
      } else {
        sHigh = sGuess;
      }
    }
    return sLow;
  }

  /**
   * Computes the maximum linear acceleration scalar \( s_{max} \) for the driving vector wheel slip
   * threshold.
   *
   * <p>Calculates the boundary threshold solving for: \( \sqrt{V_x^2 + V_y^2} - (\text{previous
   * magnitude} + \text{max allowed acceleration step}) = 0 \).
   *
   * @param x0 Prior magnitude X velocity
   * @param y0 Prior magnitude Y velocity
   * @param f0 Previous hypotenuse sum velocity
   * @param x1 Desired magnitude X velocity
   * @param y1 Desired magnitude Y velocity
   * @param f1 Desired hypotenuse sum velocity
   * @param maxVelStep Absolute hardcap velocity slip threshold (\( \mu \cdot 9.81 \cdot dt \))
   * @param maxIterations Limits Newton iteration overloop.
   * @return A restricted translation scalar multiplier [0.0, 1.0].
   */
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

    // Iterative bisection to avoid lambda allocation
    double sLow = 0.0;
    double sHigh = 1.0;
    double fLow = f0 - offset;
    for (int i = 0; i < maxIterations; i++) {
      double sGuess = (sLow + sHigh) / 2.0;
      double xGuess = (x1 - x0) * sGuess + x0;
      double yGuess = (y1 - y0) * sGuess + y0;
      double fGuess = Math.hypot(xGuess, yGuess) - offset;

      if (Math.signum(fLow) == Math.signum(fGuess)) {
        sLow = sGuess;
        fLow = fGuess;
      } else {
        sHigh = sGuess;
      }
    }
    return sLow;
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
    // Zero-allocation: inline epsilon check instead of allocating Twist2d objects
    if (chassisSpeedsAreZero(workingDesiredState, kEpsilon)) {
      needToSteer = false;
      for (int i = 0; i < modules.length; ++i) {
        desiredModuleState[i].angle = prevSetpoint.moduleStates[i].angle;
        desiredModuleState[i].speedMetersPerSecond = 0.0;
      }
    }

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

    // Zero-allocation: inline epsilon check instead of allocating Twist2d objects
    if (allModulesShouldFlip
        && !chassisSpeedsAreZero(prevSetpoint.chassisSpeeds, kEpsilon)
        && !chassisSpeedsAreZero(workingDesiredState, kEpsilon)) {
      return generateSetpoint(limits, prevSetpoint, ZERO_CHASSIS_SPEEDS, dt);
    }

    double dx =
        workingDesiredState.vxMetersPerSecond - prevSetpoint.chassisSpeeds.vxMetersPerSecond;
    double dy =
        workingDesiredState.vyMetersPerSecond - prevSetpoint.chassisSpeeds.vyMetersPerSecond;
    double dtheta =
        workingDesiredState.omegaRadiansPerSecond
            - prevSetpoint.chassisSpeeds.omegaRadiansPerSecond;

    double minS = 1.0;

    final double maxThetaStep = dt * limits.maxSteeringVelocity;

    for (int i = 0; i < modules.length; ++i) {
      if (!needToSteer) {
        overrideSteeringCache[i] = prevSetpoint.moduleStates[i].angle;
        overrideSteeringActive[i] = true;
        continue;
      }
      overrideSteeringActive[i] = false;
      if (epsilonEquals(prevSetpoint.moduleStates[i].speedMetersPerSecond, 0.0)) {
        if (epsilonEquals(desiredModuleState[i].speedMetersPerSecond, 0.0)) {
          overrideSteeringCache[i] = prevSetpoint.moduleStates[i].angle;
          overrideSteeringActive[i] = true;
          continue;
        }

        var necessaryRotation =
            prevSetpoint.moduleStates[i].angle.unaryMinus().rotateBy(desiredModuleState[i].angle);
        if (flipHeading(necessaryRotation)) {
          necessaryRotation = necessaryRotation.rotateBy(Rotation2d.fromRadians(Math.PI));
        }
        final double numStepsNeeded = Math.abs(necessaryRotation.getRadians()) / maxThetaStep;

        if (numStepsNeeded <= 1.0) {
          overrideSteeringCache[i] = desiredModuleState[i].angle;
          overrideSteeringActive[i] = true;
          continue;
        } else {
          overrideSteeringCache[i] =
              prevSetpoint.moduleStates[i].angle.rotateBy(
                  Rotation2d.fromRadians(
                      Math.signum(necessaryRotation.getRadians()) * maxThetaStep));
          overrideSteeringActive[i] = true;
          minS = 0.0;
          continue;
        }
      }
      if (minS == 0.0) {
        continue;
      }

      final int kMaxIterations = 8;
      double scaleFactor =
          findSteeringMaxS(
              prevVx[i],
              prevVy[i],
              prevHeading[i].getRadians(),
              desiredVx[i],
              desiredVy[i],
              desiredHeading[i].getRadians(),
              maxThetaStep,
              kMaxIterations);
      minS = Math.min(minS, scaleFactor);
    }

    final double maxVelStep = dt * limits.maxDriveAcceleration;
    for (int i = 0; i < modules.length; ++i) {
      if (minS == 0.0) {
        break;
      }
      double vxMinS = minS == 1.0 ? desiredVx[i] : (desiredVx[i] - prevVx[i]) * minS + prevVx[i];
      double vyMinS = minS == 1.0 ? desiredVy[i] : (desiredVy[i] - prevVy[i]) * minS + prevVy[i];

      final int kMaxIterations = 10;
      double scaleFactor =
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
      minS = Math.min(minS, scaleFactor);
    }

    resultCache.chassisSpeeds.vxMetersPerSecond =
        prevSetpoint.chassisSpeeds.vxMetersPerSecond + minS * dx;
    resultCache.chassisSpeeds.vyMetersPerSecond =
        prevSetpoint.chassisSpeeds.vyMetersPerSecond + minS * dy;
    resultCache.chassisSpeeds.omegaRadiansPerSecond =
        prevSetpoint.chassisSpeeds.omegaRadiansPerSecond + minS * dtheta;

    for (int i = 0; i < modules.length; i++) {
      double vx =
          resultCache.chassisSpeeds.vxMetersPerSecond
              - resultCache.chassisSpeeds.omegaRadiansPerSecond * modules[i].getY();
      double vy =
          resultCache.chassisSpeeds.vyMetersPerSecond
              + resultCache.chassisSpeeds.omegaRadiansPerSecond * modules[i].getX();

      resultCache.moduleStates[i].speedMetersPerSecond = Math.hypot(vx, vy);
      resultCache.moduleStates[i].angle = new Rotation2d(vx, vy);

      if (overrideSteeringActive[i]) {
        var override = overrideSteeringCache[i];
        if (flipHeading(resultCache.moduleStates[i].angle.unaryMinus().rotateBy(override))) {
          resultCache.moduleStates[i].speedMetersPerSecond *= -1.0;
        }
        resultCache.moduleStates[i].angle = override;
      }
      final var deltaRotation =
          prevSetpoint
              .moduleStates[i]
              .angle
              .unaryMinus()
              .rotateBy(resultCache.moduleStates[i].angle);
      if (flipHeading(deltaRotation)) {
        resultCache.moduleStates[i].angle =
            resultCache.moduleStates[i].angle.rotateBy(Rotation2d.fromDegrees(180));
        resultCache.moduleStates[i].speedMetersPerSecond *= -1.0;
      }
    }
    return resultCache;
  }
}
