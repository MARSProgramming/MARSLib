/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.swerve;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import org.littletonrobotics.junction.Logger;

/**
 * Represents a singular Swerve Module (e.g. Front-Left, Front-Right). This class abstracts the
 * actual hardware implementation (TalonFX vs Sim) via the {@link SwerveModuleIO} layer.
 *
 * <p>Students: This class handles the math converting raw Radians from the IO layer into standard
 * WPILib Meters and Meters/Second parameters for the PoseEstimator.
 */
public class SwerveModule {
  private final SwerveModuleIO io;
  private final SwerveModuleIOInputsAutoLogged inputs = new SwerveModuleIOInputsAutoLogged();
  private final SwerveConfig config;
  private final String logPath;

  private double lastDriveVoltage = 0.0;
  private final SwerveModulePosition[] cachedDeltas = new SwerveModulePosition[20];
  private int cachedDeltaCount = 0;

  /**
   * Constructs a generic Swerve Module boundary structure.
   *
   * @param index The ID/Index (0=FL, 1=FR, 2=BL, 3=BR) for structural AdvantageKit logging keys.
   * @param io The implementation-specific IO layer (TalonFX or Sim).
   * @param config The global swerve configuration.
   */
  public SwerveModule(int index, SwerveModuleIO io, SwerveConfig config) {
    this.io = io;
    this.config = config;
    this.logPath = "SwerveDrive/Module" + index;
    for (int i = 0; i < cachedDeltas.length; i++) {
      cachedDeltas[i] = new SwerveModulePosition(0.0, Rotation2d.fromRadians(0.0));
    }
  }

  public void periodic() {
    io.updateInputs(inputs);
    Logger.processInputs(logPath, inputs);

    // Update statically allocated positional cache
    cachedDeltaCount = Math.min(inputs.drivePositionsRad.length, cachedDeltas.length);
    for (int i = 0; i < cachedDeltaCount; i++) {
      cachedDeltas[i].distanceMeters = inputs.drivePositionsRad[i] * config.wheelRadiusMeters();
      double newAngle = inputs.turnPositionsRad[i];
      if (Math.abs(newAngle - cachedDeltas[i].angle.getRadians()) > 1e-6) {
        cachedDeltas[i].angle = Rotation2d.fromRadians(newAngle);
      }
    }
  }

  /** Returns the count of valid samples populated in the buffer during the last periodic cycle. */
  public int getDeltaCount() {
    return cachedDeltaCount;
  }

  /** Accesses the pre-allocated cache representing positional samples without creating arrays. */
  public SwerveModulePosition getCachedDelta(int i) {
    if (cachedDeltaCount == 0) return cachedDeltas[0];
    return cachedDeltas[i];
  }

  /**
   * Returns the hardware timestamps of the high-frequency positional records.
   *
   * @return Array of FPGA/Hardware timestamps tightly synced with {@link #getCachedDelta(int)}.
   */
  public double[] getOdometryTimestamps() {
    return inputs.odometryTimestamps;
  }

  /**
   * Returns the most recent physical location recorded by the drive motors.
   *
   * @return A singular {@link SwerveModulePosition} bounding distance traveled and heading.
   */
  public SwerveModulePosition getLatestPosition() {
    if (cachedDeltaCount == 0) return cachedDeltas[0];
    return cachedDeltas[cachedDeltaCount - 1];
  }

  private final SwerveModuleState cachedLatestState = new SwerveModuleState();

  /**
   * Translates drive wheel RPS and turn module radians into WPILib Velocity metrics.
   *
   * @return A cached {@link SwerveModuleState} tracking linear velocity (m/s) and angular heading.
   *     The returned object is reused — do not store references across ticks.
   */
  public SwerveModuleState getLatestState() {
    cachedLatestState.speedMetersPerSecond =
        inputs.driveVelocityRadPerSec * config.wheelRadiusMeters();
    double angle =
        inputs.turnPositionsRad.length > 0
            ? inputs.turnPositionsRad[inputs.turnPositionsRad.length - 1]
            : 0.0;
    if (Math.abs(angle - cachedLatestState.angle.getRadians()) > 1e-6) {
      cachedLatestState.angle = Rotation2d.fromRadians(angle);
    }
    return cachedLatestState;
  }

  private SwerveModuleState lastDesiredState = new SwerveModuleState();

  /**
   * Optimizes and applies a desired module state (drive speed + turn angle) with closed-loop turn
   * control and simple voltage feedforward for driving.
   *
   * <p>Students: This method first calls {@code SwerveModuleState.optimize()} to minimize turn
   * motor rotation. It then applies a proportional voltage controller to steer the module and a
   * linear voltage mapping for drive speed.
   *
   * @param desiredState The target speed and angle for this module.
   */
  public void setDesiredState(SwerveModuleState desiredState) {
    this.lastDesiredState = desiredState;
    // Get current module angle
    Rotation2d currentAngle =
        Rotation2d.fromRadians(
            inputs.turnPositionsRad.length > 0
                ? inputs.turnPositionsRad[inputs.turnPositionsRad.length - 1]
                : 0.0);

    // Optimize to minimize turn rotation (may flip drive direction)
    desiredState.optimize(currentAngle);

    // Turn voltage: proportional controller on angular error
    double angleErrorRad = desiredState.angle.minus(currentAngle).getRadians();

    // Cosine Compensation: Scale drive speed by cosine of error angle
    // This prevents the robot from driving while the wheels are sideways, eliminating drift.
    double driveVoltage =
        (desiredState.speedMetersPerSecond * Math.cos(angleErrorRad))
            * config.nominalVoltage()
            / config.maxLinearSpeedMps();

    double turnVoltage = angleErrorRad * config.turnKp();
    turnVoltage =
        Math.max(-config.nominalVoltage(), Math.min(config.nominalVoltage(), turnVoltage));

    lastDriveVoltage = driveVoltage;
    io.setDriveVoltage(driveVoltage);
    io.setTurnVoltage(turnVoltage);
  }

  /**
   * Routes target voltage demands safely down into the IO execution layer.
   *
   * @param volts Target requested feedforward / PID voltage calculated securely.
   */
  public void setDriveVoltage(double volts) {
    lastDriveVoltage = volts;
    io.setDriveVoltage(volts);
  }

  /** Used strictly for extracting injected sim forces. */
  public double getSimDriveVoltage() {
    return lastDriveVoltage;
  }

  /**
   * Returns the actual applied voltage read from the motor controller or sim model. Useful for
   * SysId extraction and telemetry.
   */
  public double getDriveAppliedVoltage() {
    return inputs.driveAppliedVolts;
  }

  /** Gets the actual stator current flowing through the drive motor. */
  public double getDriveCurrentAmps() {
    return inputs.driveCurrentAmps;
  }

  /** Gets the previously cached desired state of the module for traction comparison. */
  public SwerveModuleState getDesiredState() {
    return lastDesiredState;
  }

  /**
   * Routes steer voltage demands safely down into the IO execution layer.
   *
   * @param volts Target requested feedforward / PID voltage calculated securely.
   */
  public void setTurnVoltage(double volts) {
    io.setTurnVoltage(volts);
  }

  /**
   * Injects the maple-sim module simulation reference into the underlying IO layer if it is a
   * {@link SwerveModuleIOSim}. This ensures the sim IO reads encoder readings from the single
   * physics engine rather than running its own duplicate motor simulation.
   *
   * <p>No-op if the IO layer is not a sim implementation.
   *
   * @param simModule The {@link org.ironmaple.simulation.drivesims.SwerveModuleSimulation} instance
   *     to inject.
   */
  public void injectModuleSimulation(
      org.ironmaple.simulation.drivesims.SwerveModuleSimulation simModule) {
    if (io instanceof SwerveModuleIOSim) {
      ((SwerveModuleIOSim) io).setModuleSimulation(simModule);
    }
  }
}
