/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.swerve;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.system.plant.LinearSystemId;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.simulation.DCMotorSim;

/**
 * Simulation IO layer for a single swerve module.
 *
 * <p>Both the drive and turn motor physics are handled purely by WPILib's native {@link
 * DCMotorSim}.
 */
public class SwerveModuleIOSim implements SwerveModuleIO {
  // Generic Swerve Module estimations (Kraken X60 FOC)
  private final DCMotorSim driveSim;
  private final DCMotorSim steerSim;

  private double driveAppliedVolts = 0.0;
  private double turnAppliedVolts = 0.0;

  private final double[] drivePositionsRadBuffer = new double[1];
  private final double[] turnPositionsRadBuffer = new double[1];
  private final double[] odometryTimestampsBuffer = new double[1];

  private final PIDController turnController;
  private final PIDController driveController;
  private final SimpleMotorFeedforward driveFeedforward;
  private final SwerveConfig config;

  @SuppressWarnings("PMD.UnusedFormalParameter")
  public SwerveModuleIOSim(int moduleIndex, SwerveConfig config) {
    this.config = config;

    driveSim =
        new DCMotorSim(
            LinearSystemId.createDCMotorSystem(
                DCMotor.getKrakenX60Foc(1), 0.01, config.driveGearRatio()),
            DCMotor.getKrakenX60Foc(1));

    steerSim =
        new DCMotorSim(
            LinearSystemId.createDCMotorSystem(
                DCMotor.getKrakenX60Foc(1), 1.0, config.turnGearRatio()),
            DCMotor.getKrakenX60Foc(1));
    turnController = new PIDController(config.turnKp(), 0.0, config.turnKd());
    double maxTurnRots = 0.5 * config.turnGearRatio();
    turnController.enableContinuousInput(-maxTurnRots, maxTurnRots);

    driveController = new PIDController(config.driveKp(), 0.0, config.driveKd());
    driveFeedforward =
        new SimpleMotorFeedforward(config.driveKs(), config.driveKv(), config.driveKa());
  }

  @Override
  public void updateInputs(SwerveModuleIOInputs inputs) {
    // Step the physics sims by the standard synchronized loop duration
    driveSim.update(frc.robot.constants.ModeConstants.LOOP_PERIOD_SECS);
    steerSim.update(frc.robot.constants.ModeConstants.LOOP_PERIOD_SECS);

    inputs.hasHardwareConnected = true;

    inputs.turnVelocityRadPerSec = steerSim.getAngularVelocityRadPerSec();
    // Decouple hardware rotor backward spin from true wheel output
    inputs.driveVelocityRadPerSec =
        driveSim.getAngularVelocityRadPerSec()
            + (inputs.turnVelocityRadPerSec * config.couplingRatio());

    turnPositionsRadBuffer[0] = steerSim.getAngularPositionRad();
    inputs.turnPositionsRad = turnPositionsRadBuffer;

    drivePositionsRadBuffer[0] =
        driveSim.getAngularPositionRad() + (turnPositionsRadBuffer[0] * config.couplingRatio());
    inputs.drivePositionsRad = drivePositionsRadBuffer;

    inputs.driveCurrentAmps = driveSim.getCurrentDrawAmps();
    inputs.turnCurrentAmps = steerSim.getCurrentDrawAmps();

    inputs.driveAppliedVolts = driveAppliedVolts;
    inputs.turnAppliedVolts = turnAppliedVolts;

    odometryTimestampsBuffer[0] = Timer.getFPGATimestamp();
    inputs.odometryTimestamps = odometryTimestampsBuffer;
  }

  @Override
  public void setDriveVelocity(double velocityRadPerSec) {
    double targetRPS = Units.radiansToRotations(velocityRadPerSec) * config.driveGearRatio();
    double currentRPS =
        Units.radiansToRotations(driveSim.getAngularVelocityRadPerSec()) * config.driveGearRatio();

    double pidVal = driveController.calculate(currentRPS, targetRPS);
    double ffVal = driveFeedforward.calculate(targetRPS);
    double volts = pidVal + ffVal;

    volts = Math.min(Math.max(volts, -12.0), 12.0);
    driveAppliedVolts = volts;
    driveSim.setInputVoltage(volts);
  }

  @Override
  public void setDriveVoltage(double volts) {
    driveAppliedVolts = volts;
    driveSim.setInputVoltage(volts);
  }

  @Override
  public void setTurnPosition(double positionRad) {
    double targetRots = Units.radiansToRotations(positionRad) * config.turnGearRatio();
    double currentRots =
        Units.radiansToRotations(steerSim.getAngularPositionRad()) * config.turnGearRatio();

    double volts = turnController.calculate(currentRots, targetRots);
    volts = Math.min(Math.max(volts, -12.0), 12.0);
    turnAppliedVolts = volts;
    steerSim.setInputVoltage(volts);
  }

  public double getSimDriveVoltage() {
    return driveAppliedVolts;
  }

  public double getSimTurnVoltage() {
    return turnAppliedVolts;
  }
}
