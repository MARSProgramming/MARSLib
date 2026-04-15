/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.swerve;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.MotorOutputConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.marslib.util.CANUtil;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Voltage;

/** Hardware IO implementation for a Swerve Module using CTRE TalonFX motors and a CANcoder. */
public class SwerveModuleIOTalonFX implements SwerveModuleIO {
  /** Maximum number of CAN config retries before reporting a fault. */
  private static final int CONFIG_RETRIES = 5;

  private final TalonFX driveMotor;
  private final TalonFX turnMotor;

  private final StatusSignal<AngularVelocity> driveVelocity;
  private final StatusSignal<AngularVelocity> turnVelocity;
  private final StatusSignal<Voltage> driveAppliedVolts;
  private final StatusSignal<Voltage> turnAppliedVolts;
  private final StatusSignal<Current> driveCurrent;
  private final StatusSignal<Current> turnCurrent;

  private final VoltageOut driveVoltageRequest = new VoltageOut(0.0);
  private final VoltageOut turnVoltageRequest = new VoltageOut(0.0);

  private final int odometryId;
  private final SwerveConfig config;

  public SwerveModuleIOTalonFX(
      int driveMotorId, int turnMotorId, String canbus, SwerveConfig config) {
    driveMotor = new TalonFX(driveMotorId, new com.ctre.phoenix6.CANBus(canbus));
    turnMotor = new TalonFX(turnMotorId, new com.ctre.phoenix6.CANBus(canbus));
    this.config = config;

    TalonFXConfiguration driveConfig = new TalonFXConfiguration();
    driveConfig.CurrentLimits.StatorCurrentLimitEnable = true;
    driveConfig.CurrentLimits.StatorCurrentLimit = config.driveStatorCurrentLimit();
    driveConfig.CurrentLimits.SupplyCurrentLimitEnable = true;
    driveConfig.CurrentLimits.SupplyCurrentLimit = 40.0;
    driveConfig.MotorOutput.NeutralMode = NeutralModeValue.Brake;
    applyWithRetry(driveMotor, driveConfig, "DriveMotor[" + driveMotorId + "]");

    TalonFXConfiguration turnConfig = new TalonFXConfiguration();
    turnConfig.CurrentLimits.StatorCurrentLimitEnable = true;
    turnConfig.CurrentLimits.StatorCurrentLimit = config.turnStatorCurrentLimit();
    turnConfig.CurrentLimits.SupplyCurrentLimitEnable = true;
    turnConfig.CurrentLimits.SupplyCurrentLimit = 40.0;
    turnConfig.MotorOutput.NeutralMode = NeutralModeValue.Brake;
    applyWithRetry(turnMotor, turnConfig, "TurnMotor[" + turnMotorId + "]");

    driveVelocity = driveMotor.getVelocity();
    turnVelocity = turnMotor.getVelocity();
    driveAppliedVolts = driveMotor.getMotorVoltage();
    turnAppliedVolts = turnMotor.getMotorVoltage();
    driveCurrent = driveMotor.getStatorCurrent();
    turnCurrent = turnMotor.getStatorCurrent();

    // Ensure these signals run at standard config frequency
    CANUtil.setUpdateFrequencyWithRetry(
        config.telemetryHz(),
        driveVelocity,
        turnVelocity,
        driveAppliedVolts,
        turnAppliedVolts,
        driveCurrent,
        turnCurrent);

    // Register position signals to Odometry thread
    odometryId =
        PhoenixOdometryThread.getInstance()
            .registerModule(driveMotor.getPosition(), turnMotor.getPosition(), config.odometryHz());

    driveMotor.optimizeBusUtilization();
    turnMotor.optimizeBusUtilization();
  }

  // Pre-allocated odometry arrays — only reallocated when sample count changes (rare)
  private double[] cachedDrivePositionsRad = new double[0];
  private double[] cachedTurnPositionsRad = new double[0];
  private double[] cachedTimestamps = new double[0];

  @Override
  public void updateInputs(SwerveModuleIOInputs inputs) {
    // Query the bulk 50hz telemetry — StatusCode indicates CAN health
    StatusCode refreshStatus =
        BaseStatusSignal.refreshAll(
            driveVelocity, turnVelocity,
            driveAppliedVolts, turnAppliedVolts,
            driveCurrent, turnCurrent);

    inputs.hasHardwareConnected = refreshStatus.isOK();
    // Convert from motor-domain (rotations) to output-shaft-domain (radians at the wheel)
    inputs.driveVelocityRadPerSec =
        Units.rotationsToRadians(driveVelocity.getValueAsDouble()) / config.driveGearRatio();
    inputs.turnVelocityRadPerSec =
        Units.rotationsToRadians(turnVelocity.getValueAsDouble()) / config.turnGearRatio();
    inputs.driveAppliedVolts = driveAppliedVolts.getValueAsDouble();
    inputs.turnAppliedVolts = turnAppliedVolts.getValueAsDouble();
    inputs.driveCurrentAmps = driveCurrent.getValueAsDouble();
    inputs.turnCurrentAmps = turnCurrent.getValueAsDouble();

    // Drain the high-frequency buffer (returns pre-allocated SyncData)
    PhoenixOdometryThread.SyncData data =
        PhoenixOdometryThread.getInstance().getSyncData(odometryId);
    int count = data.validCount;

    // Only reallocate when sample count changes (typically stable at ~5 samples per drain)
    if (cachedDrivePositionsRad.length != count) {
      cachedDrivePositionsRad = new double[count];
      cachedTurnPositionsRad = new double[count];
      cachedTimestamps = new double[count];
    }

    // Convert from motor rotations to output-shaft radians (post-gearing)
    for (int i = 0; i < count; i++) {
      cachedDrivePositionsRad[i] =
          Units.rotationsToRadians(data.drivePositions[i]) / config.driveGearRatio();
      cachedTurnPositionsRad[i] =
          Units.rotationsToRadians(data.turnPositions[i]) / config.turnGearRatio();
      cachedTimestamps[i] = data.timestamps[i];
    }

    inputs.drivePositionsRad = cachedDrivePositionsRad;
    inputs.turnPositionsRad = cachedTurnPositionsRad;
    inputs.odometryTimestamps = cachedTimestamps;
  }

  @Override
  public void setDriveVoltage(double volts) {
    driveMotor.setControl(driveVoltageRequest.withOutput(volts));
  }

  @Override
  public void setTurnVoltage(double volts) {
    turnMotor.setControl(turnVoltageRequest.withOutput(volts));
  }

  @Override
  public void setDriveBrakeMode(boolean enable) {
    MotorOutputConfigs config = new MotorOutputConfigs();
    StatusCode refreshStatus = driveMotor.getConfigurator().refresh(config);
    config.NeutralMode = enable ? NeutralModeValue.Brake : NeutralModeValue.Coast;
    StatusCode applyStatus = driveMotor.getConfigurator().apply(config);

    if (!refreshStatus.isOK() || !applyStatus.isOK()) {
      new com.marslib.faults.Alert(
              "SwerveModule DriveBrakeMode failed: "
                  + refreshStatus.getName()
                  + "/"
                  + applyStatus.getName(),
              com.marslib.faults.Alert.AlertType.WARNING)
          .set(true);
    }
  }

  @Override
  public void setTurnBrakeMode(boolean enable) {
    MotorOutputConfigs config = new MotorOutputConfigs();
    StatusCode refreshStatus = turnMotor.getConfigurator().refresh(config);
    config.NeutralMode = enable ? NeutralModeValue.Brake : NeutralModeValue.Coast;
    StatusCode applyStatus = turnMotor.getConfigurator().apply(config);

    if (!refreshStatus.isOK() || !applyStatus.isOK()) {
      new com.marslib.faults.Alert(
              "SwerveModule TurnBrakeMode failed: "
                  + refreshStatus.getName()
                  + "/"
                  + applyStatus.getName(),
              com.marslib.faults.Alert.AlertType.WARNING)
          .set(true);
    }
  }

  /**
   * Applies a TalonFX configuration with retry logic for CAN bus contention.
   *
   * @param motor The TalonFX motor to configure.
   * @param config The configuration to apply.
   * @param label A human-readable label for fault reporting.
   */
  private static void applyWithRetry(TalonFX motor, TalonFXConfiguration config, String label) {
    StatusCode status = StatusCode.StatusCodeNotInitialized;
    for (int i = 0; i < CONFIG_RETRIES; i++) {
      status = motor.getConfigurator().apply(config);
      if (status.isOK()) {
        return;
      }
    }
    new com.marslib.faults.Alert(
            "SwerveModule " + label + " config failed: " + status.getName(),
            com.marslib.faults.Alert.AlertType.CRITICAL)
        .set(true);
  }
}
