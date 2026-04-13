/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.mechanisms;

import org.littletonrobotics.junction.AutoLog;

/** Hardware abstraction interface for a rotary mechanism (like an arm or wrist). */
public interface RotaryMechanismIO {
  @AutoLog
  public static class RotaryMechanismIOInputs {
    public boolean hasHardwareConnected = true;
    public double positionRad = 0.0;
    public double velocityRadPerSec = 0.0;
    public double targetVelocityRadPerSec = 0.0;
    public double appliedVolts = 0.0;
    public double[] currentAmps = new double[] {};
  }

  public default void updateInputs(RotaryMechanismIOInputs inputs) {}

  public default void setVoltage(double volts) {}

  /** Runs a low latency closed-loop profile on motor with external feedforward. */
  public default void setClosedLoopPosition(double positionRad, double feedforwardVolts) {}

  /** Resets the internal encoder to a given position. */
  public default void setEncoderPosition(double positionRad) {}

  public default void setCurrentLimit(double amps) {}

  public default void setBrakeMode(boolean enable) {}
}
