/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.swerve;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.config.ModuleConfig;
import com.pathplanner.lib.config.PIDConstants;
import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.controllers.PPHolonomicDriveController;
import com.pathplanner.lib.path.PathConstraints;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Encapsulates the static configurations for PathPlanner's AutoBuilder. Kept independent from the
 * main drivetrain subsystem to allow headless mock testing.
 */
public final class SwerveAutoBuilder {
  private SwerveAutoBuilder() {}

  /**
   * Configures PathPlanner's AutoBuilder for autonomous path following.
   *
   * @param drive The SwerveDrive subsystem instance for method references and command requirements.
   */
  public static void configure(SwerveDrive drive) {
    try {
      SwerveConfig swerveConfig = drive.getConfig();
      DCMotor gearbox = DCMotor.getKrakenX60(1).withReduction(swerveConfig.driveGearRatio());

      ModuleConfig moduleConfig =
          new ModuleConfig(
              swerveConfig.wheelRadiusMeters(),
              swerveConfig.maxLinearSpeedMps(),
              swerveConfig.wheelCOFStatic(),
              gearbox,
              swerveConfig.driveStatorCurrentLimit(),
              1);

      RobotConfig config =
          new RobotConfig(
              swerveConfig.robotMassKg(),
              swerveConfig.robotMoiKgM2(),
              moduleConfig,
              swerveConfig.moduleLocations());

      AutoBuilder.configure(
          drive::getPose,
          drive::resetPose,
          drive::getChassisSpeeds,
          (speeds, feedforwards) -> drive.runVelocity(speeds),
          new PPHolonomicDriveController(
              new PIDConstants(
                  swerveConfig.autoTranslationKp(), 0.0, swerveConfig.autoTranslationKd()),
              new PIDConstants(swerveConfig.autoRotationKp(), 0.0, swerveConfig.autoRotationKd())),
          config,
          () ->
              DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue)
                  == DriverStation.Alliance.Red, // Mirror paths for Red
          drive);
    } catch (Exception e) {
      new com.marslib.faults.Alert(
              "SwerveDrive: Failed to configure AutoBuilder: " + e.getMessage(),
              com.marslib.faults.Alert.AlertType.CRITICAL)
          .set(true);
      // Do NOT re-throw — the robot must remain operable in teleop even if auto config fails.
      // The CRITICAL alert will be visible on the dashboard and AdvantageScope.
    }
  }

  /**
   * Generates a command that automatically paths the robot to the specified target utilizing
   * PathPlanner's physics-constrained solver to avoid field obstacles dynamically.
   *
   * @param drive The SwerveDrive logic
   * @param target A supplier of the absolute field pose to navigate to.
   * @return A Command orchestrating the autonomous drive.
   */
  public static Command alignToPoint(SwerveDrive drive, Supplier<Pose2d> target) {
    return Commands.defer(
        () -> {
          SwerveConfig swerveConfig = drive.getConfig();
          return AutoBuilder.pathfindToPose(
                  target.get(),
                  new PathConstraints(
                      swerveConfig.maxLinearSpeedMps(),
                      swerveConfig.maxLinearSpeedMps() * 0.7,
                      swerveConfig.maxAngularSpeedRadPerSec(),
                      swerveConfig.maxAngularSpeedRadPerSec() * 0.7),
                  0.0)
              .withTimeout(3.0);
        },
        Set.of(drive));
  }
}
