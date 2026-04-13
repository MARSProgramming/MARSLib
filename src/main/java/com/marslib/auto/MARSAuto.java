/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.auto;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.path.PathConstraints;
import com.pathplanner.lib.path.PathPlannerPath;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.constants.AutoConstants;

/**
 * Utility Command factory for autonomous routines.
 *
 * <p>Students: This class hooks directly into PathPlanner and Choreo. Use `runChoreoTrajectory` to
 * execute the exact pre-rendered JSON files mapped in your deploy directory natively.
 */
public class MARSAuto {

  /**
   * Executes a pre-planned, time-optimal Choreo trajectory via PathPlannerLib. Retains support for
   * PathPlanner NamedCommands hooks.
   *
   * @param choreoTrajectoryName The filename of the trajectory inside deploy/choreo
   * @return The autonomous execution Command
   */
  public static Command runChoreoTrajectory(String choreoTrajectoryName) {
    try {
      PathPlannerPath path = PathPlannerPath.fromChoreoTrajectory(choreoTrajectoryName);
      return AutoBuilder.followPath(path);
    } catch (Exception e) {
      new com.marslib.faults.Alert(
              "MARSAuto: Failed to load Choreo trajectory: " + choreoTrajectoryName,
              com.marslib.faults.Alert.AlertType.CRITICAL)
          .set(true);
      return edu.wpi.first.wpilibj2.command.Commands.none();
    }
  }

  /**
   * Executes a pre-planned Choreo trajectory but dynamically pathfinds to the start position first
   * if the robot is off-target. This is the ultimate "Hybrid" approach.
   *
   * @param choreoTrajectoryName The filename of the trajectory inside deploy/choreo
   * @return The autonomous execution Command
   */
  public static Command pathfindThenRunChoreoTrajectory(String choreoTrajectoryName) {
    try {
      PathPlannerPath path = PathPlannerPath.fromChoreoTrajectory(choreoTrajectoryName);

      // Constraints for navigating to the starting pose of the trajectory
      PathConstraints pathfindingConstraints =
          new PathConstraints(
              AutoConstants.MAX_VELOCITY_MPS,
              AutoConstants.MAX_ACCELERATION_MPS2,
              AutoConstants.MAX_ANGULAR_VELOCITY_RAD_PER_SEC,
              AutoConstants.MAX_ANGULAR_ACCELERATION_RAD_PER_SEC2);

      return AutoBuilder.pathfindThenFollowPath(path, pathfindingConstraints);
    } catch (Exception e) {
      new com.marslib.faults.Alert(
              "MARSAuto: Failed to load Choreo pathfinding: " + choreoTrajectoryName,
              com.marslib.faults.Alert.AlertType.CRITICAL)
          .set(true);
      return edu.wpi.first.wpilibj2.command.Commands.none();
    }
  }

  /**
   * Utilizes PathPlanner's A* Pathfinding to navigate avoiding obstacles. Helpful for dynamic
   * teleop routing.
   *
   * @param targetPose The destination field pose
   * @return Pathfinding command
   */
  public static Command pathfindObstacleAvoidance(Pose2d targetPose) {
    // Tunable dynamic pathfinding constraints
    PathConstraints constraints =
        new PathConstraints(
            AutoConstants.MAX_VELOCITY_MPS,
            AutoConstants.MAX_ACCELERATION_MPS2,
            AutoConstants.MAX_ANGULAR_VELOCITY_RAD_PER_SEC,
            AutoConstants.MAX_ANGULAR_ACCELERATION_RAD_PER_SEC2);

    // Pathfind dynamically to target, ending with 0 velocity
    return AutoBuilder.pathfindToPose(
        targetPose, constraints, 0.0 // Goal end velocity
        );
  }
}
