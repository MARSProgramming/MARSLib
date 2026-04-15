/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.swerve;

import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.system.plant.LinearSystemId;
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
  private final DCMotorSim driveSim =
      new DCMotorSim(
          LinearSystemId.createDCMotorSystem(DCMotor.getKrakenX60Foc(1), 0.025, 6.12),
          DCMotor.getKrakenX60Foc(1));
  private final DCMotorSim steerSim =
      new DCMotorSim(
          LinearSystemId.createDCMotorSystem(DCMotor.getKrakenX60Foc(1), 0.004, 15.0),
          DCMotor.getKrakenX60Foc(1));

  private double driveAppliedVolts = 0.0;
  private double turnAppliedVolts = 0.0;

  private final double[] drivePositionsRadBuffer = new double[1];
  private final double[] turnPositionsRadBuffer = new double[1];
  private final double[] odometryTimestampsBuffer = new double[1];

  @SuppressWarnings("PMD.UnusedFormalParameter")
  public SwerveModuleIOSim(int moduleIndex) {}

  @Override
  public void updateInputs(SwerveModuleIOInputs inputs) {
    // Step the physics sims by the standard 20ms WPILib loop duration
    driveSim.update(0.02);
    steerSim.update(0.02);

    inputs.hasHardwareConnected = true;

    inputs.driveVelocityRadPerSec = driveSim.getAngularVelocityRadPerSec();
    inputs.turnVelocityRadPerSec = steerSim.getAngularVelocityRadPerSec();

    drivePositionsRadBuffer[0] = driveSim.getAngularPositionRad();
    inputs.drivePositionsRad = drivePositionsRadBuffer;

    turnPositionsRadBuffer[0] = steerSim.getAngularPositionRad();
    inputs.turnPositionsRad = turnPositionsRadBuffer;

    inputs.driveCurrentAmps = driveSim.getCurrentDrawAmps();
    inputs.turnCurrentAmps = steerSim.getCurrentDrawAmps();

    inputs.driveAppliedVolts = driveAppliedVolts;
    inputs.turnAppliedVolts = turnAppliedVolts;

    odometryTimestampsBuffer[0] = Timer.getFPGATimestamp();
    inputs.odometryTimestamps = odometryTimestampsBuffer;
  }

  @Override
  public void setDriveVoltage(double volts) {
    driveAppliedVolts = volts;
    driveSim.setInputVoltage(volts);
  }

  @Override
  public void setTurnVoltage(double volts) {
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
