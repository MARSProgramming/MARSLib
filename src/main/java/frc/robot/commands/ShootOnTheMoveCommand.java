package frc.robot.commands;

import com.marslib.swerve.SwerveDrive;
import com.marslib.util.AllianceUtil;
import com.marslib.util.EliteShooterMath;
import com.marslib.util.EliteShooterMath.EliteShooterSetpoint;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.MARSSuperstructure;
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
  private final DoubleSupplier joystickX;
  private final DoubleSupplier joystickY;

  private final PIDController thetaAlignController;
  private final com.marslib.swerve.TractionControlLimiter tractionLimiter =
      new com.marslib.swerve.TractionControlLimiter(
          frc.robot.constants.DriveConstants.TELEOP_LINEAR_ACCEL_LIMIT);

  private final MARSSuperstructure superstructure;
  private final EliteShooterSetpoint shotCache = new EliteShooterSetpoint();
  private final ChassisSpeeds targetSpeeds = new ChassisSpeeds();

  private static final Translation3d BLUE_HUB_3D = frc.robot.constants.FieldConstants.BLUE_HUB_3D;

  private static final Translation3d RED_HUB_3D = frc.robot.constants.FieldConstants.RED_HUB_3D;

  public ShootOnTheMoveCommand(
      SwerveDrive swerveDrive,
      MARSSuperstructure superstructure,
      DoubleSupplier joystickX,
      DoubleSupplier joystickY) {
    this.swerveDrive = swerveDrive;
    this.superstructure = superstructure;
    this.joystickX = joystickX;
    this.joystickY = joystickY;

    this.thetaAlignController =
        new PIDController(
            frc.robot.constants.AutoConstants.ALIGN_THETA_KP,
            0,
            frc.robot.constants.AutoConstants.ALIGN_THETA_KD);
    this.thetaAlignController.setIZone(Math.toRadians(5.0)); // Task 3
    this.thetaAlignController.enableContinuousInput(-Math.PI, Math.PI);

    addRequirements(swerveDrive);
  }

  @Override
  public void initialize() {
    this.thetaAlignController.reset();
    if (superstructure != null) {
      superstructure
          .getStateMachine()
          .requestTransition(frc.robot.subsystems.MARSSuperstructure.SuperstructureState.SCORE);
    }
    org.littletonrobotics.junction.Logger.recordOutput(
        "ShootOnTheMove/State", "INITIALIZED - Requested SCORE");

    // Sync limiter timestamp to avoid jumpy time-deltas
    edu.wpi.first.math.kinematics.ChassisSpeeds robotSpeeds = swerveDrive.getChassisSpeeds();
    edu.wpi.first.math.kinematics.ChassisSpeeds fieldSpeeds =
        edu.wpi.first.math.kinematics.ChassisSpeeds.fromRobotRelativeSpeeds(
            robotSpeeds, swerveDrive.getPose().getRotation());
    tractionLimiter.reset(fieldSpeeds.vxMetersPerSecond, fieldSpeeds.vyMetersPerSecond);
  }

  @Override
  public void execute() {
    // Continuously assert SCORE state every frame. Self-transitions are accepted as no-ops
    // by MARSStateMachine, so this is zero-cost when already SCORE. This defends against
    // other bindings (e.g., leftTrigger.onFalse → STOWED) racing with this command.
    if (superstructure != null) {
      superstructure
          .getStateMachine()
          .requestTransition(frc.robot.subsystems.MARSSuperstructure.SuperstructureState.SCORE);
    }

    Pose2d currentPose = swerveDrive.getPose();

    // 1. Let the driver keep complete X/Y translating freedom
    double fieldVx = joystickX.getAsDouble();
    double fieldVy = joystickY.getAsDouble();

    // 2. Extract true robot instantaneous momentum for math
    ChassisSpeeds currentSpeeds = swerveDrive.getChassisSpeeds();
    ChassisSpeeds currentFieldSpeeds =
        ChassisSpeeds.fromRobotRelativeSpeeds(currentSpeeds, currentPose.getRotation());

    // 3. Determine dynamic target based on alliance natively without GC allocation
    Translation3d targetNode = AllianceUtil.isRed() ? RED_HUB_3D : BLUE_HUB_3D;

    // 4. Exact True-Vector Quadratic Time-Of-Flight Intersection Solver
    EliteShooterSetpoint setpoint =
        EliteShooterMath.calculateShotOnTheMove(
            currentPose,
            currentFieldSpeeds,
            targetNode,
            frc.robot.constants.FieldConstants.GAME_PIECE_REST_HEIGHT_METERS,
            frc.robot.constants.ShooterConstants.PROJECTILE_SPEED_MPS,
            -9.81,
            0.1, // Fuel aerodynamic lift coefficient
            shotCache);

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
    // Clamp to 80% maximum rotation to ensure the swerve drive retains at least 20%
    // output authority for lateral translation to maintain driver mobility
    double unclampedOmega = pidOmega + feedforwardOmega;
    double limit = swerveDrive.getConfig().maxAngularSpeedRadPerSec() * 0.8;
    double finalOmega = edu.wpi.first.math.MathUtil.clamp(unclampedOmega, -limit, limit);

    // 6. Package field-centric commands into kinematics
    tractionLimiter.calculate(fieldVx, fieldVy, targetSpeeds);

    ChassisSpeeds robotSpeeds =
        ChassisSpeeds.fromFieldRelativeSpeeds(
            targetSpeeds.vxMetersPerSecond,
            targetSpeeds.vyMetersPerSecond,
            finalOmega,
            currentPose.getRotation());

    swerveDrive.runVelocity(robotSpeeds);
  }

  @Override
  public boolean isFinished() {
    return false; // Designed to run until button release; safety timeout applied at binding site
  }

  @Override
  public void end(boolean interrupted) {
    org.littletonrobotics.junction.Logger.recordOutput(
        "ShootOnTheMove/State", "ENDED - interrupted=" + interrupted);
    if (superstructure != null) {
      superstructure
          .getStateMachine()
          .requestTransition(frc.robot.subsystems.MARSSuperstructure.SuperstructureState.STOWED);
    }
    swerveDrive.runVelocity(new ChassisSpeeds());
  }
}
