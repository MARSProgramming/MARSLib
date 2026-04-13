package com.marslib.swerve;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.kinematics.ChassisSpeeds;

/**
 * Pure-function joystick-to-ChassisSpeeds math for the teleop drive pipeline.
 *
 * <p>This class exists so the exact same math is used in {@code RobotContainer} and in tests. If
 * this is ever changed, the {@code TeleopDrivePipelineTest} will immediately catch regressions.
 *
 * <p><b>Pipeline stages:</b>
 *
 * <ol>
 *   <li>Deadband (10%) — filters stick drift
 *   <li>Cubic scaling — provides fine control at small deflections
 *   <li>Physical scaling — converts [-1,1] to m/s and rad/s
 *   <li>WPILib sign convention — negates to convert stick-forward to +X
 *   <li>Alliance flip — negates X and Y for Red alliance
 * </ol>
 *
 * <p>Note: slew rate limiting and gyro-lock are stateful operations that remain in {@code
 * RobotContainer} since they depend on previous tick state.
 */
public final class TeleopDriveMath {

  /** Default joystick deadband threshold. */
  public static final double DEADBAND = 0.1;

  private TeleopDriveMath() {} // Utility class — no instances

  /**
   * Computes field-relative ChassisSpeeds from raw joystick axes BEFORE slew rate limiting. This is
   * the pure-math portion of the teleop pipeline: deadband → cube → scale → negate → alliance-flip.
   *
   * @param rawLeftY Raw left stick Y axis (negative = forward per HID convention)
   * @param rawLeftX Raw left stick X axis (negative = left per HID convention)
   * @param rawOmega Raw right stick X axis (negative = CCW per HID convention)
   * @param maxLinearSpeedMps The maximum translation speed of the robot.
   * @param maxAngularSpeedRadPerSec The maximum rotation speed of the robot.
   * @param isRedAlliance Whether the robot is on the Red alliance (flips translation axes)
   * @param outSpeeds The pre-allocated ChassisSpeeds to populate with field-relative velocities.
   *     Omega is negated so that rightward stick (positive HID) produces negative (clockwise)
   *     rotation per WPILib ChassisSpeeds convention.
   */
  public static void computeFieldRelativeSpeeds(
      double rawLeftY,
      double rawLeftX,
      double rawOmega,
      double maxLinearSpeedMps,
      double maxAngularSpeedRadPerSec,
      boolean isRedAlliance,
      ChassisSpeeds outSpeeds) {
    double xVal = MathUtil.applyDeadband(rawLeftY, DEADBAND);
    double yVal = MathUtil.applyDeadband(rawLeftX, DEADBAND);
    double omgVal = MathUtil.applyDeadband(rawOmega, DEADBAND);

    // Cube joystick first to maintain exponential curve, then scale to physical units
    double mappedX = Math.pow(xVal, 3.0) * maxLinearSpeedMps;
    double mappedY = Math.pow(yVal, 3.0) * maxLinearSpeedMps;
    double mappedOmg = Math.pow(omgVal, 3.0) * maxAngularSpeedRadPerSec;

    // Negate for WPILib convention (stick forward = negative axis, but field +X = forward)
    double orientedX = -mappedX;
    double orientedY = -mappedY;

    // Alliance flip — Red drives in the opposite field direction
    if (isRedAlliance) {
      orientedX = -orientedX;
      orientedY = -orientedY;
    }

    outSpeeds.vxMetersPerSecond = orientedX;
    outSpeeds.vyMetersPerSecond = orientedY;
    outSpeeds.omegaRadiansPerSecond = -mappedOmg;
  }
}
