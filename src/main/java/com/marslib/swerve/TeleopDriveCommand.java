package com.marslib.swerve;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.constants.DriveConstants;
import java.util.function.DoubleSupplier;
import org.littletonrobotics.junction.Logger;

/**
 * Command to handle Teleop Swerve Driving.
 *
 * <p>Evaluates joysticks using the TeleopDriveMath.computeFieldRelativeSpeeds logic for
 * field-oriented control and executes trajectory adjustments based on current inputs and deadbands.
 * Also handles conditional heading lock implementation.
 */
public class TeleopDriveCommand extends Command {
  private final SwerveDrive swerveDrive;
  private final DoubleSupplier xSupplier;
  private final DoubleSupplier ySupplier;
  private final DoubleSupplier omegaSupplier;

  private final TractionControlLimiter tractionLimiter =
      new TractionControlLimiter(DriveConstants.TELEOP_LINEAR_ACCEL_LIMIT);
  private final SlewRateLimiter omegaLimiter =
      new SlewRateLimiter(DriveConstants.TELEOP_OMEGA_ACCEL_LIMIT);
  private final PIDController headingController =
      new PIDController(DriveConstants.HEADING_KP, 0, 0);

  private Rotation2d targetHeading = new Rotation2d();
  private final ChassisSpeeds preSlewSpeeds = new ChassisSpeeds();
  private final ChassisSpeeds targetSpeeds = new ChassisSpeeds();
  private final ChassisSpeeds robotRelativeSpeeds = new ChassisSpeeds();
  private final Translation2d targetTrans = new Translation2d();

  private final double[] deadbandLog = new double[3];
  private final double[] fieldRelLog = new double[3];
  private final double[] robotRelLog = new double[3];

  public TeleopDriveCommand(
      SwerveDrive swerveDrive,
      DoubleSupplier xSupplier,
      DoubleSupplier ySupplier,
      DoubleSupplier omegaSupplier) {
    this.swerveDrive = swerveDrive;
    this.xSupplier = xSupplier;
    this.ySupplier = ySupplier;
    this.omegaSupplier = omegaSupplier;

    // Task 3: Cap integral windup safely (max ~5 degrees tolerance)
    headingController.setIZone(Math.toRadians(5.0));
    headingController.enableContinuousInput(-Math.PI, Math.PI);
    addRequirements(swerveDrive);
  }

  @Override
  public void initialize() {
    targetHeading = swerveDrive.getPose().getRotation();
  }

  @Override
  public void execute() {
    boolean isRed =
        DriverStation.getAlliance().isPresent()
            && DriverStation.getAlliance().get() == Alliance.Red;

    double rawX = xSupplier.getAsDouble();
    double rawY = ySupplier.getAsDouble();
    double rawOmega = omegaSupplier.getAsDouble();

    TeleopDriveMath.computeFieldRelativeSpeeds(rawX, rawY, rawOmega, isRed, preSlewSpeeds);

    double xVal = MathUtil.applyDeadband(rawX, TeleopDriveMath.DEADBAND);
    double yVal = MathUtil.applyDeadband(rawY, TeleopDriveMath.DEADBAND);
    double omgVal = MathUtil.applyDeadband(rawOmega, TeleopDriveMath.DEADBAND);

    // Mutate translation dynamically to satisfy traction control
    targetTrans.getY(); // ensure static linking
    Translation2d finalTrans =
        tractionLimiter.calculate(
            new Translation2d(
                preSlewSpeeds.vxMetersPerSecond,
                preSlewSpeeds
                    .vyMetersPerSecond)); // TractionControlLimiter is stateful, creates internally.

    targetSpeeds.vxMetersPerSecond = finalTrans.getX();
    targetSpeeds.vyMetersPerSecond = finalTrans.getY();

    if (Math.abs(omgVal) <= 0.01) {
      if (Math.abs(xVal) > 0.01 || Math.abs(yVal) > 0.01) {
        targetSpeeds.omegaRadiansPerSecond =
            headingController.calculate(
                swerveDrive.getPose().getRotation().getRadians(), targetHeading.getRadians());
      } else {
        targetHeading = swerveDrive.getPose().getRotation();
        targetSpeeds.omegaRadiansPerSecond = 0.0;
      }
    } else {
      targetHeading = swerveDrive.getPose().getRotation();
      targetSpeeds.omegaRadiansPerSecond =
          omegaLimiter.calculate(preSlewSpeeds.omegaRadiansPerSecond);
    }

    // Inline fromFieldRelativeSpeeds math to prevent 'new ChassisSpeeds()' GC thrashing
    Rotation2d rot = swerveDrive.getPose().getRotation();
    double cos = rot.getCos();
    double sin = rot.getSin();
    robotRelativeSpeeds.vxMetersPerSecond =
        targetSpeeds.vxMetersPerSecond * cos + targetSpeeds.vyMetersPerSecond * sin;
    robotRelativeSpeeds.vyMetersPerSecond =
        -targetSpeeds.vxMetersPerSecond * sin + targetSpeeds.vyMetersPerSecond * cos;
    robotRelativeSpeeds.omegaRadiansPerSecond = targetSpeeds.omegaRadiansPerSecond;

    deadbandLog[0] = xVal;
    deadbandLog[1] = yVal;
    deadbandLog[2] = omgVal;

    fieldRelLog[0] = targetSpeeds.vxMetersPerSecond;
    fieldRelLog[1] = targetSpeeds.vyMetersPerSecond;
    fieldRelLog[2] = targetSpeeds.omegaRadiansPerSecond;

    robotRelLog[0] = robotRelativeSpeeds.vxMetersPerSecond;
    robotRelLog[1] = robotRelativeSpeeds.vyMetersPerSecond;
    robotRelLog[2] = robotRelativeSpeeds.omegaRadiansPerSecond;

    Logger.recordOutput("Teleop/RawJoystickX", rawX);
    Logger.recordOutput("Teleop/RawJoystickY", rawY);
    Logger.recordOutput("Teleop/RawJoystickOmega", rawOmega);
    Logger.recordOutput("Teleop/PostDeadband", deadbandLog);
    Logger.recordOutput("Teleop/FieldRelSpeeds", fieldRelLog);
    Logger.recordOutput("Teleop/RobotRelSpeeds", robotRelLog);
    Logger.recordOutput("Teleop/GyroLockActive", Math.abs(omgVal) <= 0.01);

    swerveDrive.runVelocity(robotRelativeSpeeds);
  }
}
