package frc.robot.commands;

import com.marslib.swerve.SwerveDrive;
import com.marslib.util.EliteShooterMath;
import com.marslib.util.EliteShooterMath.EliteShooterSetpoint;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.MARSCowl;
import frc.robot.subsystems.MARSShooter;
import java.util.function.DoubleSupplier;

/**
 * Overrides the driver's rotational (Theta) control to perfectly track a specific field coordinate
 * using Velocity-Added Kinematic Leading, while allowing them to freely strafe and sprint in X/Y.
 *
 * <p><b>Mathematical Architecture:</b> Replaced entirely by the robust EliteShooterMath engine
 * ported from Team 254. Solves exact Time-of-Flight quadratics and natively outputs required Cowl
 * pitches, Flywheel speeds, and Chassis angular feedforwards.
 */
public class ShootOnTheMoveCommand extends Command {

  private final SwerveDrive swerveDrive;
  private final MARSCowl cowl;
  private final MARSShooter shooter;
  private final DoubleSupplier joystickX;
  private final DoubleSupplier joystickY;

  private final PIDController thetaAlignController;
  private final com.marslib.swerve.TractionControlLimiter tractionLimiter =
      new com.marslib.swerve.TractionControlLimiter(
          frc.robot.constants.DriveConstants.TELEOP_LINEAR_ACCEL_LIMIT);

  // Conversion scalar to turn linear velocity of the game piece into angular velocity for the
  // shooter
  // (Tune this parameter based on wheel radius and surface slip!)
  private static final double VELOCITY_TO_RAD_PER_SEC = 30.0;

  private static final Translation3d BLUE_HUB_3D =
      new Translation3d(
          frc.robot.constants.FieldConstants.BLUE_HUB_POS.getX(),
          frc.robot.constants.FieldConstants.BLUE_HUB_POS.getY(),
          frc.robot.constants.FieldConstants.HUB_SIZE_METERS);

  private static final Translation3d RED_HUB_3D =
      new Translation3d(
          frc.robot.constants.FieldConstants.RED_HUB_POS.getX(),
          frc.robot.constants.FieldConstants.RED_HUB_POS.getY(),
          frc.robot.constants.FieldConstants.HUB_SIZE_METERS);

  public ShootOnTheMoveCommand(
      SwerveDrive swerveDrive,
      MARSCowl cowl,
      MARSShooter shooter,
      DoubleSupplier joystickX,
      DoubleSupplier joystickY) {
    this.swerveDrive = swerveDrive;
    this.cowl = cowl;
    this.shooter = shooter;
    this.joystickX = joystickX;
    this.joystickY = joystickY;

    this.thetaAlignController =
        new PIDController(frc.robot.constants.AutoConstants.ALIGN_THETA_KP, 0, 0);
    this.thetaAlignController.enableContinuousInput(-Math.PI, Math.PI);

    addRequirements(swerveDrive, cowl, shooter);
  }

  @Override
  public void execute() {
    Pose2d currentPose = swerveDrive.getPose();

    // 1. Let the driver keep complete X/Y translating freedom
    double fieldVx = joystickX.getAsDouble();
    double fieldVy = joystickY.getAsDouble();

    // 2. Extract true robot instantaneous momentum for math
    ChassisSpeeds currentSpeeds = swerveDrive.getChassisSpeeds();
    ChassisSpeeds currentFieldSpeeds =
        ChassisSpeeds.fromRobotRelativeSpeeds(currentSpeeds, currentPose.getRotation());

    // 3. Determine dynamic target based on alliance natively without GC allocation
    Translation3d targetNode = BLUE_HUB_3D;
    if (DriverStation.getAlliance().isPresent()
        && DriverStation.getAlliance().get() == Alliance.Red) {
      targetNode = RED_HUB_3D;
    }

    // 4. Exact True-Vector Quadratic Time-Of-Flight Intersection Solver
    EliteShooterSetpoint setpoint =
        EliteShooterMath.calculateShotOnTheMove(
            currentPose,
            currentFieldSpeeds,
            targetNode,
            frc.robot.constants.FieldConstants.GAME_PIECE_REST_HEIGHT_METERS,
            frc.robot.constants.ShooterConstants.PROJECTILE_SPEED_MPS,
            -9.81,
            0.1 // Fuel aerodynamic lift coefficient
            );

    // 5. Calculate heading intercept (with feedforward)
    double aimTheta =
        setpoint.isValid
            ? setpoint.robotAimYawRadians
            : Math.atan2(
                targetNode.getY() - currentPose.getY(), targetNode.getX() - currentPose.getX());

    double pidOmega =
        thetaAlignController.calculate(currentPose.getRotation().getRadians(), aimTheta);
    double feedforwardOmega = setpoint.isValid ? setpoint.chassisAngularFeedforward : 0.0;

    // Combine PID stabilization with dynamic target kinematic tracking
    double finalOmega = pidOmega + feedforwardOmega;

    // 6. Package field-centric commands into kinematics
    // Note: Translation2d does not have setters, so we simply construct one or bypass if possible.
    // Given tractionLimiter requires Translation2d, we must allocate it unless modified.
    Translation2d limitedTrans = tractionLimiter.calculate(new Translation2d(fieldVx, fieldVy));

    ChassisSpeeds robotSpeeds =
        ChassisSpeeds.fromFieldRelativeSpeeds(
            limitedTrans.getX(), limitedTrans.getY(), finalOmega, currentPose.getRotation());

    swerveDrive.runVelocity(robotSpeeds);

    // 7. Auto-adjust Cowl and Flywheel on-the-fly!
    if (setpoint.isValid) {
      cowl.setTargetPosition(setpoint.hoodRadians);
      shooter.setClosedLoopVelocity(setpoint.launchSpeedMetersPerSec * VELOCITY_TO_RAD_PER_SEC);
    }
  }

  @Override
  public void end(boolean interrupted) {
    swerveDrive.runVelocity(new ChassisSpeeds());
  }
}
