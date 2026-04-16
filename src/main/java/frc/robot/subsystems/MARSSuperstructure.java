package frc.robot.subsystems;

import com.marslib.util.AllianceUtil;
import com.marslib.util.EliteShooterMath;
import com.marslib.util.MARSStateMachine;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.constants.FieldConstants;
import frc.robot.constants.ShooterConstants;
import frc.robot.constants.SuperstructureConstants;
import java.util.Optional;
import java.util.function.Supplier;
import org.littletonrobotics.junction.Logger;

/**
 * Superstructure orchestrator using a validated finite state machine.
 *
 * <p>Coordinates the cowl, intake pivot, floor intake, shooter, and feeder subsystems through safe
 * state transitions. Automatically enters {@link SuperstructureState#BEACHED BEACHED} mode when the
 * robot tilt exceeds 25°, disabling all mechanisms to prevent damage during field obstacle
 * traversal.
 */
public class MARSSuperstructure extends SubsystemBase {

  private final MARSCowl cowl;
  private final MARSIntakePivot intakePivot;
  private final MARSShooter floorIntake;
  private final MARSShooter shooter;
  private final MARSShooter feeder;

  private final Supplier<Pose2d> poseSupplier;

  @SuppressWarnings({"PMD.UnusedPrivateField", "unused"})
  private final Supplier<Optional<Translation2d>> visionTargetSupplier;

  private final Supplier<ChassisSpeeds> fieldSpeedsSupplier;

  /** The set of valid superstructure states. */
  public enum SuperstructureState {
    STOWED,
    INTAKE_DOWN,
    INTAKE_RUNNING,
    SCORE,
    UNJAM,
    BEACHED
  }

  private final MARSStateMachine<SuperstructureState> stateMachine;

  // Caches for zero-allocation performance in hot loop
  private final EliteShooterMath.EliteShooterSetpoint shotCache =
      new EliteShooterMath.EliteShooterSetpoint();
  private final Translation3d redHub3dCache = FieldConstants.RED_HUB_3D;
  private final Translation3d blueHub3dCache = FieldConstants.BLUE_HUB_3D;

  // Math State
  private double goalCowlAngle = 0.0;
  private double goalIntakeAngle = 0.0;
  private int internalPieceCount = edu.wpi.first.wpilibj.RobotBase.isSimulation() ? 40 : 0;
  private int simShooterCooldown = 0;
  private int simDebugCounter = 0;

  private final Supplier<Double> tiltRadiansSupplier;

  /**
   * Constructs the superstructure orchestrator.
   *
   * @param cowl The cowl (hood) rotary mechanism.
   * @param intakePivot The intake pivot rotary mechanism.
   * @param floorIntake The floor intake flywheel (as MARSShooter).
   * @param shooter The main shooter flywheel.
   * @param feeder The feeder flywheel.
   * @param poseSupplier Supplier for the current robot field pose.
   * @param visionTargetSupplier Supplier for the latest vision target translation.
   * @param tiltRadiansSupplier Supplier for the current robot tilt in radians.
   * @param fieldSpeedsSupplier Supplier for the current robot field-relative speeds.
   */
  public MARSSuperstructure(
      MARSCowl cowl,
      MARSIntakePivot intakePivot,
      MARSShooter floorIntake,
      MARSShooter shooter,
      MARSShooter feeder,
      Supplier<Pose2d> poseSupplier,
      Supplier<Optional<Translation2d>> visionTargetSupplier,
      Supplier<Double> tiltRadiansSupplier,
      Supplier<ChassisSpeeds> fieldSpeedsSupplier) {

    this.cowl = cowl;
    this.intakePivot = intakePivot;
    this.floorIntake = floorIntake;
    this.shooter = shooter;
    this.feeder = feeder;
    this.poseSupplier = poseSupplier;
    this.visionTargetSupplier = visionTargetSupplier;
    this.tiltRadiansSupplier = tiltRadiansSupplier;
    this.fieldSpeedsSupplier = fieldSpeedsSupplier;

    stateMachine =
        new MARSStateMachine<>(
            "Superstructure", SuperstructureState.class, SuperstructureState.STOWED);

    stateMachine.addValidBidirectional(SuperstructureState.STOWED, SuperstructureState.INTAKE_DOWN);
    stateMachine.addValidBidirectional(
        SuperstructureState.INTAKE_DOWN, SuperstructureState.INTAKE_RUNNING);
    stateMachine.addValidBidirectional(
        SuperstructureState.STOWED, SuperstructureState.INTAKE_RUNNING);
    stateMachine.addValidBidirectional(SuperstructureState.STOWED, SuperstructureState.SCORE);
    stateMachine.addValidBidirectional(SuperstructureState.STOWED, SuperstructureState.UNJAM);
    stateMachine.addValidBidirectional(SuperstructureState.STOWED, SuperstructureState.BEACHED);
    stateMachine.addValidBidirectional(
        SuperstructureState.INTAKE_DOWN, SuperstructureState.BEACHED);
    stateMachine.addValidBidirectional(
        SuperstructureState.INTAKE_RUNNING, SuperstructureState.BEACHED);
    stateMachine.addValidBidirectional(SuperstructureState.SCORE, SuperstructureState.BEACHED);
    stateMachine.addValidBidirectional(SuperstructureState.UNJAM, SuperstructureState.BEACHED);

    stateMachine.setEntryAction(
        SuperstructureState.SCORE,
        () -> {
          Logger.recordOutput("Superstructure/EntryAction", "SCORE: Readying shot");
        });

    stateMachine.setOnTransition(
        (from, to) -> {
          Logger.recordOutput("Superstructure/Transition", from.name() + " -> " + to.name());
        });
  }

  /**
   * Creates a command that requests a transition to the given superstructure state. If the
   * transition is illegal, it is rejected and logged — no mechanism movement occurs.
   *
   * @param targetState The desired superstructure state.
   * @return A command that executes the transition request.
   */
  public Command setAbsoluteState(SuperstructureState targetState) {
    return Commands.runOnce(
        () -> {
          boolean accepted = stateMachine.requestTransition(targetState);
          if (!accepted) {
            Logger.recordOutput(
                "Superstructure/TransitionRejectedReason",
                stateMachine.getState().name()
                    + " -> "
                    + targetState.name()
                    + " is not a legal transition.");
          }
        },
        this);
  }

  /**
   * Forces the superstructure into the specified state by routing through STOWED first. This
   * bypasses normal transition validation and should only be used for safety overrides (e.g.,
   * automatic beaching on tilt detection).
   *
   * @param targetState The state to force.
   */
  public void forceState(SuperstructureState targetState) {
    stateMachine.requestTransition(SuperstructureState.STOWED);
    stateMachine.requestTransition(targetState);
  }

  @Override
  public void periodic() {
    // Safety: auto-beach when robot tilts beyond 25°
    double tiltDegrees = Math.toDegrees(Math.abs(tiltRadiansSupplier.get()));
    if (tiltDegrees > 25.0 && stateMachine.getState() != SuperstructureState.BEACHED) {
      forceState(SuperstructureState.BEACHED);
    }
    // Automatic recovery: return to STOWED when tilt drops below 20° (hysteresis band)
    if (tiltDegrees < 20.0 && stateMachine.getState() == SuperstructureState.BEACHED) {
      forceState(SuperstructureState.STOWED);
    }

    stateMachine.update();
    SuperstructureState currentState = stateMachine.getState();

    // Cache shot calculation once per loop to avoid duplicate solver runs
    EliteShooterMath.EliteShooterSetpoint cachedShot = null;
    if (currentState == SuperstructureState.SCORE) {
      cachedShot = calculateDynamicShot();
      Logger.recordOutput("Superstructure/SimDebug/ShotIsValid", cachedShot.isValid);
      Logger.recordOutput(
          "Superstructure/SimDebug/ShotLaunchSpeed", cachedShot.launchSpeedMetersPerSec);
      Logger.recordOutput("Superstructure/SimDebug/ShotHoodRads", cachedShot.hoodRadians);
    }

    // Update mechanism targets based on current state
    updateMechanismTargets(currentState, cachedShot);
    cowl.setTargetPosition(goalCowlAngle);
    intakePivot.setTargetPosition(goalIntakeAngle);

    // Execute state-specific motor logic
    handleIntakeLogic(currentState);
    handleScoringLogic(currentState, cachedShot);
    handleUnjamLogic(currentState);

    // Log state
    logOutputs(currentState);
  }

  @Override
  public void simulationPeriodic() {
    SuperstructureState currentState = stateMachine.getState();

    // 1. Simulate Intaking
    if (currentState == SuperstructureState.INTAKE_RUNNING) {
      if (internalPieceCount < 40) { // Limit to 40 pieces as requested!
        // Calculate intake position relative to robot pose
        Pose2d robotPose = poseSupplier.get();
        edu.wpi.first.math.geometry.Translation2d intakeCenter =
            robotPose
                .getTranslation()
                .plus(
                    new edu.wpi.first.math.geometry.Translation2d(-0.4, 0)
                        .rotateBy(robotPose.getRotation()));

        org.dyn4j.dynamics.Body overlappingFuel =
            com.marslib.simulation.MARSPhysicsWorld.getInstance()
                .getOverlappingFuel(intakeCenter, 0.4);
        if (overlappingFuel != null) {
          com.marslib.simulation.MARSPhysicsWorld.getInstance().removeFuel(overlappingFuel);
          internalPieceCount++;
          Logger.recordOutput(
              "Superstructure/SimEvent", "Gathered Game Piece. Total: " + internalPieceCount);
        }
      }
    }

    // 2. Simulate Shooting
    if (simShooterCooldown > 0) {
      simShooterCooldown--;
    } else if (currentState == SuperstructureState.SCORE && internalPieceCount > 0) {
      // Log exactly why we cannot or can fire — this is the core diagnostic
      boolean shooterReady = shooter.isAtTolerance();
      boolean cowlReady = cowl.isAtTolerance();
      Logger.recordOutput("Superstructure/SimDebug/ShooterAtTolerance", shooterReady);
      Logger.recordOutput("Superstructure/SimDebug/CowlAtTolerance", cowlReady);
      Logger.recordOutput(
          "Superstructure/SimDebug/ShooterVelocity", shooter.getVelocityRadPerSec());
      Logger.recordOutput("Superstructure/SimDebug/CowlPosition", cowl.getPositionRads());
      Logger.recordOutput("Superstructure/SimDebug/GoalCowlAngle", goalCowlAngle);
      Logger.recordOutput("Superstructure/SimDebug/ShooterTarget", goalCowlAngle);

      // Throttled console print every 1s (50 loops) so we can see in terminal
      simDebugCounter++;
      if (simDebugCounter % 50 == 0) {
        System.out.printf(
            "[SIM-SHOOT] shooterVel=%.1f (ok=%b) cowlPos=%.3f/goal=%.3f (ok=%b) pieces=%d%n",
            shooter.getVelocityRadPerSec(),
            shooterReady,
            cowl.getPositionRads(),
            goalCowlAngle,
            cowlReady,
            internalPieceCount);
      }

      if (shooterReady && cowlReady) {
        // Triggers exactly when we start feeding the piece into the shooter
        internalPieceCount--;
        simShooterCooldown = 15; // 300ms delay between shots
        Logger.recordOutput(
            "Superstructure/SimEvent", "Fired Shot! Remaining: " + internalPieceCount);

        Pose2d robotPose = poseSupplier.get();
        edu.wpi.first.math.geometry.Pose3d nozzlePose =
            new edu.wpi.first.math.geometry.Pose3d(robotPose)
                .plus(
                    new edu.wpi.first.math.geometry.Transform3d(
                        0.2,
                        0,
                        0.6,
                        new edu.wpi.first.math.geometry.Rotation3d(0, -goalCowlAngle, 0)));

        double launchSpeedMetersPerSec =
            shooter.getVelocityRadPerSec()
                * frc.robot.constants.ShooterConstants.SHOOTER_WHEEL_RADIUS_METERS;

        // goalCowlAngle comes from EliteShooterMath which calculates pitch relative to the flat
        // horizontal plane
        double pitch = goalCowlAngle;

        double launchSpeedX =
            Math.cos(robotPose.getRotation().getRadians())
                * launchSpeedMetersPerSec
                * Math.cos(pitch);
        double launchSpeedY =
            Math.sin(robotPose.getRotation().getRadians())
                * launchSpeedMetersPerSec
                * Math.cos(pitch);
        double launchSpeedZ = launchSpeedMetersPerSec * Math.sin(pitch);

        com.marslib.simulation.SimulationProjectile shot =
            new com.marslib.simulation.SimulationProjectile(
                nozzlePose, launchSpeedX, launchSpeedY, launchSpeedZ);
        com.marslib.simulation.MARSPhysicsWorld.getInstance().addProjectile(shot);
      }
    }
  }

  /** Sets goalCowlAngle and goalIntakeAngle based on the current state. */
  private void updateMechanismTargets(
      SuperstructureState currentState, EliteShooterMath.EliteShooterSetpoint cachedShot) {
    switch (currentState) {
      case INTAKE_DOWN:
      case INTAKE_RUNNING:
        goalIntakeAngle = SuperstructureConstants.INTAKE_PIVOT_ANGLE;
        goalCowlAngle = 0.0;
        break;
      case SCORE:
        goalIntakeAngle = 0.0;
        if (cachedShot != null && cachedShot.isValid) {
          goalCowlAngle = cachedShot.hoodRadians;
        } else {
          goalCowlAngle = SuperstructureConstants.SCORE_FALLBACK_COWL_ANGLE;
        }
        break;
      case STOWED:
      case UNJAM:
      case BEACHED:
      default:
        goalIntakeAngle = 0.0;
        goalCowlAngle = 0.0;
        break;
    }
  }

  /** Handles intake roller activation and physics-based game piece collection. */
  private void handleIntakeLogic(SuperstructureState currentState) {
    if (currentState == SuperstructureState.INTAKE_RUNNING) {
      floorIntake.setVoltage(12.0);
    } else if (currentState != SuperstructureState.SCORE
        && currentState != SuperstructureState.UNJAM) {
      floorIntake.setVoltage(0.0);
    }
  }

  /** Handles shooter spin-up, flywheel readiness checks, feeding, and game piece launching. */
  private void handleScoringLogic(
      SuperstructureState currentState, EliteShooterMath.EliteShooterSetpoint cachedShot) {
    if (currentState == SuperstructureState.SCORE) {
      double targetRadPerSec = 4000.0 * Math.PI * 2.0 / 60.0; // Default fallback
      if (cachedShot != null && cachedShot.isValid) {
        targetRadPerSec =
            cachedShot.launchSpeedMetersPerSec / ShooterConstants.SHOOTER_WHEEL_RADIUS_METERS;
      }

      shooter.setClosedLoopVelocity(targetRadPerSec);

      // Wait for flywheel and cowl to be at tolerance before transferring
      if (shooter.isAtTolerance() && cowl.isAtTolerance()) {
        feeder.setVoltage(12.0);
        floorIntake.setVoltage(12.0);
      } else {
        feeder.setVoltage(0.0);
        floorIntake.setVoltage(0.0);
      }
    } else if (currentState != SuperstructureState.UNJAM
        && currentState != SuperstructureState.BEACHED) {
      // Manage idle flywheel speed when not scoring
      double currentRPM = shooter.getVelocityRadPerSec() * 60.0 / (Math.PI * 2.0);
      if (currentRPM > 1600.0) {
        shooter.setVoltage(0.0); // Coast down via friction
      } else {
        double idleRadPerSec = 1500.0 * Math.PI * 2.0 / 60.0;
        shooter.setClosedLoopVelocity(idleRadPerSec); // Maintain idle speed
      }
      feeder.setVoltage(0.0);
    } else if (currentState == SuperstructureState.BEACHED) {
      shooter.setVoltage(0.0);
      feeder.setVoltage(0.0);
    }
  }

  /** Handles reverse motor operation for unjamming game pieces. */
  private void handleUnjamLogic(SuperstructureState currentState) {
    if (currentState == SuperstructureState.UNJAM) {
      feeder.setVoltage(-6.0);
      floorIntake.setVoltage(-6.0);
      shooter.setVoltage(-6.0);
    }
  }

  /**
   * Calculates a dynamic shot setpoint to the alliance-appropriate hub using {@link
   * EliteShooterMath} and current field velocities. Inherently acts as a static solver when speeds
   * are zero.
   */
  private EliteShooterMath.EliteShooterSetpoint calculateDynamicShot() {
    Translation3d targetHub = AllianceUtil.isRed() ? redHub3dCache : blueHub3dCache;

    // The fieldSpeedsSupplier from RobotContainer already provides field-relative speeds
    // via ChassisSpeeds.fromRobotRelativeSpeeds(). Do NOT convert again — that was a bug
    // introduced by a previous fix that double-rotated the velocity vector.
    return EliteShooterMath.calculateShotOnTheMove(
        poseSupplier.get(),
        fieldSpeedsSupplier.get(),
        targetHub,
        FieldConstants.GAME_PIECE_REST_HEIGHT_METERS,
        ShooterConstants.PROJECTILE_SPEED_MPS,
        -9.81,
        0.1,
        shotCache);
  }

  /** Logs all superstructure telemetry outputs. */
  private void logOutputs(SuperstructureState currentState) {
    Logger.recordOutput("Superstructure/GoalCowlAngle", goalCowlAngle);
    Logger.recordOutput("Superstructure/GoalIntakeAngle", goalIntakeAngle);
    Logger.recordOutput("Superstructure/CurrentState", currentState.name());
    Logger.recordOutput("Superstructure/InternalPieceCount", internalPieceCount);
  }

  /**
   * Returns the current superstructure state.
   *
   * @return The active {@link SuperstructureState}.
   */
  public SuperstructureState getCurrentState() {
    return stateMachine.getState();
  }

  /**
   * Returns the underlying state machine for testing and diagnostics.
   *
   * @return The {@link MARSStateMachine} instance.
   */
  public MARSStateMachine<SuperstructureState> getStateMachine() {
    return stateMachine;
  }
}
