package frc.robot.subsystems.shooter;

import org.littletonrobotics.junction.AutoLog;

/**
 * Hardware abstraction interface for the Shooter flywheel mechanism.
 *
 * <p>Units: position in rotations, velocity in rot/s.
 */
public interface ShooterIO {
  @AutoLog
  class ShooterIOInputs {
    /** Current flywheel position in rotations. */
    public double positionRotations = 0.0;

    /** Current flywheel velocity in rot/s. */
    public double velocityRotationsPerSec = 0.0;

    /** Voltage applied to the motor in volts. */
    public double appliedVolts = 0.0;

    /** Stator current draw in amps. */
    public double[] currentAmps = new double[] {};

    /** Motor temperature in degrees Celsius. */
    public double[] temperatureCelsius = new double[] {};
  }

  /** Updates the set of loggable inputs. */
  default void updateInputs(ShooterIOInputs inputs) {}

  /** Run open loop at the specified voltage. */
  default void setVoltage(double volts) {}

  /** Stop the motor immediately. */
  default void stop() {}
}
