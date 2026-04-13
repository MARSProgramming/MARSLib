/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.swerve;

import static edu.wpi.first.units.Units.Radians;
import static edu.wpi.first.units.Units.RadiansPerSecond;
import static edu.wpi.first.units.Units.Volts;

import edu.wpi.first.wpilibj.Timer;
import org.ironmaple.simulation.drivesims.SwerveModuleSimulation;
import org.ironmaple.simulation.motorsims.SimulatedMotorController.GenericMotorController;

/**
 * Simulation IO layer for a single swerve module.
 *
 * <p>Both the drive and turn motor physics are handled by the maple-sim {@link
 * SwerveModuleSimulation}.
 */
public class SwerveModuleIOSim implements SwerveModuleIO {
  private SwerveModuleSimulation simModule;
  private GenericMotorController driveCont;
  private GenericMotorController steerCont;

  private double driveAppliedVolts = 0.0;
  private double turnAppliedVolts = 0.0;

  @SuppressWarnings("PMD.UnusedFormalParameter")
  public SwerveModuleIOSim(int moduleIndex) {}

  public void setModuleSimulation(SwerveModuleSimulation sim) {
    this.simModule = sim;
    this.driveCont = sim.useGenericMotorControllerForDrive();
    this.steerCont = sim.useGenericControllerForSteer();
  }

  @Override
  public void updateInputs(SwerveModuleIOInputs inputs) {
    inputs.hasHardwareConnected = true;

    if (simModule != null) {
      inputs.driveVelocityRadPerSec = simModule.getDriveWheelFinalSpeed().in(RadiansPerSecond);
      inputs.turnVelocityRadPerSec = simModule.getSteerAbsoluteEncoderSpeed().in(RadiansPerSecond);

      inputs.drivePositionsRad = new double[] {simModule.getDriveWheelFinalPosition().in(Radians)};
      inputs.turnPositionsRad = new double[] {simModule.getSteerAbsoluteFacing().getRadians()};

      inputs.driveCurrentAmps =
          simModule.getDriveMotorSupplyCurrent().in(edu.wpi.first.units.Units.Amps);
      inputs.turnCurrentAmps =
          simModule.getSteerMotorSupplyCurrent().in(edu.wpi.first.units.Units.Amps);
    } else {
      inputs.driveVelocityRadPerSec = 0.0;
      inputs.turnVelocityRadPerSec = 0.0;
      inputs.drivePositionsRad = new double[] {0.0};
      inputs.turnPositionsRad = new double[] {0.0};
      inputs.driveCurrentAmps = 0.0;
      inputs.turnCurrentAmps = 0.0;
    }

    inputs.driveAppliedVolts = driveAppliedVolts;
    inputs.turnAppliedVolts = turnAppliedVolts;
    inputs.odometryTimestamps = new double[] {Timer.getFPGATimestamp()};
  }

  @Override
  public void setDriveVoltage(double volts) {
    driveAppliedVolts = volts;
    if (driveCont != null) {
      driveCont.requestVoltage(Volts.of(volts));
    }
  }

  @Override
  public void setTurnVoltage(double volts) {
    turnAppliedVolts = volts;
    if (steerCont != null) {
      steerCont.requestVoltage(Volts.of(volts));
    }
  }

  public double getSimDriveVoltage() {
    return driveAppliedVolts;
  }

  public double getSimTurnVoltage() {
    return turnAppliedVolts;
  }
}
