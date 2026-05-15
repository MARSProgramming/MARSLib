/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.auto;

import com.marslib.swerve.SwerveDrive;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj2.command.Command;
import java.util.function.Supplier;

/**
 * Command to bypass default PathPlanner routing and dynamically align the robot natively onto a
 * dynamically moving or stationary field target timestamped via Vision.
 */
public class MARSAlignmentCommand extends Command {
  private final SwerveDrive swerveDrive;
  private final Supplier<Pose2d> targetPoseSupplier;

  private final ProfiledPIDController xController;
  private final ProfiledPIDController yController;
  private final ProfiledPIDController thetaController;

  private final double translationTolerance;
  private final double rotationTolerance;

  // Pre-allocated cache to avoid ChassisSpeeds.fromFieldRelativeSpeeds() heap allocation
  private final ChassisSpeeds fieldRelativeCache = new ChassisSpeeds();
  private final edu.wpi.first.wpilibj.Timer safetyTimer = new edu.wpi.first.wpilibj.Timer();

  /** Constructs an alignment command with parameterized constants. */
  public MARSAlignmentCommand(
      SwerveDrive swerveDrive,
      Supplier<Pose2d> targetPoseSupplier,
      double kPTranslation,
      double kPRotation,
      double maxTranslationVelMps,
      double maxTranslationAccelMps2,
      double maxRotationVelRps,
      double maxRotationAccelRps2,
      double translationToleranceMeters,
      double rotationToleranceRad) {

    this.swerveDrive = swerveDrive;
    this.targetPoseSupplier = targetPoseSupplier;
    this.translationTolerance = translationToleranceMeters;
    this.rotationTolerance = rotationToleranceRad;

    TrapezoidProfile.Constraints translationConstraints =
        new TrapezoidProfile.Constraints(maxTranslationVelMps, maxTranslationAccelMps2);
    TrapezoidProfile.Constraints rotationConstraints =
        new TrapezoidProfile.Constraints(maxRotationVelRps, maxRotationAccelRps2);

    xController = new ProfiledPIDController(kPTranslation, 0, 0, translationConstraints);
    yController = new ProfiledPIDController(kPTranslation, 0, 0, translationConstraints);

    thetaController = new ProfiledPIDController(kPRotation, 0, 0, rotationConstraints);
    thetaController.enableContinuousInput(-Math.PI, Math.PI);

    addRequirements(swerveDrive);
  }

  @Override
  public void initialize() {
    Pose2d currentPose = swerveDrive.getPose();
    xController.reset(currentPose.getX());
    yController.reset(currentPose.getY());
    thetaController.reset(currentPose.getRotation().getRadians());
    safetyTimer.restart();
  }

  @Override
  public void execute() {
    Pose2d target = targetPoseSupplier.get();
    Pose2d currentPos = swerveDrive.getPose();

    double xFeedback = xController.calculate(currentPos.getX(), target.getX());
    double yFeedback = yController.calculate(currentPos.getY(), target.getY());
    double thetaFeedback =
        thetaController.calculate(
            currentPos.getRotation().getRadians(), target.getRotation().getRadians());

    // Inline fromFieldRelativeSpeeds to avoid new ChassisSpeeds() allocation
    double cosHeading = currentPos.getRotation().getCos();
    double sinHeading = currentPos.getRotation().getSin();
    fieldRelativeCache.vxMetersPerSecond = xFeedback * cosHeading + yFeedback * sinHeading;
    fieldRelativeCache.vyMetersPerSecond = -xFeedback * sinHeading + yFeedback * cosHeading;
    fieldRelativeCache.omegaRadiansPerSecond = thetaFeedback;

    swerveDrive.runVelocity(fieldRelativeCache);
  }

  @Override
  public boolean isFinished() {
    Pose2d target = targetPoseSupplier.get();
    Pose2d currentPos = swerveDrive.getPose();

    Translation2d error = target.getTranslation().minus(currentPos.getTranslation());
    double rotationError =
        Math.abs(currentPos.getRotation().minus(target.getRotation()).getRadians());

    return safetyTimer.hasElapsed(5.0)
        || (error.getNorm() < translationTolerance && rotationError < rotationTolerance);
  }

  @Override
  public void end(boolean interrupted) {
    fieldRelativeCache.vxMetersPerSecond = 0.0;
    fieldRelativeCache.vyMetersPerSecond = 0.0;
    fieldRelativeCache.omegaRadiansPerSecond = 0.0;
    swerveDrive.runVelocity(fieldRelativeCache);
  }
}
