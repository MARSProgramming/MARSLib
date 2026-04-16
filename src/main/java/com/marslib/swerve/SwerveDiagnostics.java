/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.swerve;

import static edu.wpi.first.units.Units.Volts;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import org.littletonrobotics.junction.Logger;

/**
 * Diagnostic and automated alignment sequences for the Swerve Drive. Handles SystemChecks and
 * WPILib SysId integration.
 */
public final class SwerveDiagnostics {

  private final SwerveDrive drive;
  private final SysIdRoutine sysIdRoutine;

  public SwerveDiagnostics(SwerveDrive drive, SwerveModule... modules) {
    this.drive = drive;

    this.sysIdRoutine =
        new SysIdRoutine(
            new SysIdRoutine.Config(
                null,
                null,
                null,
                (state) -> Logger.recordOutput("SysIdTestState", state.toString())),
            new SysIdRoutine.Mechanism(
                (edu.wpi.first.units.measure.Voltage volts) -> {
                  for (SwerveModule mod : modules) {
                    mod.setDriveVoltage(volts.in(Volts));
                    mod.setTurnPosition(0.0);
                  }
                },
                null, // Log is handled implicitly via AdvantageKit's @AutoLog IO capturing
                drive));
  }

  public Command sysIdQuasistatic(SysIdRoutine.Direction direction) {
    return sysIdRoutine.quasistatic(direction);
  }

  public Command sysIdDynamic(SysIdRoutine.Direction direction) {
    return sysIdRoutine.dynamic(direction);
  }

  /**
   * Open-loop dead-reckoning sequence designed to precisely lock the robot onto the climb chain
   * using predefined translational bump limits.
   */
  public Command finalClimbLineupCommand() {
    ChassisSpeeds leftSpeed = new ChassisSpeeds(0.0, -0.5, 0.0);
    ChassisSpeeds fwdSpeed = new ChassisSpeeds(0.5, 0.0, 0.0);
    ChassisSpeeds stopSpeed = new ChassisSpeeds();

    return Commands.sequence(
            Commands.run(() -> drive.runVelocity(leftSpeed), drive).withTimeout(0.5),
            Commands.run(() -> drive.runVelocity(fwdSpeed), drive).withTimeout(0.5))
        .finallyDo(() -> drive.runVelocity(stopSpeed));
  }

  /** Verification sequence to ensure Modules are mechanically linked and not browning out. */
  public Command getSystemCheckCommand() {
    return Commands.sequence(
            Commands.runOnce(
                () -> {
                  SwerveModuleState[] steerStates = new SwerveModuleState[4];
                  for (int i = 0; i < 4; i++) {
                    steerStates[i] = new SwerveModuleState(0, Rotation2d.fromDegrees(90));
                  }
                  drive.setModuleStates(steerStates);
                }),
            Commands.waitSeconds(1.5),
            Commands.runOnce(
                () -> {
                  if (com.marslib.faults.MARSFaultManager.hasActiveCriticalFaults()) {
                    new com.marslib.faults.Alert(
                            "SystemCheck: Swerve module(s) failed to reach 90°.",
                            com.marslib.faults.Alert.AlertType.CRITICAL)
                        .set(true);
                  }
                }),
            Commands.runOnce(
                () -> {
                  SwerveModuleState[] zeroStates = new SwerveModuleState[4];
                  for (int i = 0; i < 4; i++) {
                    zeroStates[i] = new SwerveModuleState(0, Rotation2d.fromDegrees(0));
                  }
                  drive.setModuleStates(zeroStates);
                }),
            Commands.waitSeconds(1.5),
            Commands.runOnce(
                () -> {
                  if (com.marslib.faults.MARSFaultManager.hasActiveCriticalFaults()) {
                    new com.marslib.faults.Alert(
                            "SystemCheck: Swerve module(s) failed to return to 0°.",
                            com.marslib.faults.Alert.AlertType.CRITICAL)
                        .set(true);
                  }
                }))
        .withName("SwerveSystemTest");
  }
}
