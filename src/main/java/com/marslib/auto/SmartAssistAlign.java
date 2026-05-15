/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.auto;

import com.marslib.swerve.SwerveConfig;
import com.marslib.swerve.SwerveDrive;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.Command;
import java.util.function.DoubleSupplier;

/**
 * Command for dynamic, odometry-based robot alignment specifically for scoring on standard FRC grid
 * nodes.
 */
public class SmartAssistAlign extends Command {

  private final SwerveDrive swerveDrive;
  private final DoubleSupplier forwardThrottleSupplier;
  private final Pose2d targetNode;

  private final PIDController yAlignController;
  private final PIDController thetaAlignController;

  // Pre-allocated cache to avoid ChassisSpeeds.fromFieldRelativeSpeeds() heap allocation
  private final ChassisSpeeds robotSpeedsCache = new ChassisSpeeds();
  private final edu.wpi.first.wpilibj.Timer safetyTimer = new edu.wpi.first.wpilibj.Timer();

  /**
   * Overrides the driver's lateral (Y) and rotational (Theta) control to perfectly track a specific
   * field coordinate, while allowing them to maintain forward/backward (X) speed.
   *
   * @param swerveDrive Standard drive subsystem hook.
   * @param forwardThrottleSupplier Lambda to the driver's X-axis joystick input.
   * @param targetNode The stationary coordinate mapping we want to align to.
   */
  public SmartAssistAlign(
      SwerveDrive swerveDrive, DoubleSupplier forwardThrottleSupplier, Pose2d targetNode) {
    this.swerveDrive = swerveDrive;
    this.forwardThrottleSupplier = forwardThrottleSupplier;
    this.targetNode = targetNode;

    SwerveConfig config = swerveDrive.getConfig();

    // These controllers compare the Robot's true position to the Node's true position
    this.yAlignController = new PIDController(config.alignTranslationKp(), 0, 0);
    this.yAlignController.setIZone(config.alignTranslationIZoneMeters());

    this.thetaAlignController = new PIDController(config.alignThetaKp(), 0, 0);
    this.thetaAlignController.setIZone(config.alignThetaIZoneRad());

    this.thetaAlignController.enableContinuousInput(-Math.PI, Math.PI);

    addRequirements(swerveDrive);
  }

  @Override
  public void initialize() {
    safetyTimer.restart();
  }

  @Override
  public void execute() {
    Pose2d currentPose = swerveDrive.getPose();

    // 1. Let the driver keep control of the speed advancing towards the grid (Field X)
    double fieldVx = forwardThrottleSupplier.getAsDouble();

    // 2. Automate strafing to line up exactly with target Y (Field Y)
    double fieldVy = yAlignController.calculate(currentPose.getY(), targetNode.getY());

    // 3. Automate rotation to point exactly at target heading (Field Theta)
    double omega =
        thetaAlignController.calculate(
            currentPose.getRotation().getRadians(), targetNode.getRotation().getRadians());

    // Inline fromFieldRelativeSpeeds to avoid new ChassisSpeeds() allocation
    double cosHeading = currentPose.getRotation().getCos();
    double sinHeading = currentPose.getRotation().getSin();
    robotSpeedsCache.vxMetersPerSecond = fieldVx * cosHeading + fieldVy * sinHeading;
    robotSpeedsCache.vyMetersPerSecond = -fieldVx * sinHeading + fieldVy * cosHeading;
    robotSpeedsCache.omegaRadiansPerSecond = omega;

    // Feed to kinematics
    swerveDrive.runVelocity(robotSpeedsCache);
  }

  @Override
  public void end(boolean interrupted) {
    robotSpeedsCache.vxMetersPerSecond = 0.0;
    robotSpeedsCache.vyMetersPerSecond = 0.0;
    robotSpeedsCache.omegaRadiansPerSecond = 0.0;
    swerveDrive.runVelocity(robotSpeedsCache);
  }

  @Override
  public boolean isFinished() {
    Pose2d currentPose = swerveDrive.getPose();
    double yError = Math.abs(currentPose.getY() - targetNode.getY());
    double thetaError =
        Math.abs(
            edu.wpi.first.math.MathUtil.angleModulus(
                currentPose.getRotation().getRadians() - targetNode.getRotation().getRadians()));
    // Converged when within 2cm laterally and 2° rotationally, or fallback safety timeout
    return safetyTimer.hasElapsed(5.0) || (yError < 0.02 && thetaError < Math.toRadians(2.0));
  }
}
