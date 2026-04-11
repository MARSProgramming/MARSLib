package frc.robot.constants;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;

/**
 * Field-level constants for the 2026 REBUILT game.
 *
 * <p>All dimensions are sourced from the maple-sim {@code Arena2026Rebuilt} field geometry and the
 * official FRC Game Manual to ensure simulation-to-reality parity.
 */
public final class FieldConstants {

  // ---- Field Dimensions ----

  /** Standard FRC field length (meters). Matches maple-sim Arena2026Rebuilt. */
  public static final double FIELD_LENGTH_METERS = 16.54105;

  /** Standard FRC field width (meters). Matches maple-sim Arena2026Rebuilt. */
  public static final double FIELD_WIDTH_METERS = 8.06926;

  /** Thickness of simulated field boundary walls (meters). */
  public static final double WALL_THICKNESS_METERS = 1.0;

  // ---- Game Piece Properties ----

  /**
   * Radius of a single Rebuilt Fuel ball (meters). 15 cm diameter = 7.5 cm radius. Source:
   * RebuiltFuelOnField.REBUILT_FUEL_INFO
   */
  public static final double GAME_PIECE_RADIUS_METERS = 0.075;

  /** Height above the field surface at which game pieces rest in 3D visualization (meters). */
  public static final double GAME_PIECE_REST_HEIGHT_METERS = GAME_PIECE_RADIUS_METERS;

  /**
   * Mass of a single Rebuilt Fuel ball (kg). 0.5 lbs = 0.2268 kg. Source:
   * RebuiltFuelOnField.REBUILT_FUEL_INFO
   */
  public static final double GAME_PIECE_MASS_KG = 0.2268;

  /** Coulomb friction coefficient for field boundary walls and obstacles. */
  public static final double WALL_FRICTION = 0.2;

  /** Coefficient of restitution (bounciness) for field boundary walls and obstacles. */
  public static final double WALL_RESTITUTION = 0.1;

  // ---- Hub Positions ----
  // Source: Arena2026Rebuilt field obstacle map (hub center X/Y from CAD)

  /** Approximate side length of the hub structures (meters). */
  public static final double HUB_SIZE_METERS = 1.194;

  /** Blue alliance hub center position (meters). */
  public static final Translation2d BLUE_HUB_POS = new Translation2d(4.597, 4.035);

  /** Red alliance hub center position (meters). */
  public static final Translation2d RED_HUB_POS = new Translation2d(11.938, 4.035);

  /** Blue hub 3D target for shooter math (top opening, meters). */
  public static final Translation3d BLUE_HUB_3D =
      new Translation3d(BLUE_HUB_POS.getX(), BLUE_HUB_POS.getY(), 2.64);

  /** Red hub 3D target for shooter math (top opening, meters). */
  public static final Translation3d RED_HUB_3D =
      new Translation3d(RED_HUB_POS.getX(), RED_HUB_POS.getY(), 2.64);

  // ---- Intake ----

  /** Intake collection radius (meters) for swallowing pieces in simulation. */
  public static final double INTAKE_COLLECTION_RADIUS_METERS = 1.0;

  // ---- Climb Positions ----

  public static final edu.wpi.first.math.geometry.Pose2d BLUE_NEARDEPOT_CLIMB_POSE =
      new edu.wpi.first.math.geometry.Pose2d(
          1.1, 4.86, edu.wpi.first.math.geometry.Rotation2d.fromDegrees(180));
  public static final edu.wpi.first.math.geometry.Pose2d BLUE_OUTPOST_CLIMB_POSE =
      new edu.wpi.first.math.geometry.Pose2d(
          1.0, 2.6, edu.wpi.first.math.geometry.Rotation2d.fromDegrees(0));
  public static final edu.wpi.first.math.geometry.Pose2d RED_NEARDEPOT_CLIMB_POSE =
      new edu.wpi.first.math.geometry.Pose2d(
          15.35, 3.2, edu.wpi.first.math.geometry.Rotation2d.fromDegrees(0));
  public static final edu.wpi.first.math.geometry.Pose2d RED_NEAROUTPOST_CLIMB_POSE =
      new edu.wpi.first.math.geometry.Pose2d(
          15.4, 5.45, edu.wpi.first.math.geometry.Rotation2d.fromDegrees(180));

  private static final java.util.List<edu.wpi.first.math.geometry.Pose2d> CLIMB_POSES_BLUE =
      java.util.List.of(BLUE_NEARDEPOT_CLIMB_POSE, BLUE_OUTPOST_CLIMB_POSE);
  private static final java.util.List<edu.wpi.first.math.geometry.Pose2d> CLIMB_POSES_RED =
      java.util.List.of(RED_NEARDEPOT_CLIMB_POSE, RED_NEAROUTPOST_CLIMB_POSE);

  /**
   * Returns the closest climbing position to the robot based on current alliance.
   *
   * @param currentRobotPose The robot's current field-relative pose.
   * @return The nearest climb approach pose.
   */
  public static edu.wpi.first.math.geometry.Pose2d getClosestClimbingPosition(
      edu.wpi.first.math.geometry.Pose2d currentRobotPose) {
    java.util.Optional<edu.wpi.first.wpilibj.DriverStation.Alliance> alliance =
        edu.wpi.first.wpilibj.DriverStation.getAlliance();
    if (alliance.isPresent()
        && alliance.get() == edu.wpi.first.wpilibj.DriverStation.Alliance.Blue) {
      return currentRobotPose.nearest(CLIMB_POSES_BLUE);
    }
    return currentRobotPose.nearest(CLIMB_POSES_RED);
  }

  // ---- Simulation Tuning ----

  /**
   * Controls whether the maple-sim arena runs in efficiency mode.
   *
   * <p><b>OFF (false)</b>: Full realism — all game pieces spawned, both depots, all center field
   * pieces. Recommended for match-accurate simulation and autonomous testing.
   *
   * <p><b>ON (true)</b>: Reduced piece count — only 1/3 of center pieces and only your alliance's
   * depot. Useful for faster iteration during mechanism development when full field population is
   * unnecessary.
   */
  public static final boolean MAPLE_SIM_EFFICIENCY_MODE = false;

  private FieldConstants() {}
}
